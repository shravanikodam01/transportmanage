package com.example.transport.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTruckRequest {
    @NotBlank
    private String id;

    private String name;
    private String licensePlate;
}
