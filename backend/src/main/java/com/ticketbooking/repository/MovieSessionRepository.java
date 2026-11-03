package com.ticketbooking.repository;

import com.ticketbooking.entity.MovieSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieSessionRepository extends JpaRepository<MovieSession, Long> {

    List<MovieSession> findAllByOrderBySessionDateAscSessionTimeAsc();
}
