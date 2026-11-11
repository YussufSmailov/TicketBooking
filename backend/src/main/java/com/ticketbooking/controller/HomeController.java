package com.ticketbooking.controller;

import com.ticketbooking.dto.HomeSection;
import com.ticketbooking.service.HomeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping("/api/home")
    public List<HomeSection> getHome() {
        return homeService.getHome();
    }
}
