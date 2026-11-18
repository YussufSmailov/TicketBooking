package com.ticketbooking.service;

import com.ticketbooking.dto.ReviewRequest;
import com.ticketbooking.dto.ReviewResponse;
import com.ticketbooking.dto.ReviewSummary;
import com.ticketbooking.entity.Review;
import com.ticketbooking.entity.User;
import com.ticketbooking.repository.ConcertRepository;
import com.ticketbooking.repository.FootballMatchRepository;
import com.ticketbooking.repository.MovieRepository;
import com.ticketbooking.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ReviewService {

    public static final Set<String> EVENT_TYPES = Set.of("movie", "concert", "match");

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;
    private final ConcertRepository concertRepository;
    private final FootballMatchRepository matchRepository;
    private final UserService userService;

    public ReviewService(ReviewRepository reviewRepository,
                         MovieRepository movieRepository,
                         ConcertRepository concertRepository,
                         FootballMatchRepository matchRepository,
                         UserService userService) {
        this.reviewRepository = reviewRepository;
        this.movieRepository = movieRepository;
        this.concertRepository = concertRepository;
        this.matchRepository = matchRepository;
        this.userService = userService;
    }

    public ReviewSummary getReviews(String eventType, Long eventId, String currentUserEmail) {
        checkEventExists(eventType, eventId);
        Long currentUserId = currentUserEmail == null ? null : userService.getByEmail(currentUserEmail).getUserId();

        List<Review> reviews = reviewRepository.findByEventTypeAndEventIdOrderByUpdatedAtDesc(eventType, eventId);
        List<ReviewResponse> responses = new ArrayList<>();
        List<Integer> distribution = new ArrayList<>(List.of(0, 0, 0, 0, 0));
        int sum = 0;

        for (Review review : reviews) {
            boolean mine = review.getUser().getUserId().equals(currentUserId);
            ReviewResponse response = toResponse(review, mine);
            if (mine) {
                responses.add(0, response);
            } else {
                responses.add(response);
            }
            distribution.set(review.getRating() - 1, distribution.get(review.getRating() - 1) + 1);
            sum += review.getRating();
        }

        Double average = reviews.isEmpty() ? null : BigDecimal.valueOf(sum)
                .divide(BigDecimal.valueOf(reviews.size()), 1, RoundingMode.HALF_UP)
                .doubleValue();
        return new ReviewSummary(eventType, eventId, average, reviews.size(), distribution, responses);
    }

    @Transactional
    public ReviewResponse saveReview(String eventType, Long eventId, String userEmail, ReviewRequest request) {
        checkEventExists(eventType, eventId);
        User user = userService.getByEmail(userEmail);
        LocalDateTime now = LocalDateTime.now();

        Review review = reviewRepository.findByUserUserIdAndEventTypeAndEventId(user.getUserId(), eventType, eventId)
                .orElseGet(() -> {
                    Review created = new Review();
                    created.setUser(user);
                    created.setEventType(eventType);
                    created.setEventId(eventId);
                    created.setCreatedAt(now);
                    return created;
                });
        review.setRating(request.rating());
        review.setComment(request.comment() == null || request.comment().isBlank() ? null : request.comment().trim());
        review.setUpdatedAt(now);
        return toResponse(reviewRepository.save(review), true);
    }

    @Transactional
    public void deleteReview(Long reviewId, String userEmail, boolean isAdmin) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Отзыв не найден"));
        User user = userService.getByEmail(userEmail);
        if (!isAdmin && !review.getUser().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нельзя удалить чужой отзыв");
        }
        reviewRepository.delete(review);
    }

    public void deleteReviewsOf(String eventType, Long eventId) {
        reviewRepository.deleteByEventTypeAndEventId(eventType, eventId);
    }

    private void checkEventExists(String eventType, Long eventId) {
        boolean exists = switch (eventType) {
            case "movie" -> movieRepository.existsById(eventId);
            case "concert" -> concertRepository.existsById(eventId);
            case "match" -> matchRepository.existsById(eventId);
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Неизвестный тип события");
        };
        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Событие не найдено");
        }
    }

    private ReviewResponse toResponse(Review review, boolean mine) {
        return new ReviewResponse(review.getReviewId(), shortName(review.getUser().getFullName()), review.getRating(),
                review.getComment(), review.getCreatedAt(), review.getUpdatedAt(), mine);
    }

    static String shortName(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length < 2) {
            return parts[0];
        }
        return parts[0] + " " + parts[1].charAt(0) + ".";
    }
}
