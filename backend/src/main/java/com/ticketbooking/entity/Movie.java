package com.ticketbooking.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Formula;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long movieId;

    private String title;
    private Integer durationMin;
    private String ageRating;

    @OneToOne(mappedBy = "movie", cascade = CascadeType.ALL)
    private MovieDetails details;

    @Formula("(SELECT ROUND(AVG(r.rating), 1) FROM reviews r WHERE r.event_type = 'movie' AND r.event_id = movie_id)")
    private Double rating;

    @Formula("(SELECT COUNT(*) FROM reviews r WHERE r.event_type = 'movie' AND r.event_id = movie_id)")
    private Integer reviewCount;
}
