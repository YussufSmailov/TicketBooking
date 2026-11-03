package com.ticketbooking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "concert_seats")
@Getter
@Setter
@NoArgsConstructor
public class ConcertSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatId;

    @ManyToOne
    @JoinColumn(name = "concert_id")
    private Concert concert;

    private String zone;
    private String seatNumber;
    private BigDecimal price;
    private String status;
}
