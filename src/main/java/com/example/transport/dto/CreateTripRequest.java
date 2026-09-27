package com.example.transport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTripRequest {
    @NotNull
    private Long transportOrderId;

    @NotBlank
    private String truckId;

    @NotNull
    private Long driverId;

    // Optional per-trip override of the app-wide default arrival geofence radius.
    private Double arrivalRadiusMeters;
}