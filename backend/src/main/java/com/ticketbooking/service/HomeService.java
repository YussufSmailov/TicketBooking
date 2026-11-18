package com.ticketbooking.service;

import com.ticketbooking.dto.HomeCard;
import com.ticketbooking.dto.HomeSection;
import com.ticketbooking.entity.*;
import com.ticketbooking.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class HomeService {

    private static final int CARDS_IN_SECTION = 12;

    private final MovieRepository movieRepository;
    private final MovieSessionRepository sessionRepository;
    private final SeatRepository seatRepository;
    private final ConcertRepository concertRepository;
    private final ConcertSeatRepository concertSeatRepository;
    private final FootballMatchRepository matchRepository;
    private final MatchSeatRepository matchSeatRepository;

    public HomeService(MovieRepository movieRepository,
                       MovieSessionRepository sessionRepository,
                       SeatRepository seatRepository,
                       ConcertRepository concertRepository,
                       ConcertSeatRepository concertSeatRepository,
                       FootballMatchRepository matchRepository,
                       MatchSeatRepository matchSeatRepository) {
        this.movieRepository = movieRepository;
        this.sessionRepository = sessionRepository;
        this.seatRepository = seatRepository;
        this.concertRepository = concertRepository;
        this.concertSeatRepository = concertSeatRepository;
        this.matchRepository = matchRepository;
        this.matchSeatRepository = matchSeatRepository;
    }

    public List<HomeSection> getHome() {
        LocalDate today = LocalDate.now();

        List<HomeCard> premieres = new ArrayList<>();
        List<HomeCard> newMovies = new ArrayList<>();
        List<HomeCard> comingSoon = new ArrayList<>();

        for (Movie movie : movieRepository.findAll()) {
            MovieDetails details = movie.getDetails();
            Optional<MovieSession> nextSession = sessionRepository
                    .findFirstByMovieMovieIdAndSessionDateGreaterThanEqualOrderBySessionDateAscSessionTimeAsc(movie.getMovieId(), today);
            if (nextSession.isEmpty()) {
                continue;
            }

            HomeCard card = movieCard(movie, nextSession.get());
            LocalDate releaseDate = details != null ? details.getReleaseDate() : null;

            if (releaseDate != null && releaseDate.isAfter(today)) {
                comingSoon.add(card);
            } else if (details != null && details.isPremiere()) {
                premieres.add(card);
            } else {
                newMovies.add(card);
            }
        }

        premieres.sort((a, b) -> a.date().compareTo(b.date()));
        comingSoon.sort((a, b) -> a.date().compareTo(b.date()));

        List<HomeCard> concerts = new ArrayList<>();
        for (Concert concert : concertRepository.findByConcertDateGreaterThanEqualOrderByConcertDateAsc(today)) {
            concerts.add(concertCard(concert));
        }

        List<HomeCard> matches = new ArrayList<>();
        for (FootballMatch match : matchRepository.findByMatchDateGreaterThanEqualOrderByMatchDateAscMatchTimeAsc(today)) {
            matches.add(matchCard(match));
        }

        List<HomeSection> sections = new ArrayList<>();
        sections.add(new HomeSection("premieres", "Премьеры", firstCards(premieres)));
        sections.add(new HomeSection("new-movies", "Новинки кино", firstCards(newMovies)));
        sections.add(new HomeSection("concerts", "Концерты", firstCards(concerts)));
        sections.add(new HomeSection("matches", "Матчи КПЛ", firstCards(matches)));
        sections.add(new HomeSection("soon", "Скоро", firstCards(comingSoon)));
        return sections;
    }

    public HomeCard movieCard(Movie movie) {
        Optional<MovieSession> nextSession = sessionRepository
                .findFirstByMovieMovieIdAndSessionDateGreaterThanEqualOrderBySessionDateAscSessionTimeAsc(movie.getMovieId(), LocalDate.now());
        return movieCard(movie, nextSession.orElse(null));
    }

    private HomeCard movieCard(Movie movie, MovieSession nextSession) {
        MovieDetails details = movie.getDetails();
        BigDecimal minPrice = seatRepository
                .findFirstBySessionMovieMovieIdAndSessionSessionDateGreaterThanEqualOrderByPriceAsc(movie.getMovieId(), LocalDate.now())
                .map(Seat::getPrice)
                .orElse(null);
        String genre = details != null && details.getGenre() != null ? details.getGenre() : "Кино";
        String posterUrl = details != null ? details.getPosterUrl() : null;
        return new HomeCard("movie", movie.getMovieId(), movie.getTitle(),
                genre + ", " + movie.getDurationMin() + " мин, " + movie.getAgeRating(),
                nextSession != null ? nextSession.getSessionDate() : null,
                nextSession != null ? nextSession.getSessionTime() : null,
                posterUrl, minPrice, movie.getRating(), movie.getReviewCount(), null, null);
    }

    public HomeCard concertCard(Concert concert) {
        BigDecimal minPrice = concertSeatRepository.findFirstByConcertConcertIdOrderByPriceAsc(concert.getConcertId())
                .map(ConcertSeat::getPrice)
                .orElse(null);
        return new HomeCard("concert", concert.getConcertId(), concert.getArtist(),
                concert.getTitle() + ", " + concert.getCity(), concert.getConcertDate(), concert.getConcertTime(),
                concert.getPosterUrl(), minPrice, concert.getRating(), concert.getReviewCount(), null, null);
    }

    public HomeCard matchCard(FootballMatch match) {
        BigDecimal minPrice = matchSeatRepository.findFirstByMatchMatchIdOrderByPriceAsc(match.getMatchId())
                .map(MatchSeat::getPrice)
                .orElse(null);
        return new HomeCard("match", match.getMatchId(),
                match.getHomeTeam().getName() + " — " + match.getAwayTeam().getName(),
                match.getHomeTeam().getStadium(), match.getMatchDate(), match.getMatchTime(),
                match.getPosterUrl(), minPrice, match.getRating(), match.getReviewCount(),
                match.getHomeTeam().getLogoUrl(), match.getAwayTeam().getLogoUrl());
    }

    private List<HomeCard> firstCards(List<HomeCard> cards) {
        if (cards.size() <= CARDS_IN_SECTION) {
            return cards;
        }
        return cards.subList(0, CARDS_IN_SECTION);
    }
}
