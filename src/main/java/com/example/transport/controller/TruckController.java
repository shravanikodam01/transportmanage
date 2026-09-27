package com.example.transport.controller;

import com.example.transport.dto.AvailableTruckResponse;
import com.example.transport.dto.CreateTruckRequest;
import com.example.transport.model.Truck;
import com.example.transport.service.TruckService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trucks")
public class TruckController {

    private final TruckService truckService;

    public TruckController(TruckService truckService) {
        this.truckService = truckService;
    }

    /**
     * Pre-registers a truck (id, name, license plate) before it's sent any GPS
     * pings yet -- e.g. so it shows up in dropdowns for trip assignment.
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateTruckRequest request) {
        try {
            return ResponseEntity.ok(truckService.create(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Truck>> list() {
        return ResponseEntity.ok(truckService.list());
    }

    /**
     * Trucks available for a given order, nearest-first to its pickup location.
     * Excludes trucks already tied to an active (PLANNED/EN_ROUTE/ARRIVED) trip.
     */
    @GetMapping("/available")
    public ResponseEntity<?> available(@RequestParam Long orderId,
                                        @RequestParam(required = false) Double maxDistanceMeters) {
        try {
            List<AvailableTruckResponse> trucks = truckService.listAvailableForOrder(orderId, maxDistanceMeters);
            return ResponseEntity.ok(trucks);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}