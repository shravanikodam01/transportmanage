package com.example.transport.service;

import com.example.transport.dto.AvailableTruckResponse;
import com.example.transport.dto.CreateTruckRequest;
import com.example.transport.model.Truck;
import com.example.transport.model.TransportOrder;
import com.example.transport.repository.TransportOrderRepository;
import com.example.transport.repository.TruckRepository;
import com.example.transport.util.GeoUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class TruckService {

    private final TruckRepository truckRepository;
    private final TransportOrderRepository transportOrderRepository;
    private final TripService tripService;

    public TruckService(TruckRepository truckRepository, TransportOrderRepository transportOrderRepository,
                         TripService tripService) {
        this.truckRepository = truckRepository;
        this.transportOrderRepository = transportOrderRepository;
        this.tripService = tripService;
    }

    /**
     * Pre-registers a truck by id, before it's ever sent a GPS ping. Rejects
     * an id that's already taken -- pings against an existing truck should
     * update it via location ingestion, not this endpoint.
     */
    public Truck create(CreateTruckRequest request) {
        if (truckRepository.existsById(request.getId())) {
            throw new IllegalArgumentException("Truck with id " + request.getId() + " already exists");
        }
        Truck truck = new Truck(request.getId(), request.getName(), request.getLicensePlate(), null);
        return truckRepository.save(truck);
    }

    public List<Truck> list() {
        return truckRepository.findAll();
    }

    public Truck get(String id) {
        return truckRepository.findById(id).orElse(null);
    }

    /**
     * Trucks eligible for a given order: not already tied to an active trip,
     * with a known last position, ranked nearest-first to the order's pickup
     * point. If maxDistanceMeters is given, trucks farther than that are excluded.
     */
    public List<AvailableTruckResponse> listAvailableForOrder(Long orderId, Double maxDistanceMeters) {
        TransportOrder order = transportOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("No transport order with id " + orderId));
        if (order.getOriginLatitude() == null || order.getOriginLongitude() == null) {
            throw new IllegalArgumentException("Order " + orderId + " has no pickup location set");
        }

        Set<String> busyTruckIds = tripService.activeTruckIds();

        return truckRepository.findAll().stream()
                .filter(truck -> !busyTruckIds.contains(truck.getId()))
                .filter(truck -> truck.getLastLatitude() != null && truck.getLastLongitude() != null)
                .map(truck -> toResponse(truck, order))
                .filter(response -> maxDistanceMeters == null || response.getDistanceMeters() <= maxDistanceMeters)
                .sorted(Comparator.comparingDouble(AvailableTruckResponse::getDistanceMeters))
                .toList();
    }

    private AvailableTruckResponse toResponse(Truck truck, TransportOrder order) {
        double distanceMeters = GeoUtils.haversineDistanceMeters(
                order.getOriginLatitude(), order.getOriginLongitude(),
                truck.getLastLatitude(), truck.getLastLongitude());
        return new AvailableTruckResponse(truck.getId(), truck.getName(), truck.getLicensePlate(),
                truck.getLastLocationName(), truck.getLastLatitude(), truck.getLastLongitude(), distanceMeters);
    }
}