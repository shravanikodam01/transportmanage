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

    private final TruckRepository truckRepository;
    private final LocationPingRepository locationPingRepository;

    public LocationService(TruckRepository truckRepository, LocationPingRepository locationPingRepository) {
        this.truckRepository = truckRepository;
        this.locationPingRepository = locationPingRepository;
    }

    /**
     * Ingests a batch of Overland location features for a given truck.
     * If the truck doesn't exist yet, it's auto-created (handy for a POC --
     * remove this in production and require trucks to be pre-registered).
     */
    public int ingest(String truckId, OverlandPayload payload) {
        Truck truck = truckRepository.findById(truckId)
                .orElseGet(() -> truckRepository.save(new Truck(truckId, truckId, null)));

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

            LocationPing ping = new LocationPing(truck, latitude, longitude, epochSeconds, speed, accuracy, battery);
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
