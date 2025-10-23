package at.feedback_service;

import org.springframework.boot.SpringApplication;

public class TestFeedbackServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(FeedbackServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
