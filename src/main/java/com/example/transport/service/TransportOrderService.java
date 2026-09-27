package com.example.transport.service;

import com.example.transport.dto.CreateTransportOrderRequest;
import com.example.transport.model.Client;
import com.example.transport.model.OrderStatus;
import com.example.transport.model.TransportOrder;
import com.example.transport.repository.ClientRepository;
import com.example.transport.repository.TransportOrderRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class TransportOrderService {

    private final TransportOrderRepository transportOrderRepository;
    private final ClientRepository clientRepository;

    public TransportOrderService(TransportOrderRepository transportOrderRepository, ClientRepository clientRepository) {
        this.transportOrderRepository = transportOrderRepository;
        this.clientRepository = clientRepository;
    }

    public TransportOrder create(CreateTransportOrderRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("No client with id " + request.getClientId()));

        TransportOrder order = new TransportOrder();
        order.setOrderNumber(request.getOrderNumber());
        order.setClient(client);
        order.setOriginName(request.getOriginName());
        order.setOriginLatitude(request.getOriginLatitude());
        order.setOriginLongitude(request.getOriginLongitude());
        order.setDestinationName(request.getDestinationName());
        order.setDestinationLatitude(request.getDestinationLatitude());
        order.setDestinationLongitude(request.getDestinationLongitude());
        order.setCargoDescription(request.getCargoDescription());
        order.setQuantity(request.getQuantity());
        order.setEstimatedCost(request.getEstimatedCost());
        order.setRequestedPickupAtEpochSeconds(parseTimestamp(request.getRequestedPickupAt()));
        order.setRequestedDeliveryAtEpochSeconds(parseTimestamp(request.getRequestedDeliveryAt()));
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAtEpochSeconds(Instant.now().getEpochSecond());
        return transportOrderRepository.save(order);
    }

    public List<TransportOrder> list() {
        return transportOrderRepository.findAll();
    }

    public TransportOrder get(Long id) {
        return transportOrderRepository.findById(id).orElse(null);
    }

    private Long parseTimestamp(String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.isBlank()) return null;
        try {
            return Instant.parse(isoTimestamp).getEpochSecond();
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
