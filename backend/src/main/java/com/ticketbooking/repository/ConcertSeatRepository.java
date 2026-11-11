package com.ticketbooking.repository;

import com.ticketbooking.entity.ConcertSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConcertSeatRepository extends JpaRepository<ConcertSeat, Long> {

    List<ConcertSeat> findByConcertConcertIdOrderBySeatIdAsc(Long concertId);

    Optional<ConcertSeat> findFirstByConcertConcertIdOrderByPriceAsc(Long concertId);

    boolean existsByConcertConcertIdAndZoneAndSeatNumber(Long concertId, String zone, String seatNumber);
}
