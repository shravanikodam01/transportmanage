package com.example.transport.repository;

import com.example.transport.model.LocationPing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationPingRepository extends JpaRepository<LocationPing, Long> {

    List<LocationPing> findByTruckIdOrderByTimestampEpochSecondsDesc(String truckId);
}
