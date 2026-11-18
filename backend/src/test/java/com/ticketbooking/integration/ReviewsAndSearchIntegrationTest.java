package com.ticketbooking.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.ticketbooking.entity.MovieSession;
import com.ticketbooking.repository.MovieSessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewsAndSearchIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MovieSessionRepository sessionRepository;

    private JsonNode getJson(MockHttpServletRequestBuilder request) throws Exception {
        MvcResult result = mockMvc.perform(request).andExpect(status().isOk()).andReturn();
        return readJson(result);
    }

    private List<JsonNode> list(JsonNode array) {
        List<JsonNode> items = new ArrayList<>();
        array.forEach(items::add);
        return items;
    }

    private JsonNode movieByTitle(String title) throws Exception {
        return list(getJson(get("/api/cinema/movies"))).stream()
                .filter(movie -> movie.get("title").asText().equals(title))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void movieListIncludesAverageRatingFromReviews() throws Exception {
        JsonNode odyssey = movieByTitle("Одиссея");

        assertThat(odyssey.get("reviewCount").asInt()).isEqualTo(3);
        assertThat(odyssey.get("rating").asDouble()).isEqualTo(4.7);
        assertThat(odyssey.get("details").get("posterUrl").asText()).isEqualTo("/images/movies/the-odyssey.jpg");
    }

    @Test
    void moviesFilterByDurationAndSortByLength() throws Exception {
        List<JsonNode> movies = list(getJson(get("/api/cinema/movies")
                .param("minDuration", "120").param("maxDuration", "150").param("sort", "duration")));

        assertThat(movies).isNotEmpty();
        List<Integer> durations = movies.stream().map(movie -> movie.get("durationMin").asInt()).toList();
        assertThat(durations).allMatch(minutes -> minutes >= 120 && minutes <= 150);
        assertThat(durations).isSorted();
    }

    @Test
    void moviesFilterByMinimumRating() throws Exception {
        List<JsonNode> movies = list(getJson(get("/api/cinema/movies").param("minRating", "4.5")));

        assertThat(movies).isNotEmpty();
        assertThat(movies).allMatch(movie -> !movie.get("rating").isNull() && movie.get("rating").asDouble() >= 4.5);
    }

    @Test
    void moviesFilterByGenreAndText() throws Exception {
        List<JsonNode> cartoons = list(getJson(get("/api/cinema/movies").param("genre", "мультфильм")));
        assertThat(cartoons).extracting(movie -> movie.get("details").get("genre").asText()).containsOnly("Мультфильм");

        List<JsonNode> dune = list(getJson(get("/api/cinema/movies").param("q", "ДЮНА")));
        assertThat(dune).extracting(movie -> movie.get("title").asText()).containsExactly("Дюна: Часть третья");
    }

    @Test
    void moviesFilterByDateShowsOnlyMoviesWithSessionsThatDay() throws Exception {
        LocalDate day = LocalDate.now().plusDays(2);
        Set<Long> expected = sessionRepository.findBySessionDate(day).stream()
                .map(MovieSession::getMovie)
                .map(movie -> movie.getMovieId())
                .collect(Collectors.toSet());

        List<JsonNode> movies = list(getJson(get("/api/cinema/movies").param("date", day.toString())));

        assertThat(movies.stream().map(movie -> movie.get("movieId").asLong()).collect(Collectors.toSet()))
                .isEqualTo(expected);
    }

    @Test
    void badFilterValuesReturn400() throws Exception {
        mockMvc.perform(get("/api/cinema/movies").param("sort", "price")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/cinema/movies").param("date", "tomorrow")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/concert/concerts").param("minDuration", "long")).andExpect(status().isBadRequest());
    }

    @Test
    void concertsFilterByCityDatesAndDuration() throws Exception {
        LocalDate from = LocalDate.now().plusDays(10);
        LocalDate to = LocalDate.now().plusDays(30);
        List<JsonNode> concerts = list(getJson(get("/api/concert/concerts")
                .param("city", "Almaty").param("dateFrom", from.toString()).param("dateTo", to.toString())
                .param("maxDuration", "150")));

        assertThat(concerts).isNotEmpty();
        for (JsonNode concert : concerts) {
            LocalDate date = LocalDate.parse(concert.get("concertDate").asText());
            assertThat(concert.get("city").asText()).isEqualTo("Almaty");
            assertThat(date).isBetween(from, to);
            assertThat(concert.get("durationMin").asInt()).isLessThanOrEqualTo(150);
        }
    }

    @Test
    void matchesFilterByTeamAndRussianName() throws Exception {
        List<JsonNode> kairat = list(getJson(get("/api/sport/matches").param("teamId", "1")));
        assertThat(kairat).isNotEmpty();
        assertThat(kairat).allMatch(match -> match.get("homeTeam").get("teamId").asLong() == 1
                || match.get("awayTeam").get("teamId").asLong() == 1);
        assertThat(kairat.get(0).get("homeTeam").get("logoUrl").asText()).startsWith("/images/teams/");

        List<JsonNode> derby = list(getJson(get("/api/sport/matches").param("q", "кайрат астана")));
        assertThat(derby).hasSize(1);
        assertThat(derby.get(0).get("homeTeam").get("name").asText()).isEqualTo("Kairat");
    }

    @Test
    void globalSearchFindsAllTypes() throws Exception {
        List<JsonNode> concert = list(getJson(get("/api/search").param("q", "скриптонит")));
        assertThat(concert).extracting(card -> card.get("type").asText()).containsExactly("concert");

        List<JsonNode> kairat = list(getJson(get("/api/search").param("q", "Kairat")));
        assertThat(kairat).extracting(card -> card.get("title").asText()).contains("Kairat Nurtas", "Kairat — Astana");
        JsonNode derby = kairat.stream().filter(card -> card.get("type").asText().equals("match")).findFirst().orElseThrow();
        assertThat(derby.get("homeLogoUrl").asText()).startsWith("/images/teams/");

        List<JsonNode> mixed = list(getJson(get("/api/search").param("q", "Astana")));
        assertThat(mixed).extracting(card -> card.get("type").asText()).contains("concert", "match");

        mockMvc.perform(get("/api/search").param("q", "a")).andExpect(status().isBadRequest());
    }

    @Test
    void userCanRateUpdateAndDeleteOwnReview() throws Exception {
        String token = registerAndLogin("Review Tester");
        Long movieId = movieByTitle("Майкл").get("movieId").asLong();
        int countBefore = getJson(get("/api/reviews/movie/" + movieId)).get("count").asInt();

        mockMvc.perform(post("/api/reviews/movie/" + movieId).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"rating\": 2, \"comment\": \"Не впечатлило\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/reviews/movie/" + movieId).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"rating\": 5, \"comment\": \"Пересмотрел — шедевр\"}"))
                .andExpect(status().isOk());

        JsonNode summary = getJson(get("/api/reviews/movie/" + movieId).header("Authorization", "Bearer " + token));
        assertThat(summary.get("count").asInt()).isEqualTo(countBefore + 1);
        JsonNode mine = summary.get("reviews").get(0);
        assertThat(mine.get("mine").asBoolean()).isTrue();
        assertThat(mine.get("rating").asInt()).isEqualTo(5);
        assertThat(mine.get("userName").asText()).isEqualTo("Review T.");
        assertThat(movieByTitle("Майкл").get("reviewCount").asInt()).isEqualTo(countBefore + 1);

        String stranger = registerAndLogin("Stranger Reviewer");
        Long reviewId = mine.get("reviewId").asLong();
        mockMvc.perform(delete("/api/reviews/" + reviewId).header("Authorization", "Bearer " + stranger))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/reviews/" + reviewId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(getJson(get("/api/reviews/movie/" + movieId)).get("count").asInt()).isEqualTo(countBefore);
    }

    @Test
    void reviewRulesAreEnforced() throws Exception {
        mockMvc.perform(post("/api/reviews/concert/1").contentType(MediaType.APPLICATION_JSON).content("{\"rating\": 5}"))
                .andExpect(status().isUnauthorized());

        String token = registerAndLogin("Strict Reviewer");
        mockMvc.perform(post("/api/reviews/concert/1").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"rating\": 6}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/reviews/theatre/1").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"rating\": 4}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/reviews/match/99999").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"rating\": 4}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void reviewSummaryIsPublicAndHasDistribution() throws Exception {
        JsonNode summary = getJson(get("/api/reviews/match/1"));

        assertThat(summary.get("count").asInt()).isGreaterThanOrEqualTo(1);
        assertThat(summary.get("distribution")).hasSize(5);
        assertThat(summary.get("reviews").get(0).has("mine")).isTrue();
    }
}
