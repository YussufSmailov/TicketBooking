package com.ticketbooking.service;

import com.ticketbooking.dto.ConcertFilter;
import com.ticketbooking.dto.HomeCard;
import com.ticketbooking.dto.MatchFilter;
import com.ticketbooking.dto.MovieFilter;
import com.ticketbooking.entity.Concert;
import com.ticketbooking.entity.FootballMatch;
import com.ticketbooking.entity.Movie;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    public static final int MAX_RESULTS_PER_TYPE = 20;

    private final CinemaService cinemaService;
    private final ConcertService concertService;
    private final SportService sportService;
    private final HomeService homeService;

    public SearchService(CinemaService cinemaService,
                         ConcertService concertService,
                         SportService sportService,
                         HomeService homeService) {
        this.cinemaService = cinemaService;
        this.concertService = concertService;
        this.sportService = sportService;
        this.homeService = homeService;
    }

    public List<HomeCard> search(String query) {
        if (TextSearch.isBlank(query) || query.trim().length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Введите хотя бы 2 символа");
        }
        String q = query.trim();
        LocalDate today = LocalDate.now();
        List<HomeCard> cards = new ArrayList<>();

        List<Movie> movies = cinemaService.searchMovies(new MovieFilter(q, null, null, null, null, null, null, "rating"));
        for (Movie movie : movies.subList(0, Math.min(movies.size(), MAX_RESULTS_PER_TYPE))) {
            cards.add(homeService.movieCard(movie));
        }

        List<Concert> concerts = concertService.searchConcerts(new ConcertFilter(q, null, null, null, null, null, today, null, "date"));
        for (Concert concert : concerts.subList(0, Math.min(concerts.size(), MAX_RESULTS_PER_TYPE))) {
            cards.add(homeService.concertCard(concert));
        }

        List<FootballMatch> matches = sportService.searchMatches(new MatchFilter(q, null, null, today, null, "date"));
        for (FootballMatch match : matches.subList(0, Math.min(matches.size(), MAX_RESULTS_PER_TYPE))) {
            cards.add(homeService.matchCard(match));
        }
        return cards;
    }
}
