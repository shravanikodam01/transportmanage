package com.example.transport.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The operational assignment of a truck + driver to a {@link TransportOrder}.
 * destinationLatitude/Longitude are copied from the order at assignment time
 * so arrival detection (which runs on every location ping) doesn't need to
 * join back to the order.
 */
@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transport_order_id", nullable = false)
    private TransportOrder transportOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_id", nullable = false)
    private Truck truck;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Enumerated(EnumType.STRING)
    private TripStatus status = TripStatus.PLANNED;

    private String destinationName;
    private Double destinationLatitude;
    private Double destinationLongitude;

    // Overrides the app-wide default geofence radius for this trip specifically, if set.
    private Double arrivalRadiusMeters;


    // Known once the trip is completed -- fuel, tolls, driver pay, etc.
    private Double actualCost;

    private Long startedAtEpochSeconds;
    private Long arrivedAtEpochSeconds;
    private Long completedAtEpochSeconds;

    // Set once the arrival email has actually been sent -- guards against notifying twice.
    private Long arrivalNotifiedAtEpochSeconds;
}
