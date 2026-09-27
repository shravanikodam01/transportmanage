package com.example.transport.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    private DriverStatus status = DriverStatus.AVAILABLE;

    public Driver(String name, String phone, String licenseNumber) {
        this.name = name;
        this.phone = phone;
        this.licenseNumber = licenseNumber;
        this.status = DriverStatus.AVAILABLE;
    }
}