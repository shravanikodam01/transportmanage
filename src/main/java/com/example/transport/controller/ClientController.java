package com.example.transport.controller;

import com.example.transport.dto.CreateClientRequest;
import com.example.transport.model.Client;
import com.example.transport.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ResponseEntity<Client> create(@Valid @RequestBody CreateClientRequest request) {
        return ResponseEntity.ok(clientService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<Client>> list() {
        return ResponseEntity.ok(clientService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> get(@PathVariable Long id) {
        Client client = clientService.get(id);
        if (client == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(client);
    }
}