package com.ticketbooking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
public class MovieSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    private LocalDate sessionDate;
    private LocalTime sessionTime;
    private Integer hallNumber;
}
