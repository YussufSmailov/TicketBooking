package com.ticketbooking.repository;

import com.ticketbooking.entity.MatchSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchSeatRepository extends JpaRepository<MatchSeat, Long> {

    List<MatchSeat> findByMatchMatchIdOrderBySeatIdAsc(Long matchId);

    Optional<MatchSeat> findFirstByMatchMatchIdOrderByPriceAsc(Long matchId);

    boolean existsByMatchMatchIdAndSectorAndSeatNumber(Long matchId, String sector, String seatNumber);
}
