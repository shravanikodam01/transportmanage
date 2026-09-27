package com.example.transport.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteTripRequest {
    // Optional -- fuel, tolls, driver pay, etc., once known.
    private Double actualCost;
}