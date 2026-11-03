package com.ticketbooking.repository;

import com.ticketbooking.entity.ConcertSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcertSeatRepository extends JpaRepository<ConcertSeat, Long> {

    List<ConcertSeat> findByConcertConcertIdOrderBySeatIdAsc(Long concertId);
}
