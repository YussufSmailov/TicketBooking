package com.ticketbooking.repository;

import com.ticketbooking.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findBySessionSessionIdOrderBySeatIdAsc(Long sessionId);

    Optional<Seat> findFirstBySessionMovieMovieIdAndSessionSessionDateGreaterThanEqualOrderByPriceAsc(Long movieId, LocalDate date);

    boolean existsBySessionSessionIdAndSeatNumber(Long sessionId, String seatNumber);
}
