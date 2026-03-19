-- Создание таблицы настроек уведомлений
CREATE TABLE IF NOT EXISTS notification_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,

    -- Способы уведомлений
    email_notifications BOOLEAN NOT NULL DEFAULT true,
    push_notifications BOOLEAN NOT NULL DEFAULT false,

    -- О чём уведомлять
    new_deals BOOLEAN NOT NULL DEFAULT true,
    deal_updates BOOLEAN NOT NULL DEFAULT true,
    messages BOOLEAN NOT NULL DEFAULT true,
    reviews BOOLEAN NOT NULL DEFAULT true,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_notification_settings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Создаём индекс для быстрого поиска по user_id
CREATE INDEX IF NOT EXISTS idx_notification_settings_user_id ON notification_settings(user_id);

-- Комментарии для документации
COMMENT ON TABLE notification_settings IS 'Настройки уведомлений пользователей';
COMMENT ON COLUMN notification_settings.email_notifications IS 'Получать Email уведомления';
COMMENT ON COLUMN notification_settings.push_notifications IS 'Получать Push уведомления';
COMMENT ON COLUMN notification_settings.new_deals IS 'Уведомлять о новых сделках';
COMMENT ON COLUMN notification_settings.deal_updates IS 'Уведомлять об обновлениях сделок';
COMMENT ON COLUMN notification_settings.messages IS 'Уведомлять о новых сообщениях';
COMMENT ON COLUMN notification_settings.reviews IS 'Уведомлять о новых отзывах';