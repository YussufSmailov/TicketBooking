package com.ticketbooking.entity;

import jakarta.persistence.*;
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
    private String description;
    private String posterUrl;
}
