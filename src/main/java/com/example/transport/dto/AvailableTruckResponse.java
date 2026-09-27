package com.example.transport.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvailableTruckResponse {
    private String truckId;
    private String name;
    private String licensePlate;
    private String lastLocationName;
    private Double lastLatitude;
    private Double lastLongitude;
    private double distanceMeters;

    public AvailableTruckResponse(String truckId, String name, String licensePlate, String lastLocationName,
                                   Double lastLatitude, Double lastLongitude, double distanceMeters) {
        this.truckId = truckId;
        this.name = name;
        this.licensePlate = licensePlate;
        this.lastLocationName = lastLocationName;
        this.lastLatitude = lastLatitude;
        this.lastLongitude = lastLongitude;
        this.distanceMeters = distanceMeters;
    }
}