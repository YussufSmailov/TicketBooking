package com.ticketbooking.repository;

import com.ticketbooking.entity.MatchBooking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchBookingRepository extends JpaRepository<MatchBooking, Long> {
}
