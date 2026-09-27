package com.example.transport.service;

import com.example.transport.dto.CreateTripRequest;
import com.example.transport.model.*;
import com.example.transport.repository.DriverRepository;
import com.example.transport.repository.TransportOrderRepository;
import com.example.transport.repository.TripRepository;
import com.example.transport.repository.TruckRepository;
import com.example.transport.util.GeoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TripService {

    private static final Logger log = LoggerFactory.getLogger(TripService.class);

    // A truck is considered unavailable for a new assignment while it has a
    // trip in any of these statuses -- mirrors the driver AVAILABLE/ON_TRIP check.
    private static final List<TripStatus> ACTIVE_TRIP_STATUSES = List.of(
            TripStatus.PLANNED, TripStatus.EN_ROUTE, TripStatus.ARRIVED);

    private final TripRepository tripRepository;
    private final TransportOrderRepository transportOrderRepository;
    private final TruckRepository truckRepository;
    private final DriverRepository driverRepository;
    private final NotificationService notificationService;

    @Value("${app.trip.default-arrival-radius-meters:300}")
    private double defaultArrivalRadiusMeters;

    public TripService(TripRepository tripRepository, TransportOrderRepository transportOrderRepository,
                        TruckRepository truckRepository, DriverRepository driverRepository,
                        NotificationService notificationService) {
        this.tripRepository = tripRepository;
        this.transportOrderRepository = transportOrderRepository;
        this.truckRepository = truckRepository;
        this.driverRepository = driverRepository;
        this.notificationService = notificationService;
    }

    /**
     * Assigns a truck + driver to a transport order, creating a PLANNED trip.
     * The order moves to ASSIGNED and the driver to ON_TRIP.
     */
    public Trip createTrip(CreateTripRequest request) {
        TransportOrder order = transportOrderRepository.findById(request.getTransportOrderId())
                .orElseThrow(() -> new IllegalArgumentException("No transport order with id " + request.getTransportOrderId()));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order " + order.getOrderNumber() + " is not PENDING (currently " + order.getStatus() + ")");
        }

        Truck truck = truckRepository.findById(request.getTruckId())
                .orElseThrow(() -> new IllegalArgumentException("No truck with id " + request.getTruckId()));
        if (tripRepository.existsByTruckIdAndStatusIn(truck.getId(), ACTIVE_TRIP_STATUSES)) {
            throw new IllegalStateException("Truck " + truck.getId() + " already has an active trip");
        }

        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new IllegalArgumentException("No driver with id " + request.getDriverId()));
        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new IllegalStateException("Driver " + driver.getName() + " is not AVAILABLE (currently " + driver.getStatus() + ")");
        }

        Trip trip = new Trip();
        trip.setTransportOrder(order);
        trip.setTruck(truck);
        trip.setDriver(driver);
        trip.setStatus(TripStatus.PLANNED);
        trip.setDestinationName(order.getDestinationName());
        trip.setDestinationLatitude(order.getDestinationLatitude());
        trip.setDestinationLongitude(order.getDestinationLongitude());
        trip.setArrivalRadiusMeters(request.getArrivalRadiusMeters());

        order.setStatus(OrderStatus.ASSIGNED);
        driver.setStatus(DriverStatus.ON_TRIP);

        transportOrderRepository.save(order);
        driverRepository.save(driver);
        return tripRepository.save(trip);
    }

    /** Dispatch: the truck is now moving toward the destination. */
    public Trip startTrip(Long tripId) {
        Trip trip = getOrThrow(tripId);
        if (trip.getStatus() != TripStatus.PLANNED) {
            throw new IllegalStateException("Trip " + tripId + " is not PLANNED (currently " + trip.getStatus() + ")");
        }
        trip.setStatus(TripStatus.EN_ROUTE);
        trip.setStartedAtEpochSeconds(Instant.now().getEpochSecond());

        TransportOrder order = trip.getTransportOrder();
        order.setStatus(OrderStatus.IN_TRANSIT);
        transportOrderRepository.save(order);

        return tripRepository.save(trip);
    }

    /** Dispatcher/ops confirms delivery is done (proof of delivery, unloaded, etc). */
    public Trip completeTrip(Long tripId, Double actualCost) {
        Trip trip = getOrThrow(tripId);
        if (trip.getStatus() != TripStatus.ARRIVED) {
            throw new IllegalStateException("Trip " + tripId + " is not ARRIVED yet (currently " + trip.getStatus() + ")");
        }
        trip.setStatus(TripStatus.COMPLETED);
        trip.setCompletedAtEpochSeconds(Instant.now().getEpochSecond());
        trip.setActualCost(actualCost);

        TransportOrder order = trip.getTransportOrder();
        order.setStatus(OrderStatus.DELIVERED);
        transportOrderRepository.save(order);

        Driver driver = trip.getDriver();
        driver.setStatus(DriverStatus.AVAILABLE);
        driverRepository.save(driver);

        return tripRepository.save(trip);
    }

    public Trip cancelTrip(Long tripId) {
        Trip trip = getOrThrow(tripId);
        if (trip.getStatus() == TripStatus.COMPLETED || trip.getStatus() == TripStatus.CANCELLED) {
            throw new IllegalStateException("Trip " + tripId + " is already " + trip.getStatus());
        }
        trip.setStatus(TripStatus.CANCELLED);

        TransportOrder order = trip.getTransportOrder();
        order.setStatus(OrderStatus.PENDING);
        transportOrderRepository.save(order);

        Driver driver = trip.getDriver();
        driver.setStatus(DriverStatus.AVAILABLE);
        driverRepository.save(driver);

        return tripRepository.save(trip);
    }

    public Trip get(Long tripId) {
        return tripRepository.findById(tripId).orElse(null);
    }

    public List<Trip> list() {
        return tripRepository.findAll();
    }

    public List<Trip> listForTruck(String truckId) {
        return tripRepository.findByTruckIdOrderByIdDesc(truckId);
    }

    /** Ids of trucks currently tied to a PLANNED/EN_ROUTE/ARRIVED trip -- i.e. not assignable. */
    public Set<String> activeTruckIds() {
        return tripRepository.findByStatusIn(ACTIVE_TRIP_STATUSES).stream()
                .map(trip -> trip.getTruck().getId())
                .collect(Collectors.toSet());
    }

    /**
     * Called from LocationService on every fresh location ping. If the given
     * truck has an EN_ROUTE trip and this position is within the destination
     * geofence, mark the trip ARRIVED and fire a notification exactly once.
     */
    public void checkArrival(Truck truck, double latitude, double longitude) {
        tripRepository.findFirstByTruckIdAndStatus(truck.getId(), TripStatus.EN_ROUTE).ifPresent(trip -> {
            if (trip.getDestinationLatitude() == null || trip.getDestinationLongitude() == null) {
                return;
            }
            double radius = trip.getArrivalRadiusMeters() != null ? trip.getArrivalRadiusMeters() : defaultArrivalRadiusMeters;
            double distance = GeoUtils.haversineDistanceMeters(
                    latitude, longitude, trip.getDestinationLatitude(), trip.getDestinationLongitude());

            if (distance <= radius) {
                trip.setStatus(TripStatus.ARRIVED);
                trip.setArrivedAtEpochSeconds(Instant.now().getEpochSecond());
                tripRepository.save(trip);

                log.info("Trip {} arrived at destination (truck {}, {}m from target)", trip.getId(), truck.getId(), (int) distance);
                notificationService.sendTripArrivalNotification(trip);

                trip.setArrivalNotifiedAtEpochSeconds(Instant.now().getEpochSecond());
                tripRepository.save(trip);
            }
        });
    }

    private Trip getOrThrow(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("No trip with id " + tripId));
    }
}
