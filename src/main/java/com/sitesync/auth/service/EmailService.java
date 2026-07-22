package com.sitesync.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.sitesync.auth.entity.OtpCode;

/**
 * Sends OTP codes via Gmail SMTP (JavaMailSender). Gmail SMTP requires an
 * App Password (not your regular Gmail password) since Google requires
 * 2FA + app passwords for SMTP access. Free tier: Gmail's sending limit
 * is ~500/day for a regular account - fine for early testing, but plan
 * to move to a transactional email provider (Resend/SendGrid/etc.)
 * before that becomes a real constraint, not after.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendOtpEmail(String toEmail, String code, OtpCode.OtpPurpose purpose) {
        String subject = purpose == OtpCode.OtpPurpose.signup
                ? "Verify your SiteSync account"
                : "Your SiteSync login code";

        String body = purpose == OtpCode.OtpPurpose.signup
                ? "Welcome to SiteSync! Your verification code is: " + code
                    + "\n\nThis code expires in 10 minutes. If you didn't request this, ignore this email."
                : "Your SiteSync login code is: " + code
                    + "\n\nThis code expires in 10 minutes. If you didn't request this, ignore this email.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        // Not wrapped in try/catch here deliberately - a failure to send
        // should propagate as a 500 (ServerFailure in Flutter) rather than
        // silently succeeding with "OTP sent" when it wasn't.
        mailSender.send(message);
    }
}