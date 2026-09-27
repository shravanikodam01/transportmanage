package com.example.transport.repository;

import com.example.transport.model.Trip;
import com.example.transport.model.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByTruckIdOrderByIdDesc(String truckId);

    // A truck should only have one EN_ROUTE trip at a time -- this is what
    // the arrival geofence check looks up on every ingested location ping.
    Optional<Trip> findFirstByTruckIdAndStatus(String truckId, TripStatus status);

    List<Trip> findByTransportOrderIdOrderByIdDesc(Long transportOrderId);

    boolean existsByTruckIdAndStatusIn(String truckId, List<TripStatus> statuses);

    List<Trip> findByStatusIn(List<TripStatus> statuses);
}