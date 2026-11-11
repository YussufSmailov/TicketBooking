package com.ticketbooking.repository;

import com.ticketbooking.entity.MatchPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchPaymentRepository extends JpaRepository<MatchPayment, Long> {

    Optional<MatchPayment> findByBookingBookingId(Long bookingId);
}
