package com.ticketbooking.repository;

import com.ticketbooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserUserIdOrderByBookingDateDesc(Long userId);

    List<Booking> findByStatusAndBookingDateBefore(String status, LocalDateTime time);
}
