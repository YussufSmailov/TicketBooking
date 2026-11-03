package com.ticketbooking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "concert_bookings")
@Getter
@Setter
@NoArgsConstructor
public class ConcertBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "seat_id")
    private ConcertSeat seat;

    private LocalDateTime bookingDate;
    private String status;
}
