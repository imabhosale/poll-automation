package com.poll.poll_automation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PollAutomationApplication {
	public static void main(String[] args) {
		SpringApplication.run(PollAutomationApplication.class, args);
	}
}
