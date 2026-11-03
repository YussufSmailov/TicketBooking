package com.ticketbooking.repository;

import com.ticketbooking.entity.MatchSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchSeatRepository extends JpaRepository<MatchSeat, Long> {

    List<MatchSeat> findByMatchMatchIdOrderBySeatIdAsc(Long matchId);
}
