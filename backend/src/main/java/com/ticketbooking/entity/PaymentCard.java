package com.ticketbooking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_cards")
@Getter
@Setter
@NoArgsConstructor
public class PaymentCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cardId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String holderName;

    @Column(name = "last4")
    private String last4;

    private String brand;
    private Integer expMonth;
    private Integer expYear;
    private LocalDateTime createdAt;
}
