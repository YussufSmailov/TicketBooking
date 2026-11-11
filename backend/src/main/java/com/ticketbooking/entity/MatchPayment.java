package com.ticketbooking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_payments")
@Getter
@Setter
@NoArgsConstructor
public class MatchPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @OneToOne
    @JoinColumn(name = "booking_id")
    private MatchBooking booking;

    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String status;
}
