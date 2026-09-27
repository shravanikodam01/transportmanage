package com.example.transport.service;

import com.example.transport.model.Trip;

public interface NotificationService {

    /**
     * Fired once, when a trip's truck first enters the destination geofence.
     * Implementations must not throw -- a notification failure should never
     * break location ingestion or trip state.
     */
    void sendTripArrivalNotification(Trip trip);
}