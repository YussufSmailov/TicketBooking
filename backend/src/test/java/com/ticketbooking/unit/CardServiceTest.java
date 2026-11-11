package com.ticketbooking.unit;

import com.ticketbooking.dto.CardRequest;
import com.ticketbooking.entity.PaymentCard;
import com.ticketbooking.entity.User;
import com.ticketbooking.repository.PaymentCardRepository;
import com.ticketbooking.service.CardService;
import com.ticketbooking.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private PaymentCardRepository cardRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CardService cardService;

    @Test
    void expiredCardIsRejected() {
        int lastYear = LocalDate.now().getYear() - 1;
        CardRequest card = new CardRequest("4242 4242 4242 4242", "AMINA SEITKALI", 1, lastYear, "123");

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> cardService.checkCard(card));

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void savedCardKeepsOnlyLastFourDigitsAndBrand() {
        when(cardRepository.save(any(PaymentCard.class))).thenAnswer(call -> call.getArgument(0));
        CardRequest card = new CardRequest("4242 4242 4242 4242", "amina seitkali", 12, 2030, "123");

        PaymentCard saved = cardService.saveCard(new User(), card);

        assertThat(saved.getLast4()).isEqualTo("4242");
        assertThat(saved.getBrand()).isEqualTo("Visa");
        assertThat(saved.getHolderName()).isEqualTo("AMINA SEITKALI");
        assertThat(CardService.toResponse(saved).title()).isEqualTo("Visa •••• 4242");
    }

    @Test
    void deletingSomeoneElsesCardThrows403() {
        User owner = new User();
        owner.setUserId(1L);
        User stranger = new User();
        stranger.setUserId(2L);
        PaymentCard card = new PaymentCard();
        card.setCardId(5L);
        card.setUser(owner);
        when(userService.getByEmail("stranger@mail.kz")).thenReturn(stranger);
        when(cardRepository.findById(5L)).thenReturn(Optional.of(card));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> cardService.deleteCard("stranger@mail.kz", 5L));

        assertThat(error.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(cardRepository, never()).delete(any());
    }
}
