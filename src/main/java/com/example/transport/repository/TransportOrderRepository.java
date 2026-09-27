package com.example.transport.repository;

import com.example.transport.model.TransportOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransportOrderRepository extends JpaRepository<TransportOrder, Long> {
    Optional<TransportOrder> findByOrderNumber(String orderNumber);
}