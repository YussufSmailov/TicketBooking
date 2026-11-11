package com.ticketbooking.repository;

import com.ticketbooking.entity.FootballMatch;
import com.ticketbooking.entity.MatchSeat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SeatRepositoryTest {

    @Autowired
    private MatchSeatRepository matchSeatRepository;

    @Autowired
    private FootballMatchRepository matchRepository;

    @Test
    void almatyStadiumSeatsUseSectorsFromTheMap() {
        FootballMatch kairatHome = matchRepository.findAllByOrderByMatchDateAscMatchTimeAsc().stream()
                .filter(match -> match.getHomeTeam().getStadium().equals("Central Stadium Almaty"))
                .findFirst()
                .orElseThrow();

        List<String> sectors = matchSeatRepository.findByMatchMatchIdOrderBySeatIdAsc(kairatHome.getMatchId())
                .stream()
                .map(MatchSeat::getSector)
                .distinct()
                .toList();

        assertThat(sectors).containsExactlyInAnyOrder("VIP", "Западная", "Восточная", "Северная", "Южная");
    }
}
