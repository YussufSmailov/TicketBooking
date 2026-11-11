package com.ticketbooking.repository;

import com.ticketbooking.entity.MatchBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchBookingRepository extends JpaRepository<MatchBooking, Long> {

    List<MatchBooking> findByUserUserIdOrderByBookingDateDesc(Long userId);

    List<MatchBooking> findByStatusAndBookingDateBefore(String status, LocalDateTime time);
}
