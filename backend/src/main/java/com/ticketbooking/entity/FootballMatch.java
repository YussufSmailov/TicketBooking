package com.ticketbooking.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Formula;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
public class FootballMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long matchId;

    @ManyToOne
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    @ManyToOne
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;

    private String tournament;
    private String roundName;
    private LocalDate matchDate;
    private LocalTime matchTime;
    private String description;
    private String posterUrl;

    @Formula("(SELECT ROUND(AVG(r.rating), 1) FROM reviews r WHERE r.event_type = 'match' AND r.event_id = match_id)")
    private Double rating;

    @Formula("(SELECT COUNT(*) FROM reviews r WHERE r.event_type = 'match' AND r.event_id = match_id)")
    private Integer reviewCount;
}
