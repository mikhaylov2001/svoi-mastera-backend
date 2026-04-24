package ru.svoi.mastera.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.svoi.mastera.backend.dto.NotificationDto;
import ru.svoi.mastera.backend.entity.Notification;
import ru.svoi.mastera.backend.entity.User;
import ru.svoi.mastera.backend.repository.NotificationRepository;
import ru.svoi.mastera.backend.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ── Создать уведомление ──
    @Transactional
    public void create(UUID userId, String type, String title, String body, String link) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        n.setLink(link);
        n.setRead(false);
        notificationRepository.save(n);
    }

    // ── Получить все уведомления пользователя ──
    @Transactional(readOnly = true)
    public List<NotificationDto> getAll(UUID userId) {
        return notificationRepository.findAllByUserId(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── Количество непрочитанных ──
    @Transactional(readOnly = true)
    public long countUnread(UUID userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    // ── Отметить одно как прочитанное ──
    @Transactional
    public void markRead(UUID notificationId) {
        notificationRepository.markReadById(notificationId);
    }

    // ── Отметить все как прочитанные ──
    @Transactional
    public void markAllRead(UUID userId) {
        notificationRepository.markAllReadByUserId(userId);
    }

    // ── Хелперы для создания конкретных уведомлений ──

    /** Мастер откликнулся → уведомление заказчику */
    public void notifyNewOffer(UUID customerUserId, String workerName, String jobTitle, UUID jobRequestId) {
        create(
                customerUserId,
                "NEW_OFFER",
                "Новый отклик на заявку",
                workerName + " откликнулся на вашу заявку «" + jobTitle + "»",
                "/deals"
        );
    }

    /** Заказчик принял оффер → уведомление мастеру */
    public void notifyOfferAccepted(UUID workerUserId, String customerName, String customerLastName,
                                    String jobTitle, String agreedPrice, UUID dealId) {
        String fullName = customerLastName != null ? customerName + " " + customerLastName : customerName;
        create(
                workerUserId,
                "OFFER_ACCEPTED",
                "Ваш отклик принят! 🎉",
                fullName + " принял ваш отклик на «" + jobTitle + "». Договорная цена: " + agreedPrice + " ₽. Можно начинать работу!",
                "/deals"
        );
    }

    /** Одна сторона подтвердила выполнение → уведомление другой стороне */
    public void notifyDealConfirmed(UUID targetUserId, String confirmerName, String jobTitle, UUID dealId) {
        create(
                targetUserId,
                "DEAL_CONFIRMED",
                "Работа подтверждена",
                confirmerName + " подтвердил выполнение работы по заявке «" + jobTitle + "». Подтвердите и вы для завершения сделки.",
                "/deals"
        );
    }

    /** Сделка полностью завершена → уведомление обеим сторонам */
    public void notifyDealCompleted(UUID customerUserId, UUID workerUserId,
                                    String customerName, String workerName, String jobTitle) {
        create(
                customerUserId,
                "DEAL_COMPLETED",
                "Сделка завершена ✅",
                "Работа по заявке «" + jobTitle + "» успешно завершена. Не забудьте оставить отзыв мастеру " + workerName + "!",
                "/deals"
        );
        create(
                workerUserId,
                "DEAL_COMPLETED",
                "Сделка завершена ✅",
                "Работа по заявке «" + jobTitle + "» успешно завершена. Заказчик: " + customerName + ". Отличная работа!",
                "/deals"
        );
    }

    /** Заказчик отправил заявку по объявлению → уведомление мастеру */
    public void notifyDealNew(UUID workerUserId, String customerName, String jobTitle, UUID dealId) {
        create(workerUserId, "DEAL_NEW",
                "Новый заказ! 🔔",
                customerName + " хочет заказать «" + jobTitle + "». Примите или отклоните заявку.",
                "/deals");
    }

    /** Мастер принял заявку → уведомление заказчику */
    public void notifyDealStarted(UUID customerUserId, String workerName, String jobTitle, UUID dealId) {
        create(customerUserId, "DEAL_STARTED",
                "Мастер принял заказ ✅",
                workerName + " принял ваш заказ «" + jobTitle + "» и готов приступить к работе!",
                "/deals");
    }

    /** Сделка отменена → уведомление обеим сторонам */
    public void notifyDealCancelled(UUID cancellerUserId, UUID otherUserId,
                                    String cancellerName, String jobTitle, boolean cancellerIsCustomer) {
        // Уведомление отменившему
        create(cancellerUserId, "DEAL_CANCELLED",
                "Заявка отменена",
                "Вы отменили заявку «" + jobTitle + "».",
                "/deals");
        // Уведомление другой стороне
        String role = cancellerIsCustomer ? "Заказчик" : "Мастер";
        create(otherUserId, "DEAL_CANCELLED",
                role + " отменил заявку ❌",
                cancellerName + " отменил заявку «" + jobTitle + "».",
                "/deals");
    }

    /** Новое сообщение → уведомление получателю */
    public void notifyNewMessage(UUID receiverUserId, String senderName, String preview) {
        String shortPreview = preview != null && preview.length() > 60
                ? preview.substring(0, 60) + "…" : preview;
        create(
                receiverUserId,
                "NEW_MESSAGE",
                "Новое сообщение от " + senderName,
                shortPreview != null ? shortPreview : "Вам пришло новое сообщение",
                "/chat"
        );
    }

    private NotificationDto toDto(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getBody(),
                n.getLink(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}