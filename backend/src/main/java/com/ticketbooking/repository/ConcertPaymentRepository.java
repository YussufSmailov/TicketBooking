package com.ticketbooking.repository;

import com.ticketbooking.entity.ConcertPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConcertPaymentRepository extends JpaRepository<ConcertPayment, Long> {

    Optional<ConcertPayment> findByBookingBookingId(Long bookingId);
}
