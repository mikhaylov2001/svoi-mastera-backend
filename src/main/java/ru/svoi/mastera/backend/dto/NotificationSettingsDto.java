package ru.svoi.mastera.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsDto {
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private Boolean newDeals;
    private Boolean dealUpdates;
    private Boolean messages;
    private Boolean reviews;
}