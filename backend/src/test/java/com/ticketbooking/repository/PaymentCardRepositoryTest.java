package com.ticketbooking.repository;

import com.ticketbooking.entity.PaymentCard;
import com.ticketbooking.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentCardRepositoryTest {

    @Autowired
    private PaymentCardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    private PaymentCard saveCard(User user, String last4, LocalDateTime createdAt) {
        PaymentCard card = new PaymentCard();
        card.setUser(user);
        card.setHolderName("TEST USER");
        card.setLast4(last4);
        card.setBrand("Visa");
        card.setExpMonth(12);
        card.setExpYear(2030);
        card.setCreatedAt(createdAt);
        return cardRepository.save(card);
    }

    @Test
    void returnsOnlyCardsOfThisUserNewestFirst() {
        User amina = userRepository.findByEmail("amina@gmail.com").orElseThrow();
        User arman = userRepository.findByEmail("arman@mail.kz").orElseThrow();
        saveCard(amina, "1111", LocalDateTime.now().minusDays(2));
        saveCard(amina, "4242", LocalDateTime.now());
        saveCard(arman, "9999", LocalDateTime.now());

        List<PaymentCard> cards = cardRepository.findByUserUserIdOrderByCreatedAtDesc(amina.getUserId());

        assertThat(cards).extracting(PaymentCard::getLast4).containsExactly("4242", "1111");
    }
}
