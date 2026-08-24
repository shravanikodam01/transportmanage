package com.example.transport.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="trucks")
@Getter
@Setter
@NoArgsConstructor
public class Truck {

    @Id
    private String id;

    private String name;
    private String licensePlate;
    private String lastLocationName;

    // Cached last-known position for quick lookups without scanning history
    private Double lastLatitude;
    private Double lastLongitude;
    private Long lastTimestampEpochSeconds;

    public Truck(String id, String name, String licensePlate, String lastLocationName) {
        this.id = id;
        this.name = name;
        this.licensePlate = licensePlate;
        this.lastLocationName = lastLocationName;
    }
}
