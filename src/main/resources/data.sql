-- ===================================================================
-- RESET (PostgreSQL)
-- ===================================================================
DROP TABLE IF EXISTS tickets CASCADE;
DROP TABLE IF EXISTS ticket_types CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS venues CASCADE;
DROP TABLE IF EXISTS tokens CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS ticket_orders CASCADE;

-- ===================================================================
-- TABLES
-- ===================================================================

-- USERS
CREATE TABLE users (
                       id              BIGSERIAL PRIMARY KEY,
                       name            VARCHAR(200)       NOT NULL,
                       username        VARCHAR(120)       NOT NULL UNIQUE,
                       email           VARCHAR(255)       NOT NULL UNIQUE,
                       phone_number    VARCHAR(40)        NOT NULL UNIQUE,
                       password_hash   VARCHAR(255)       NOT NULL,
                       profile_picture VARCHAR(1024)      NOT NULL,
                       active          BOOLEAN            NOT NULL,
                       creation_date   TIMESTAMP          NOT NULL,
                       last_login_date TIMESTAMP          NOT NULL,
                       role            VARCHAR(40)        NOT NULL
);

-- VENUES
CREATE TABLE venues (
                        id         BIGSERIAL PRIMARY KEY,
                        name       VARCHAR(255) NOT NULL UNIQUE,
                        address    VARCHAR(512) NOT NULL,
                        seat_count INTEGER      NOT NULL
);

-- EVENTS
CREATE TABLE events (
                        id          BIGSERIAL PRIMARY KEY,
                        name        VARCHAR(255)  NOT NULL,
                        starts_at   TIMESTAMP     NOT NULL,
                        venue_id    BIGINT        NOT NULL REFERENCES venues(id),
                        description TEXT
);

-- TICKET TYPES
CREATE TABLE ticket_types (
                              id          BIGSERIAL PRIMARY KEY,
                              event_id    BIGINT        NOT NULL REFERENCES events(id),
                              name        VARCHAR(120)  NOT NULL,
                              capacity    INTEGER       NOT NULL,
                              price       NUMERIC(12,2) NOT NULL,
                              currency    VARCHAR(10)   NOT NULL DEFAULT 'EUR',
                              sales_start TIMESTAMP     NULL,
                              sales_end   TIMESTAMP     NULL,
                              active      BOOLEAN       NOT NULL DEFAULT TRUE,
                              version     BIGINT        NOT NULL DEFAULT 0,
                              CONSTRAINT uq_tickettype_event_name UNIQUE (event_id, name)
);

-- TICKET ORDERS  (NEW)
CREATE TABLE ticket_orders (
                               id                BIGSERIAL PRIMARY KEY,
                               order_number      VARCHAR(120)   NOT NULL UNIQUE,
                               purchased_at      TIMESTAMP      NOT NULL,
                               buyer_email       VARCHAR(255)   NOT NULL,
                               currency          VARCHAR(10)    NOT NULL,
                               total_amount      NUMERIC(12,2)  NOT NULL,
                               payment_intent_id VARCHAR(120)   NOT NULL UNIQUE
);
CREATE INDEX ix_order_number          ON ticket_orders(order_number);
CREATE INDEX ix_order_payment_intent  ON ticket_orders(payment_intent_id);

-- TOKENS
CREATE TABLE tokens (
                        id          BIGSERIAL PRIMARY KEY,
                        token       VARCHAR(512)  NOT NULL UNIQUE,
                        token_type  VARCHAR(40)   NOT NULL,
                        expiry_date TIMESTAMP     NOT NULL,
                        revoked     BOOLEAN       NOT NULL,
                        created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
                        updated_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
                        user_id     BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

-- TICKETS
CREATE TABLE tickets (
                         id             BIGSERIAL PRIMARY KEY,
                         description    VARCHAR(255)  NOT NULL,
                         seat           BIGINT        NOT NULL,
                         price          NUMERIC(12,2) NOT NULL,
                         date_time      TIMESTAMP     NOT NULL,
                         ticket_status  VARCHAR(40)   NOT NULL,
                         event_id       BIGINT        NOT NULL REFERENCES events(id),
                         ticket_type_id BIGINT        NOT NULL REFERENCES ticket_types(id),
                         order_id       BIGINT        NULL REFERENCES ticket_orders(id) ON DELETE SET NULL,
                         CONSTRAINT uq_tickets_event_seat UNIQUE (event_id, seat)
);

-- Indexes mirroring your annotations
CREATE INDEX ix_tickets_status ON tickets (ticket_status);
CREATE INDEX ix_tickets_type   ON tickets (ticket_type_id);
CREATE INDEX ix_tickets_order  ON tickets (order_id);

-- ===================================================================
-- SEED DATA
-- ===================================================================

-- USERS (passwords are placeholders)
INSERT INTO users (name, username, email, phone_number, password_hash, profile_picture, active, creation_date, last_login_date, role) VALUES
                                                                                                                                          ('Alice Buyer',       'alice', 'alice@example.com',  '+37060000001', '{bcrypt}$2a$10$alice', 'https://pics.example.com/a1.png', TRUE, NOW() - INTERVAL '20 days', NOW() - INTERVAL '1 day', 'BUYER'),
                                                                                                                                          ('Oscar Organizer',   'oscar', 'oscar@example.com',  '+37060000002', '{bcrypt}$2a$10$oscar', 'https://pics.example.com/o1.png', TRUE, NOW() - INTERVAL '25 days', NOW() - INTERVAL '2 days', 'ORGANIZER'),
                                                                                                                                          ('Ada Admin',         'admin', 'admin@example.com',  '+37060000003', '{bcrypt}$2a$10$admin', 'https://pics.example.com/admin.png', TRUE, NOW() - INTERVAL '100 days', NOW() - INTERVAL '1 hour', 'ADMIN');

-- VENUES
INSERT INTO venues (name, address, seat_count) VALUES
                                                   ('Vilnius Arena',     'Ozo g. 14, Vilnius',          12000),
                                                   ('Kaunas Hall',       'Karaliaus Mindaugo pr. 50, Kaunas', 4500),
                                                   ('Klaipėda Theater',  'Danės g. 19, Klaipėda',        900);

-- EVENTS
INSERT INTO events (name, starts_at, venue_id, description) VALUES
                                                                ('Rock Night',        NOW() + INTERVAL '30 days', 1, 'A loud night of classic rock.'),
                                                                ('Tech Conference',   NOW() + INTERVAL '45 days', 2, 'Talks, workshops and demos.'),
                                                                ('Classical Evening', NOW() + INTERVAL '10 days', 3, 'Orchestra and soloists.');

-- TICKET TYPES
-- Rock Night
INSERT INTO ticket_types (event_id, name, capacity, price, currency, sales_start, sales_end, active) VALUES
                                                                                                         ((SELECT id FROM events WHERE name = 'Rock Night'), 'General', 8000,  35.00, 'EUR', NOW() - INTERVAL '5 days', NULL, TRUE),
                                                                                                         ((SELECT id FROM events WHERE name = 'Rock Night'), 'VIP',      500, 120.00, 'EUR', NOW() - INTERVAL '5 days', NULL, TRUE);

-- Tech Conference
INSERT INTO ticket_types (event_id, name, capacity, price, currency, sales_start, sales_end, active) VALUES
                                                                                                         ((SELECT id FROM events WHERE name = 'Tech Conference'), 'Standard', 3000,  99.00, 'EUR', NOW() - INTERVAL '3 days', NULL, TRUE),
                                                                                                         ((SELECT id FROM events WHERE name = 'Tech Conference'), 'Workshop',  500,  49.00, 'EUR', NOW() - INTERVAL '3 days', NULL, TRUE);

-- Classical Evening
INSERT INTO ticket_types (event_id, name, capacity, price, currency, sales_start, sales_end, active) VALUES
                                                                                                         ((SELECT id FROM events WHERE name = 'Classical Evening'), 'Balcony',  400, 25.00, 'EUR', NOW() - INTERVAL '1 day', NULL, TRUE),
                                                                                                         ((SELECT id FROM events WHERE name = 'Classical Evening'), 'Parterre', 300, 55.00, 'EUR', NOW() - INTERVAL '1 day', NULL, TRUE);

-- TOKENS (examples)
INSERT INTO tokens (token, token_type, expiry_date, revoked, user_id) VALUES
                                                                          ('access-token-alice',  'ACCESS',  NOW() + INTERVAL '1 day',  FALSE, (SELECT id FROM users WHERE username='alice')),
                                                                          ('refresh-token-alice', 'REFRESH', NOW() + INTERVAL '30 days', FALSE, (SELECT id FROM users WHERE username='alice')),
                                                                          ('access-token-oscar',  'ACCESS',  NOW() + INTERVAL '1 day',  FALSE, (SELECT id FROM users WHERE username='oscar'));

-- TICKETS (seed some seats and statuses)
-- Rock Night tickets
INSERT INTO tickets (description, seat, price, date_time, ticket_status, event_id, ticket_type_id)
SELECT 'Row A Seat 1', 1, 35.00, e.starts_at, 'SOLD',      e.id, tt.id
FROM events e JOIN ticket_types tt ON tt.event_id = e.id AND tt.name = 'General'
WHERE e.name = 'Rock Night';

INSERT INTO tickets (description, seat, price, date_time, ticket_status, event_id, ticket_type_id)
SELECT 'Row A Seat 2', 2, 35.00, e.starts_at, 'RESERVED',  e.id, tt.id
FROM events e JOIN ticket_types tt ON tt.event_id = e.id AND tt.name = 'General'
WHERE e.name = 'Rock Night';

INSERT INTO tickets (description, seat, price, date_time, ticket_status, event_id, ticket_type_id)
SELECT 'VIP Seat 1', 1001, 120.00, e.starts_at, 'AVAILABLE', e.id, tt.id
FROM events e JOIN ticket_types tt ON tt.event_id = e.id AND tt.name = 'VIP'
WHERE e.name = 'Rock Night';

-- Tech Conference
INSERT INTO tickets (description, seat, price, date_time, ticket_status, event_id, ticket_type_id)
SELECT 'STD Seat 10', 10, 99.00, e.starts_at, 'SOLD', e.id, tt.id
FROM events e JOIN ticket_types tt ON tt.event_id = e.id AND tt.name = 'Standard'
WHERE e.name = 'Tech Conference';

-- Classical Evening
INSERT INTO tickets (description, seat, price, date_time, ticket_status, event_id, ticket_type_id)
SELECT 'Balcony B-12', 212, 25.00, e.starts_at, 'AVAILABLE', e.id, tt.id
FROM events e JOIN ticket_types tt ON tt.event_id = e.id AND tt.name = 'Balcony'
WHERE e.name = 'Classical Evening';

-- ===================================================================
-- ORDERS (NEW) + link SOLD tickets to orders
-- ===================================================================

-- Order #1: Alice buys Rock Night - General (Row A Seat 1)
INSERT INTO ticket_orders (order_number, purchased_at, buyer_email, currency, total_amount, payment_intent_id)
VALUES ('ORD-ALICE-0001', NOW() - INTERVAL '1 day', 'alice@example.com', 'EUR', 35.00, 'pi_test_0001');

UPDATE tickets t
SET order_id = (SELECT id FROM ticket_orders WHERE order_number = 'ORD-ALICE-0001')
WHERE t.description = 'Row A Seat 1'
  AND t.ticket_status = 'SOLD';

-- Order #2: Oscar buys Tech Conference - Standard (STD Seat 10)
INSERT INTO ticket_orders (order_number, purchased_at, buyer_email, currency, total_amount, payment_intent_id)
VALUES ('ORD-OSCAR-0001', NOW() - INTERVAL '12 hours', 'oscar@example.com', 'EUR', 99.00, 'pi_test_0002');

UPDATE tickets t
SET order_id = (SELECT id FROM ticket_orders WHERE order_number = 'ORD-OSCAR-0001')
WHERE t.description = 'STD Seat 10'
  AND t.ticket_status = 'SOLD';

-- ===================================================================
-- OPTIONAL: enum safety (check constraints)
-- Uncomment to mirror your enums at DB level
-- ALTER TABLE tickets ADD CONSTRAINT chk_ticket_status
--   CHECK (ticket_status IN ('AVAILABLE','RESERVED','SOLD','CANCELED'));
-- ALTER TABLE tokens ADD CONSTRAINT chk_token_type
--   CHECK (token_type IN ('ACCESS','REFRESH','VERIFY','RESET'));
-- ALTER TABLE users  ADD CONSTRAINT chk_role
--   CHECK (role IN ('BUYER','ORGANIZER','ORGANIZER_PENDING','ADMIN'));
-- ===================================================================
