package com.sitesync.auth.service;

import com.sitesync.auth.entity.OtpCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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

    /**
     * Sends a supervisor their login credentials after a contractor
     * creates their account (AuthService.inviteSupervisor). Contains a
     * plaintext temporary password by necessity - this is the one and
     * only time it exists outside the (already-hashed) database, since
     * AuthService never logs or stores it in plaintext. The email itself
     * is the delivery mechanism; there's no separate confirm-link/OTP
     * step for supervisor accounts the way self-signup has one.
     */
    public void sendSupervisorInviteEmail(String toEmail, String name, String temporaryPassword) {
        String subject = "You've been added to SiteSync";
        String body = "Hi " + name + ",\n\n"
                + "A contractor has added you as a supervisor on SiteSync. "
                + "Sign in with:\n\n"
                + "Email: " + toEmail + "\n"
                + "Temporary password: " + temporaryPassword + "\n\n"
                + "Please change your password after logging in (Settings > Change Password).";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}