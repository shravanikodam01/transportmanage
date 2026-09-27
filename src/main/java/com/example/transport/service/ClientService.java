package com.example.transport.service;

import com.example.transport.dto.CreateClientRequest;
import com.example.transport.model.Client;
import com.example.transport.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client create(CreateClientRequest request) {
        return clientRepository.save(new Client(request.getName()));
    }

    public List<Client> list() {
        return clientRepository.findAll();
    }

    public Client get(Long id) {
        return clientRepository.findById(id).orElse(null);
    }
}