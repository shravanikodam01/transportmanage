package com.example.transport.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateClientRequest {
    @NotBlank
    private String name;
}