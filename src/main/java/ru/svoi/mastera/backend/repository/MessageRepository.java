package ru.svoi.mastera.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.svoi.mastera.backend.entity.Message;
import ru.svoi.mastera.backend.entity.User;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    // All messages between two users, ordered by time
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :u1 AND m.receiver.id = :u2) OR " +
           "(m.sender.id = :u2 AND m.receiver.id = :u1) " +
           "ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("u1") UUID u1, @Param("u2") UUID u2);

    // All conversations for a user (latest message per partner)
    @Query(value = "SELECT DISTINCT ON (partner_id) * FROM (" +
           "  SELECT m.*, CASE WHEN m.sender_id = :userId THEN m.receiver_id ELSE m.sender_id END AS partner_id " +
           "  FROM messages m WHERE m.sender_id = :userId OR m.receiver_id = :userId" +
           ") sub ORDER BY partner_id, created_at DESC",
           nativeQuery = true)
    List<Message> findLatestPerPartner(@Param("userId") UUID userId);

    // Unread count for a user
    long countByReceiverAndIsReadFalse(User receiver);
}