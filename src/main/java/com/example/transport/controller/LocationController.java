package com.example.transport.controller;

import com.example.transport.dto.OverlandPayload;
import com.example.transport.model.LocationPing;
import com.example.transport.model.Truck;
import com.example.transport.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trucks")
public class LocationController {
    private final LocationService locationService;

    public LocationController(LocationService locationService){
        this.locationService = locationService;
    }

    /**
     * This is the URL you paste into the Overland app's "Server URL" setting:
     *   http://<your-machine-ip>:8080/api/trucks/{truckId}/locations
     *
     * Set the truckId to whatever ID you're using for this phone/truck, e.g. "truck-101".
     * Overland expects a 200 response with a small JSON body to consider the send successful.
     */
    @PostMapping("/{truckId}/locations")
    public ResponseEntity<Map<String, String>> receiveLocations(
            @PathVariable String truckId,
            @Valid @RequestBody OverlandPayload payload) {

        int saved = locationService.ingest(truckId, payload);
        return ResponseEntity.ok(Map.of("result", "ok", "saved", String.valueOf(saved)));
    }

    @GetMapping("/{truckId}/locations")
    public ResponseEntity<List<LocationPing>> getHistory(@PathVariable String truckId) {
        return ResponseEntity.ok(locationService.getHistory(truckId));
    }

    @GetMapping("/{truckId}")
    public ResponseEntity<Truck> getTruck(@PathVariable String truckId) {
        Truck truck = locationService.getTruck(truckId);
        if (truck == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(truck);
    }

    @GetMapping("/hello")
    public String demo(){
        return "hello";
    }
}
