package com.ticketbooking.repository;

import com.ticketbooking.entity.ConcertBooking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertBookingRepository extends JpaRepository<ConcertBooking, Long> {
}
