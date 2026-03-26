package com.poll.poll_automation.controller;

import com.poll.poll_automation.service.PollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private PollService pollService;

    @GetMapping("/vote-now")
    public String voteNow() {
        pollService.vote();
        return "Vote triggered";
    }
}