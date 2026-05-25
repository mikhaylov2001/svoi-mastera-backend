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

    private static String dealLink(UUID dealId) {
        return dealId != null ? "/deals?dealId=" + dealId : "/deals";
    }

    private static String jobOffersLink(UUID jobRequestId) {
        return jobRequestId != null
                ? "/my-requests?request=" + jobRequestId + "&offers=1"
                : "/my-requests";
    }

    private static String chatLink(UUID partnerUserId) {
        return partnerUserId != null ? "/chat/" + partnerUserId : "/chat";
    }

    /** Мастер откликнулся → уведомление заказчику */
    public void notifyNewOffer(UUID customerUserId, String workerName, String jobTitle, UUID jobRequestId) {
        create(
                customerUserId,
                "NEW_OFFER",
                "Новый отклик на заявку",
                workerName + " откликнулся на вашу заявку «" + jobTitle + "»",
                jobOffersLink(jobRequestId)
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
                dealLink(dealId)
        );
    }

    /** Одна сторона подтвердила выполнение → уведомление другой стороне */
    public void notifyDealConfirmed(UUID targetUserId, String confirmerName, String jobTitle, UUID dealId) {
        create(
                targetUserId,
                "DEAL_CONFIRMED",
                "Работа подтверждена",
                confirmerName + " подтвердил выполнение работы по заявке «" + jobTitle + "». Подтвердите и вы для завершения сделки.",
                dealLink(dealId)
        );
    }

    /** Сделка полностью завершена → уведомление обеим сторонам */
    public void notifyDealCompleted(UUID customerUserId, UUID workerUserId,
                                    String customerName, String workerName, String jobTitle, UUID dealId) {
        create(
                customerUserId,
                "DEAL_COMPLETED",
                "Сделка завершена ✅",
                "Работа по заявке «" + jobTitle + "» успешно завершена. Не забудьте оставить отзыв мастеру " + workerName + "!",
                dealLink(dealId)
        );
        create(
                workerUserId,
                "DEAL_COMPLETED",
                "Сделка завершена ✅",
                "Работа по заявке «" + jobTitle + "» успешно завершена. Заказчик: " + customerName + ". Отличная работа!",
                dealLink(dealId)
        );
    }

    /** Заказчик отправил заявку по объявлению → уведомление мастеру */
    public void notifyDealNew(UUID workerUserId, String customerName, String jobTitle, UUID dealId) {
        create(workerUserId, "DEAL_NEW",
                "Новый заказ! 🔔",
                customerName + " хочет заказать «" + jobTitle + "». Примите или отклоните заявку.",
                dealLink(dealId));
    }

    /** Мастер принял заявку → уведомление заказчику */
    public void notifyDealStarted(UUID customerUserId, String workerName, String jobTitle, UUID dealId) {
        create(customerUserId, "DEAL_STARTED",
                "Мастер принял заказ ✅",
                workerName + " принял ваш заказ «" + jobTitle + "» и готов приступить к работе!",
                dealLink(dealId));
    }

    /**
     * Сделка отменена → уведомление обеим сторонам (с причиной и подсказкой написать в чат).
     *
     * @param wasInProgress {@code true} — отмена при активной работе (IN_PROGRESS),
     *                        {@code false} — отмена на этапе ожидания (NEW)
     */
    public void notifyDealCancelled(UUID cancellerUserId, UUID otherUserId,
                                    String cancellerName, String jobTitle, boolean cancellerIsCustomer,
                                    String cancellationReason, boolean wasInProgress, UUID dealId) {
        String reason = (cancellationReason == null || cancellationReason.isBlank())
                ? "не указана"
                : cancellationReason.trim();
        if (reason.length() > 500) {
            reason = reason.substring(0, 497) + "…";
        }

        String chatHintOther = "\n\n💬 Нужны уточнения? Напишите собеседнику в разделе «Сообщения» — так проще договориться о деталях.";
        String chatHintSelf = "\n\n💬 При необходимости можно кратко пояснить ситуацию в «Сообщениях».";

        String titleSelf = wasInProgress ? "Сделка отменена" : "Заявка отменена";
        String bodySelf = (wasInProgress
                ? ("Вы отменили сделку «" + jobTitle + "», которая уже была в работе.\n\nПричина: " + reason + ".")
                : ("Вы отменили заявку «" + jobTitle + "».\n\nПричина: " + reason + ".")) + chatHintSelf;

        String role = cancellerIsCustomer ? "Заказчик" : "Мастер";
        String titleOther = wasInProgress
                ? (role + " отменил сделку ❌")
                : (role + " отменил заявку ❌");
        String bodyOther = wasInProgress
                ? (role + " " + cancellerName + " отменил(а) сделку «" + jobTitle
                    + "», которая уже шла в работе.\n\nУказанная причина: " + reason + "." + chatHintOther)
                : (role + " " + cancellerName + " отменил(а) заявку по «" + jobTitle
                    + "».\n\nУказанная причина: " + reason + "." + chatHintOther);

        create(cancellerUserId, "DEAL_CANCELLED", titleSelf, bodySelf, dealLink(dealId));
        // Вторая сторона — сразу в чат, чтобы было проще написать
        create(otherUserId, "DEAL_CANCELLED", titleOther, bodyOther, "/chat");
    }

    /** Новое сообщение → уведомление получателю */
    public void notifyNewMessage(UUID receiverUserId, UUID senderUserId, String senderName, String preview) {
        String shortPreview = preview != null && preview.length() > 60
                ? preview.substring(0, 60) + "…" : preview;
        create(
                receiverUserId,
                "NEW_MESSAGE",
                "Новое сообщение",
                (senderName != null ? senderName + ": " : "") + (shortPreview != null ? shortPreview : "Вам пришло новое сообщение"),
                chatLink(senderUserId)
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