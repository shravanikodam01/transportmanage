package com.example.transport.controller;

import com.example.transport.dto.CreateDriverRequest;
import com.example.transport.model.Driver;
import com.example.transport.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping
    public ResponseEntity<Driver> create(@Valid @RequestBody CreateDriverRequest request) {
        return ResponseEntity.ok(driverService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<Driver>> list() {
        return ResponseEntity.ok(driverService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Driver> get(@PathVariable Long id) {
        Driver driver = driverService.get(id);
        if (driver == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(driver);
    }
}