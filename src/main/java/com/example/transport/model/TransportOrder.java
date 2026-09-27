package com.example.transport.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * The customer-facing ask: move cargo from an origin to a destination by
 * some requested date. Kept separate from {@link Trip} so that if a truck
 * breaks down and the order needs to be reassigned, the order (and its
 * history) survives across multiple trip attempts.
 */
@Entity
@Table(name = "transport_orders")
@Getter
@Setter
@NoArgsConstructor
public class TransportOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    private String originName;
    private Double originLatitude;
    private Double originLongitude;

    private String destinationName;
    private Double destinationLatitude;
    private Double destinationLongitude;

    private String cargoDescription;
    private Integer quantity;
    private Double estimatedCost;

    private Long requestedPickupAtEpochSeconds;
    private Long requestedDeliveryAtEpochSeconds;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    private Long createdAtEpochSeconds;
}