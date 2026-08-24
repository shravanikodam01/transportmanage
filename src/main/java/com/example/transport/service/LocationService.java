package com.example.transport.service;


import com.example.transport.dto.OverlandPayload;
import com.example.transport.model.LocationPing;
import com.example.transport.model.Truck;
import com.example.transport.repository.LocationPingRepository;
import com.example.transport.repository.TruckRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class LocationService {

    // Only reverse-geocode if the truck has moved at least this far since
    // its last known position. Keeps Nominatim calls proportional to actual
    // movement instead of ping frequency -- a truck idling at a red light
    // won't trigger a new lookup every 2 minutes.
    private static final double MIN_DISTANCE_METERS_FOR_REGEOCODE = 100.0;
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private final TruckRepository truckRepository;
    private final LocationPingRepository locationPingRepository;
    private final GeocodingService geocodingService;

    public LocationService(TruckRepository truckRepository, LocationPingRepository locationPingRepository, GeocodingService geocodingService) {
        this.truckRepository = truckRepository;
        this.locationPingRepository = locationPingRepository;
        this.geocodingService = geocodingService;
    }

    /**
     * Ingests a batch of Overland location features for a given truck.
     * If the truck doesn't exist yet, it's auto-created (handy for a POC --
     * remove this in production and require trucks to be pre-registered).
     */
    public int ingest(String truckId, OverlandPayload payload) {
        Truck truck = truckRepository.findById(truckId)
                .orElseGet(() -> truckRepository.save(new Truck(truckId, truckId, null, null)));

        int saved = 0;
        for (OverlandPayload.LocationFeature feature : payload.getLocations()) {
            if (feature.getGeometry() == null || feature.getGeometry().getCoordinates() == null
                    || feature.getGeometry().getCoordinates().size() < 2) {
                continue; // skip malformed points
            }

            double longitude = feature.getGeometry().getCoordinates().get(0);
            double latitude = feature.getGeometry().getCoordinates().get(1);

            Long epochSeconds = parseTimestamp(
                    feature.getProperties() != null ? feature.getProperties().getTimestamp() : null);

            Double speed = feature.getProperties() != null ? feature.getProperties().getSpeed() : null;
            Double accuracy = feature.getProperties() != null ? feature.getProperties().getHorizontalAccuracy() : null;
            Double battery = feature.getProperties() != null ? feature.getProperties().getBatteryLevel() : null;

            String locationName = resolveLocationName(truck, latitude, longitude);

            LocationPing ping = new LocationPing(truck, latitude, longitude, epochSeconds, speed, accuracy, battery, locationName);
            ping.setLocationName(locationName);
            locationPingRepository.save(ping);
            saved++;

            // Update the truck's cached last-known position with the freshest point
            if (truck.getLastTimestampEpochSeconds() == null
                    || (epochSeconds != null && epochSeconds > truck.getLastTimestampEpochSeconds())) {
                truck.setLastLatitude(latitude);
                truck.setLastLongitude(longitude);
                truck.setLastTimestampEpochSeconds(epochSeconds);
            }
        }
        truckRepository.save(truck);
        return saved;
    }


    /**
     * Decides whether this ping needs a fresh Nominatim lookup, or can just
     * reuse the truck's last known location name because it hasn't actually
     * moved far enough for the name to plausibly have changed.
     */
    private String resolveLocationName(Truck truck, double latitude, double longitude) {
        boolean hasPreviousPosition = truck.getLastLatitude() != null && truck.getLastLongitude() != null;

        if (hasPreviousPosition) {
            double distanceMoved = haversineDistanceMeters(
                    truck.getLastLatitude(), truck.getLastLongitude(), latitude, longitude);

//            if (distanceMoved < MIN_DISTANCE_METERS_FOR_REGEOCODE) {
//                // Hasn't moved meaningfully -- reuse the existing name, skip the API call.
//                return truck.getLastLocationName();
//            }
        }

        return geocodingService.reverseGeocode(latitude, longitude);
    }

    /**
     * Great-circle distance between two lat/lng points, in meters.
     * Standard Haversine formula -- accurate enough for "has this truck
     * moved a meaningful amount" without needing a mapping library.
     */
    private double haversineDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    public List<LocationPing> getHistory(String truckId) {
        return locationPingRepository.findByTruckIdOrderByTimestampEpochSecondsDesc(truckId);
    }

    public Truck getTruck(String truckId) {
        return truckRepository.findById(truckId).orElse(null);
    }

    private Long parseTimestamp(String isoTimestamp) {
        if (isoTimestamp == null) return Instant.now().getEpochSecond();
        try {
            return Instant.parse(isoTimestamp).getEpochSecond();
        } catch (DateTimeParseException e) {
            return Instant.now().getEpochSecond();
        }
    }

}
