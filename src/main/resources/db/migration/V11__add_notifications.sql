-- V11: Notifications table
-- Дата: 2026-04-05

CREATE TABLE IF NOT EXISTS notifications (
                                             id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type        VARCHAR(50) NOT NULL,  -- NEW_OFFER, OFFER_ACCEPTED, DEAL_CONFIRMED, NEW_MESSAGE
    title       VARCHAR(255) NOT NULL,
    body        TEXT        NOT NULL,
    link        VARCHAR(500),          -- куда вести при клике
    is_read     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_notifications_user_id     ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;

COMMENT ON TABLE notifications IS 'In-app уведомления для пользователей';
COMMENT ON COLUMN notifications.type IS 'NEW_OFFER | OFFER_ACCEPTED | DEAL_CONFIRMED | NEW_MESSAGE | DEAL_COMPLETED';
COMMENT ON COLUMN notifications.link IS 'Относительный путь для навигации при клике';