package com.ticketbooking.repository;

import com.ticketbooking.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByEventTypeAndEventIdOrderByUpdatedAtDesc(String eventType, Long eventId);

    Optional<Review> findByUserUserIdAndEventTypeAndEventId(Long userId, String eventType, Long eventId);

    @Transactional
    void deleteByEventTypeAndEventId(String eventType, Long eventId);
}
