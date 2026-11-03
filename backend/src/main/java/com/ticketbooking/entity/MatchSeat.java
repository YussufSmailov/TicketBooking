package com.ticketbooking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "match_seats")
@Getter
@Setter
@NoArgsConstructor
public class MatchSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatId;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private FootballMatch match;

    private String sector;
    private String seatNumber;
    private BigDecimal price;
    private String status;
}
