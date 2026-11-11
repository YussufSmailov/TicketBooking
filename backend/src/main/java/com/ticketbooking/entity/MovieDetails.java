package com.ticketbooking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "movie_details")
@Getter
@Setter
@NoArgsConstructor
public class MovieDetails {

    @Id
    private Long movieId;

    @JsonIgnore
    @OneToOne
    @MapsId
    @JoinColumn(name = "movie_id")
    private Movie movie;

    private String description;
    private String posterUrl;
    private String genre;
    private LocalDate releaseDate;

    @Column(name = "is_premiere")
    private boolean premiere;
}
