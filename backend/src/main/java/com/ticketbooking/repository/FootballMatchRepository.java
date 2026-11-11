package com.ticketbooking.repository;

import com.ticketbooking.entity.FootballMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FootballMatchRepository extends JpaRepository<FootballMatch, Long> {

    List<FootballMatch> findAllByOrderByMatchDateAscMatchTimeAsc();

    List<FootballMatch> findByMatchDateGreaterThanEqualOrderByMatchDateAscMatchTimeAsc(LocalDate date);
}
