package com.example.transport.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Matches the JSON body the Overland iOS app POSTs to its configured
 * "Server URL". Overland sends a GeoJSON FeatureCollection-style payload:
 *
 * {
 *   "locations": [
 *     {
 *       "type": "Feature",
 *       "geometry": { "type": "Point", "coordinates": [lon, lat] },
 *       "properties": {
 *         "timestamp": "2026-07-27T10:15:00Z",
 *         "speed": 12.3,
 *         "horizontal_accuracy": 5.0,
 *         "battery_level": 0.83,
 *         "device_id": "truck-101"
 *       }
 *     }
 *   ]
 * }
 */

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OverlandPayload {
    @NotEmpty
    @Valid
    private List<LocationFeature> locations;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LocationFeature {
        private String type; // "Feature"
        private Geometry geometry;
        private Properties properties;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Geometry {
        private String type; // "Point"
        private List<Double> coordinates; // [longitude, latitude]
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {
        private String timestamp; // ISO-8601 string

        private Double speed;

        @JsonProperty("horizontal_accuracy")
        private Double horizontalAccuracy;

        @JsonProperty("battery_level")
        private Double batteryLevel;

        @JsonProperty("device_id")
        private String deviceId; // we map this to our truckId
    }
}
