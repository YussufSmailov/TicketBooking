package com.ticketbooking.unit;

import com.ticketbooking.service.CardService;
import com.ticketbooking.service.TicketNumber;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TicketNumberTest {

    @Test
    void ticketNumberHasPrefixForEachDirection() {
        assertThat(TicketNumber.create("cinema", 12L)).isEqualTo("CIN-000012");
        assertThat(TicketNumber.create("concert", 7L)).isEqualTo("CON-000007");
        assertThat(TicketNumber.create("sport", 1234L)).isEqualTo("KPL-001234");
    }

    @Test
    void cardBrandIsDetectedByFirstDigits() {
        assertThat(CardService.detectBrand("4242424242424242")).isEqualTo("Visa");
        assertThat(CardService.detectBrand("5555555555554444")).isEqualTo("Mastercard");
        assertThat(CardService.detectBrand("378282246310005")).isEqualTo("Amex");
        assertThat(CardService.detectBrand("6011111111111117")).isEqualTo("Card");
    }
}
