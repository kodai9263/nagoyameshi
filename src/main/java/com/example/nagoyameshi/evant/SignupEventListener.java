package com.example.nagoyameshi.evant;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.service.VerificationTokenService;

@Component
public class SignupEventListener {
	private final VerificationTokenService verificationTokenService;
	private final JavaMailSender javaMailSender;
	
	public SignupEventListener(VerificationTokenService verificationTokenService, JavaMailSender javaMailSender) {
		this.verificationTokenService = verificationTokenService;
		this.javaMailSender = javaMailSender;
	}
	
	@EventListener
	public void onSignupEvent(SignupEvent signupEvent) {
		User user = signupEvent.getUser();
		String token = UUID.randomUUID().toString();
		verificationTokenService.createVerificationToken(user, token);
		
		String senderAddress = "koudai9263@yahoo.co.jp";
		String recipintAddress = user.getEmail();
		String subject = "メール認証";
		String confirmationUrl = signupEvent.getRequestUrl() + "/verify?token=" + token;
		String message = "以下のリンクをクリックして会員登録を完了してください。";
		
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom(senderAddress);
		mailMessage.setTo(recipintAddress);
		mailMessage.setSubject(subject);
		mailMessage.setText(message + "\n" + confirmationUrl);
		javaMailSender.send(mailMessage);
	}
}
