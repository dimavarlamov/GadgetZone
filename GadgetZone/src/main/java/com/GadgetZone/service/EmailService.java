package com.GadgetZone.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationEmail(String to, String verificationLink) {
        try {
            logger.debug("Preparing to send verification email to: {}", to);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Подтверждение регистрации в GadgetZone");
            message.setText("Здравствуйте!\n\n" +
                    "Благодарим вас за регистрацию в GadgetZone. " +
                    "Для завершения регистрации, пожалуйста, перейдите по следующей ссылке:\n\n" +
                    verificationLink + "\n\n" +
                    "Если вы не регистрировались на нашем сайте, просто проигнорируйте это письмо.\n\n" +
                    "С уважением,\nКоманда GadgetZone");

            logger.debug("Email message prepared. Attempting to send...");
            mailSender.send(message);
            logger.info("Verification email sent successfully to: {}", to);
        } catch (MailException e) {
            logger.error("Failed to send verification email to: {}. Error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Не удалось отправить email для подтверждения. Пожалуйста, попробуйте позже.", e);
        } catch (Exception e) {
            logger.error("Unexpected error while sending email to: {}. Error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Произошла неожиданная ошибка при отправке email. Пожалуйста, попробуйте позже.", e);
        }
    }
} 