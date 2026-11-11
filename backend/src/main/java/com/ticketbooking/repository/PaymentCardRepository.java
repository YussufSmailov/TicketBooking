package com.ticketbooking.repository;

import com.ticketbooking.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long> {

    List<PaymentCard> findByUserUserIdOrderByCreatedAtDesc(Long userId);
}
