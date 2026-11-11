package com.ticketbooking.repository;

import com.ticketbooking.entity.ConcertBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ConcertBookingRepository extends JpaRepository<ConcertBooking, Long> {

    List<ConcertBooking> findByUserUserIdOrderByBookingDateDesc(Long userId);

    List<ConcertBooking> findByStatusAndBookingDateBefore(String status, LocalDateTime time);
}
