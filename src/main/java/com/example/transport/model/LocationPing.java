package com.example.transport.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "location_history")
@Getter
@Setter
@NoArgsConstructor
public class LocationPing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_id", nullable = false)
    private Truck truck;

    private double latitude;
    private double longitude;
    private String locationName;
    private Long timestampEpochSeconds;

    private Double speed;         // meters/second, nullable
    private Double horizontalAccuracy;
    private Double batteryLevel;  // 0.0 - 1.0, nullable

    public LocationPing(Truck truck, double latitude, double longitude,
                        Long timestampEpochSeconds, Double speed,
                        Double horizontalAccuracy, Double batteryLevel, String locationName) {
        this.truck = truck;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestampEpochSeconds = timestampEpochSeconds;
        this.speed = speed;
        this.horizontalAccuracy = horizontalAccuracy;
        this.batteryLevel = batteryLevel;
        this.locationName = locationName;
    }

}
