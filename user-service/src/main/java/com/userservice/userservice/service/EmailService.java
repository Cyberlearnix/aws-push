package com.userservice.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Recipient email address is missing");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            
            helper.setTo(to);
            helper.setSubject("🔐 Your OTP for CyberLearnix - Secure Verification");
            helper.setFrom("cyberlearnixprivatelimited@gmail.com");
            
            // Load and process templates
            String htmlContent = loadHtmlTemplate();
            String plainTextContent = loadPlainTextTemplate();
            
            // Replace OTP placeholder in both templates
            htmlContent = htmlContent.replace("{{OTP_CODE}}", otp);
            plainTextContent = plainTextContent.replace("{{OTP_CODE}}", otp);
            
            // Set both HTML and plain text content
            helper.setText(plainTextContent, htmlContent);
            
            mailSender.send(message);
        } catch (MessagingException | IOException e) {
            throw new RuntimeException("Could not send OTP email: " + e.getMessage(), e);
        }
    }
    
    private String loadHtmlTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/otp-email-template.html");
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
    
    private String loadPlainTextTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/otp-email-plain.txt");
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
    
    public void sendPasswordResetEmail(String to, String otp) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Recipient email address is missing");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            
            helper.setTo(to);
            helper.setSubject("🔒 Password Reset Request - CyberLearnix");
            helper.setFrom("cyberlearnixprivatelimited@gmail.com");
            
            // Load and process password reset template
            String htmlContent = loadPasswordResetTemplate();
            htmlContent = htmlContent.replace("{{OTP_CODE}}", otp);
            
            // Create plain text version
            String plainTextContent = createPasswordResetPlainText(otp);
            
            // Set both HTML and plain text content
            helper.setText(plainTextContent, htmlContent);
            
            mailSender.send(message);
        } catch (MessagingException | IOException e) {
            throw new RuntimeException("Could not send password reset email: " + e.getMessage(), e);
        }
    }
    
    private String loadPasswordResetTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/password-reset-email-template.html");
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
    
    private String createPasswordResetPlainText(String otp) {
        return String.format("""
            ===============================================
            🔒 PASSWORD RESET REQUEST - CyberLearnix
            ===============================================
            
            Hello!
            
            We received a request to reset the password for your CyberLearnix account.
            
            Your Password Reset Code: %s
            
            ⏰ IMPORTANT: This code will expire in 5 minutes for your security.
            
            📋 NEXT STEPS:
            1. Enter the verification code above in the password reset form
            2. Create a new strong password
            3. Confirm your new password
            4. You'll be automatically logged in with your new password
            
            🛡️ SECURITY TIPS FOR YOUR NEW PASSWORD:
            • Use at least 8 characters with a mix of letters, numbers, and symbols
            • Don't use personal information like your name or email
            • Avoid common words or patterns
            • Consider using a password manager
            
            ⚠️ SECURITY NOTICE:
            If you didn't request this password reset, your account may be compromised. 
            Please contact our support team immediately and do not use this code.
            
            If you're having trouble with the code or didn't request this reset, 
            please contact our support team.
            
            ===============================================
            CyberLearnix Private Limited
            📧 support@cyberlearnix.com
            🌐 www.cyberlearnix.com
            📱 +1 (555) 123-4567
            
            This is an automated message. Please do not reply to this email.
            ===============================================
            """, otp);
    }
}
