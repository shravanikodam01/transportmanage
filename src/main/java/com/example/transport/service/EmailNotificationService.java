package com.example.transport.service;

import com.example.transport.model.Trip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class EmailNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${app.notifications.dispatcher-email}")
    private String dispatcherEmail;

    @Value("${app.notifications.from-email:${app.notifications.dispatcher-email}}")
    private String fromEmail;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendTripArrivalNotification(Trip trip) {
        if (dispatcherEmail == null || dispatcherEmail.isBlank()) {
            log.warn("No dispatcher email configured (app.notifications.dispatcher-email) -- " +
                    "skipping arrival notification for trip {}", trip.getId());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(dispatcherEmail);
            message.setSubject("Truck " + trip.getTruck().getId() + " arrived at " + destinationLabel(trip));
            message.setText(buildBody(trip));
            mailSender.send(message);
            log.info("Sent arrival notification for trip {} to {}", trip.getId(), dispatcherEmail);
        } catch (Exception e) {
            // A mail server hiccup (or no SMTP creds configured yet) must never
            // break location ingestion or leave the trip stuck mid-update.
            log.error("Failed to send arrival notification for trip {}: {}", trip.getId(), e.getMessage());
        }
    }

    private String destinationLabel(Trip trip) {
        return trip.getDestinationName() != null ? trip.getDestinationName() : "destination";
    }

    private String buildBody(Trip trip) {
        return "Trip #" + trip.getId() + " has arrived at its destination.\n\n"
                + "Truck: " + trip.getTruck().getId() + "\n"
                + "Driver: " + (trip.getDriver() != null ? trip.getDriver().getName() : "unassigned") + "\n"
                + "Order: " + (trip.getTransportOrder() != null ? trip.getTransportOrder().getOrderNumber() : "n/a") + "\n"
                + "Destination: " + destinationLabel(trip) + "\n"
                + "Arrived at: " + Instant.ofEpochSecond(trip.getArrivedAtEpochSeconds() != null
                        ? trip.getArrivedAtEpochSeconds() : Instant.now().getEpochSecond()) + "\n";
    }
}