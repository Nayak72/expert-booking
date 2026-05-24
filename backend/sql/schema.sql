-- =====================================================
-- ExpertConnect PostgreSQL Schema
-- Hosted on Supabase
-- =====================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ─────────────────────────────────────────────────────
-- Table: users
-- ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20) NOT NULL DEFAULT 'user'
                        CHECK (role IN ('user', 'expert')),
    profile_image   TEXT,
    fcm_token       VARCHAR(512),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- ─────────────────────────────────────────────────────
-- Table: experts
-- ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS experts (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    bio             TEXT,
    expertise       VARCHAR(255) NOT NULL,
    skills          TEXT,
    experience      INTEGER NOT NULL DEFAULT 0,
    languages       VARCHAR(255) DEFAULT 'English',
    pricing         DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    profile_image   TEXT,
    categories      VARCHAR(512),
    availability    JSONB DEFAULT '{}',
    average_rating  DECIMAL(3, 2) DEFAULT 0.00,
    total_reviews   INTEGER DEFAULT 0,
    total_bookings  INTEGER DEFAULT 0,
    is_available    SMALLINT DEFAULT 1 CHECK (is_available IN (0, 1)),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_experts_user_id   ON experts(user_id);
CREATE INDEX IF NOT EXISTS idx_experts_expertise ON experts(expertise);
CREATE INDEX IF NOT EXISTS idx_experts_rating    ON experts(average_rating DESC);
CREATE INDEX IF NOT EXISTS idx_experts_pricing   ON experts(pricing);

-- ─────────────────────────────────────────────────────
-- Table: bookings
-- ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bookings (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id                 UUID NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    expert_id               UUID NOT NULL REFERENCES experts(id) ON DELETE CASCADE,
    booking_date            DATE NOT NULL,
    slot                    VARCHAR(50) NOT NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT 'pending'
                                CHECK (status IN ('pending', 'confirmed', 'cancelled', 'completed')),
    notes                   TEXT,
    meeting_link            VARCHAR(512),
    cancellation_reason     TEXT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Prevent double-booking: same expert/date/slot/active-status
    CONSTRAINT uq_booking_slot UNIQUE (expert_id, booking_date, slot, status)
);

CREATE INDEX IF NOT EXISTS idx_bookings_user_id   ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_expert_id ON bookings(expert_id);
CREATE INDEX IF NOT EXISTS idx_bookings_date      ON bookings(booking_date);
CREATE INDEX IF NOT EXISTS idx_bookings_status    ON bookings(status);

-- ─────────────────────────────────────────────────────
-- Table: reviews
-- ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS reviews (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    expert_id       UUID NOT NULL REFERENCES experts(id) ON DELETE CASCADE,
    rating          DECIMAL(2, 1) NOT NULL CHECK (rating >= 1.0 AND rating <= 5.0),
    review_text     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- One review per user-expert pair
    CONSTRAINT uq_user_expert_review UNIQUE (user_id, expert_id)
);

CREATE INDEX IF NOT EXISTS idx_reviews_expert_id ON reviews(expert_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user_id   ON reviews(user_id);

-- ─────────────────────────────────────────────────────
-- Table: favorites
-- ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS favorites (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    expert_id   UUID NOT NULL REFERENCES experts(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- One favorite per user-expert pair
    CONSTRAINT uq_user_expert_favorite UNIQUE (user_id, expert_id)
);

CREATE INDEX IF NOT EXISTS idx_favorites_user_id ON favorites(user_id);

-- ─────────────────────────────────────────────────────
-- Function: auto-update updated_at timestamp
-- ─────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trigger_bookings_updated_at
    BEFORE UPDATE ON bookings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trigger_reviews_updated_at
    BEFORE UPDATE ON reviews
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();
