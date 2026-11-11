package com.ticketbooking.service;

import com.ticketbooking.dto.CardRequest;
import com.ticketbooking.dto.CardResponse;
import com.ticketbooking.dto.PaymentRequest;
import com.ticketbooking.entity.PaymentCard;
import com.ticketbooking.entity.User;
import com.ticketbooking.repository.PaymentCardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CardService {

    private final PaymentCardRepository cardRepository;
    private final UserService userService;

    public CardService(PaymentCardRepository cardRepository, UserService userService) {
        this.cardRepository = cardRepository;
        this.userService = userService;
    }

    public static boolean isValidLuhn(String digits) {
        if (digits == null || !digits.matches("[0-9]{13,19}")) {
            return false;
        }
        int sum = 0;
        boolean doubleIt = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int digit = digits.charAt(i) - '0';
            if (doubleIt) {
                digit = digit * 2;
                if (digit > 9) {
                    digit = digit - 9;
                }
            }
            sum = sum + digit;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }

    public static String detectBrand(String digits) {
        if (digits.startsWith("4")) {
            return "Visa";
        }
        if (digits.startsWith("5") || digits.startsWith("2")) {
            return "Mastercard";
        }
        if (digits.startsWith("34") || digits.startsWith("37")) {
            return "Amex";
        }
        return "Card";
    }

    public static String onlyDigits(String cardNumber) {
        return cardNumber.replace(" ", "");
    }

    public void checkCard(CardRequest card) {
        String digits = onlyDigits(card.cardNumber());
        if (!isValidLuhn(digits)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный номер карты");
        }
        LocalDate today = LocalDate.now();
        boolean expired = card.expYear() < today.getYear()
                || (card.expYear() == today.getYear() && card.expMonth() < today.getMonthValue());
        if (expired) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Срок действия карты истёк");
        }
    }

    public List<CardResponse> getCards(String userEmail) {
        User user = userService.getByEmail(userEmail);
        return cardRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId())
                .stream()
                .map(CardService::toResponse)
                .toList();
    }

    public CardResponse addCard(String userEmail, CardRequest request) {
        User user = userService.getByEmail(userEmail);
        checkCard(request);
        return toResponse(saveCard(user, request));
    }

    public void deleteCard(String userEmail, Long cardId) {
        User user = userService.getByEmail(userEmail);
        PaymentCard card = getOwnCard(user, cardId);
        cardRepository.delete(card);
    }

    public String findLast4ForPayment(User user, PaymentRequest request) {
        if (request.cardId() != null) {
            return getOwnCard(user, request.cardId()).getLast4();
        }
        if (request.newCard() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Выберите карту или введите новую");
        }
        checkCard(request.newCard());
        String digits = onlyDigits(request.newCard().cardNumber());
        return digits.substring(digits.length() - 4);
    }

    public PaymentCard saveCard(User user, CardRequest request) {
        String digits = onlyDigits(request.cardNumber());
        PaymentCard card = new PaymentCard();
        card.setUser(user);
        card.setHolderName(request.holderName().trim().toUpperCase());
        card.setLast4(digits.substring(digits.length() - 4));
        card.setBrand(detectBrand(digits));
        card.setExpMonth(request.expMonth());
        card.setExpYear(request.expYear());
        card.setCreatedAt(LocalDateTime.now());
        return cardRepository.save(card);
    }

    private PaymentCard getOwnCard(User user, Long cardId) {
        PaymentCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Карта не найдена"));
        if (!card.getUser().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Это не ваша карта");
        }
        return card;
    }

    public static CardResponse toResponse(PaymentCard card) {
        String title = card.getBrand() + " •••• " + card.getLast4();
        return new CardResponse(card.getCardId(), card.getHolderName(), card.getBrand(), card.getLast4(),
                card.getExpMonth(), card.getExpYear(), title);
    }
}
