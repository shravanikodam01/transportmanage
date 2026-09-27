package com.example.transport.controller;

import com.example.transport.dto.CompleteTripRequest;
import com.example.transport.dto.CreateTripRequest;
import com.example.transport.model.Trip;
import com.example.transport.service.TripService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateTripRequest request) {
        try {
            return ResponseEntity.ok(tripService.createTrip(request));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<?> start(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(tripService.startTrip(id));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable Long id,
                                       @RequestBody(required = false) CompleteTripRequest request) {
        try {
            Double actualCost = request != null ? request.getActualCost() : null;
            return ResponseEntity.ok(tripService.completeTrip(id, actualCost));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(tripService.cancelTrip(id));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Trip>> list() {
        return ResponseEntity.ok(tripService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> get(@PathVariable Long id) {
        Trip trip = tripService.get(id);
        if (trip == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(trip);
    }

    @GetMapping("/by-truck/{truckId}")
    public ResponseEntity<List<Trip>> listForTruck(@PathVariable String truckId) {
        return ResponseEntity.ok(tripService.listForTruck(truckId));
    }
}