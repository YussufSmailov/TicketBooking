package com.ticketbooking.controller;

import com.ticketbooking.dto.HomeCard;
import com.ticketbooking.service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/api/search")
    public List<HomeCard> search(@RequestParam(name = "q", defaultValue = "") String query) {
        return searchService.search(query);
    }
}
