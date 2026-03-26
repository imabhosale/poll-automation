package com.poll.poll_automation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PollScheduler {

    @Autowired
    private PollService pollService;

    @Scheduled(cron = "0 0 10 * * ?") // 10:00 AM daily
    public void runPoll() {
        System.out.println("Running poll at 10 AM...");
        pollService.vote();
    }
}