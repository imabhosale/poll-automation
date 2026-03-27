package com.poll.poll_automation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PollScheduler {

    @Autowired
    private PollService pollService;

    @Scheduled(cron = "10 01 * * ?", zone = "Asia/Kolkata")
    public void runPoll() {
        System.out.println("Running poll at 01:10 AM...");
        pollService.vote();
    }
}