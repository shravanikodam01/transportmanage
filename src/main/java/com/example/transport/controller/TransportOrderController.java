package com.example.transport.controller;

import com.example.transport.dto.CreateTransportOrderRequest;
import com.example.transport.model.TransportOrder;
import com.example.transport.service.TransportOrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class TransportOrderController {

    private final TransportOrderService transportOrderService;

    public TransportOrderController(TransportOrderService transportOrderService) {
        this.transportOrderService = transportOrderService;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateTransportOrderRequest request) {
        try {
            return ResponseEntity.ok(transportOrderService.create(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<TransportOrder>> list() {
        return ResponseEntity.ok(transportOrderService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransportOrder> get(@PathVariable Long id) {
        TransportOrder order = transportOrderService.get(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }
}