package com.example.transport.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeocodingService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Reverse-geocodes a lat/lng pair into a short, human-readable place name
     * using OpenStreetMap's Nominatim service (free, no API key required).
     *
     * Nominatim's usage policy requires a descriptive User-Agent and asks for
     * no more than 1 request/second -- fine for a POC's ingestion rate, but
     * don't call this in a tight loop or for bulk backfills.
     */
    public String reverseGeocode(double latitude, double longitude) {
        try {
            String url = String.format(
                    "https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f&zoom=14&addressdetails=1",
                    latitude, longitude);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "truck-tracker-poc/1.0 (learning project)");
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, request, String.class).getBody();
            JsonNode root = objectMapper.readTree(response);

            if (root.has("display_name")) {
                return shortenDisplayName(root);
            }
            return null;
        } catch (Exception e) {
            // Don't let a geocoding hiccup break ingestion -- just skip the name.
            return null;
        }
    }

    /**
     * Nominatim's full display_name is verbose (full postal address).
     * Build a shorter "City, State" style name from the address components
     * when available, falling back to the full display_name otherwise.
     */
    private String shortenDisplayName(JsonNode root) {
        JsonNode address = root.get("address");
        if (address != null) {
            String city = firstNonNull(address, "city", "town", "village", "hamlet");
            String state = firstNonNull(address, "state");
            if (city != null && state != null) {
                return city + ", " + state;
            }
            if (city != null) {
                return city;
            }
        }
        return root.get("display_name").asText();
    }

    private String firstNonNull(JsonNode address, String... keys) {
        for (String key : keys) {
            if (address.has(key)) {
                return address.get(key).asText();
            }
        }
        return null;
    }
}