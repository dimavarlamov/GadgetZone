package com.GadgetZone.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
            logger.info("Попытка отправки письма подтверждения на: {}", to);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Подтверждение регистрации в GadgetZone");

            String htmlContent = "<h3>Здравствуйте!</h3>"
                    + "<p>Для завершения регистрации перейдите по ссылке:</p>"
                    + "<p><a href=\"" + verificationLink + "\">Подтвердить email</a></p>"
                    + "<p>Ссылка действительна 24 часа.</p>";

            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Письмо успешно отправлено на: {}", to);
        } catch (MailException e) {
            logger.error("Ошибка отправки письма на {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Не удалось отправить письмо подтверждения.", e);
        } catch (Exception e) {
            logger.error("Неизвестная ошибка при отправке письма на {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Произошла ошибка при отправке письма.", e);
        }
    }
}