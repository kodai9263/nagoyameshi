package com.example.nagoyameshi.evant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.nagoyameshi.entity.User;

@Component

public class SignupEventPublisher {
	private ApplicationEventPublisher applicationEventPublisger;
	
	public SignupEventPublisher(ApplicationEventPublisher applicationEventPublisger) {
		this.applicationEventPublisger = applicationEventPublisger;
	}

	public void publishSignupEvent(User user, String requestUrl) {
		applicationEventPublisger.publishEvent(new SignupEvent(this, user, requestUrl));
	}
}
