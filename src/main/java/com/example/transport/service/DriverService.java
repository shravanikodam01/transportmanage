package com.example.transport.service;

import com.example.transport.dto.CreateDriverRequest;
import com.example.transport.model.Driver;
import com.example.transport.repository.DriverRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public Driver create(CreateDriverRequest request) {
        Driver driver = new Driver(request.getName(), request.getPhone(), request.getLicenseNumber());
        return driverRepository.save(driver);
    }

    public List<Driver> list() {
        return driverRepository.findAll();
    }

    public Driver get(Long id) {
        return driverRepository.findById(id).orElse(null);
    }
}
