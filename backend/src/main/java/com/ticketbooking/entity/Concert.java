package com.ticketbooking.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Formula;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "concerts")
@Getter
@Setter
@NoArgsConstructor
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long concertId;

    private String artist;
    private String title;
    private String venue;
    private String city;
    private LocalDate concertDate;
    private LocalTime concertTime;
    private Integer durationMin;
    private String genre;
    private String description;
    private String posterUrl;

    @Formula("(SELECT ROUND(AVG(r.rating), 1) FROM reviews r WHERE r.event_type = 'concert' AND r.event_id = concert_id)")
    private Double rating;

    @Formula("(SELECT COUNT(*) FROM reviews r WHERE r.event_type = 'concert' AND r.event_id = concert_id)")
    private Integer reviewCount;
}
