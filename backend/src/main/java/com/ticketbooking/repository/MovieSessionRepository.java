package com.ticketbooking.repository;

import com.ticketbooking.entity.MovieSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovieSessionRepository extends JpaRepository<MovieSession, Long> {

    List<MovieSession> findAllByOrderBySessionDateAscSessionTimeAsc();

    List<MovieSession> findByMovieMovieIdOrderBySessionDateAscSessionTimeAsc(Long movieId);

    Optional<MovieSession> findFirstByMovieMovieIdAndSessionDateGreaterThanEqualOrderBySessionDateAscSessionTimeAsc(Long movieId, LocalDate date);
}
