package com.example.transport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTransportOrderRequest {
    @NotBlank
    private String orderNumber;

    @NotNull
    private Long clientId;

    @NotBlank
    private String originName;
    @NotNull
    private Double originLatitude;
    @NotNull
    private Double originLongitude;

    @NotBlank
    private String destinationName;
    @NotNull
    private Double destinationLatitude;
    @NotNull
    private Double destinationLongitude;

    private String cargoDescription;
    private Integer quantity;
    private Double estimatedCost;

    // ISO-8601 strings, e.g. "2026-08-30T10:00:00Z" -- optional
    private String requestedPickupAt;
    private String requestedDeliveryAt;
}