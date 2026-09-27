package com.example.transport.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDriverRequest {
    @NotBlank
    private String name;
    private String phone;
    private String licenseNumber;
}