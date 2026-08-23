-- V1__init_schema.sql
-- Adds drivers and trips on top of the existing vehicle-tracking
-- schema (trucks, location_history). Trips link to trucks.id (VARCHAR),
-- not a new vehicles table, since trucks already is the vehicle entity.

CREATE TABLE drivers (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name          VARCHAR(50) NOT NULL,
    last_name           VARCHAR(50) NOT NULL,
    license_number      VARCHAR(30) NOT NULL UNIQUE,
    phone               VARCHAR(20),
    email               VARCHAR(100) UNIQUE,
    status              VARCHAR(20) NOT NULL DEFAULT 'active'
                        CHECK (status IN ('active', 'on_leave', 'inactive')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE trips (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    truck_id            VARCHAR NOT NULL REFERENCES trucks(id),
    driver_id           UUID NOT NULL REFERENCES drivers(id),
    origin              VARCHAR(255) NOT NULL,
    destination         VARCHAR(255) NOT NULL,
    scheduled_start      TIMESTAMPTZ NOT NULL,
    scheduled_end        TIMESTAMPTZ,
    actual_start         TIMESTAMPTZ,
    actual_end           TIMESTAMPTZ,
    status              VARCHAR(20) NOT NULL DEFAULT 'scheduled'
                        CHECK (status IN ('scheduled', 'in_progress', 'completed', 'cancelled')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_trips_truck_id ON trips(truck_id);
CREATE INDEX idx_trips_driver_id ON trips(driver_id);
CREATE INDEX idx_trips_status ON trips(status);
CREATE INDEX idx_trips_scheduled_start ON trips(scheduled_start);

CREATE INDEX idx_drivers_status ON drivers(status);
