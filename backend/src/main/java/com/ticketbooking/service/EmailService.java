package com.ticketbooking.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;
    private final String fromName;
    private final long codeMinutes;

    // Имя отправителя задаётся здесь, а не в .properties: Spring читает .properties как ISO-8859-1,
    // и русские буквы оттуда превращаются в кракозябры.
    public EmailService(ObjectProvider<JavaMailSender> mailSender,
                        @Value("${app.mail.from}") String from,
                        @Value("${app.mail.from-name:Билеты}") String fromName,
                        @Value("${app.verification.code-minutes:15}") long codeMinutes) {
        this.mailSender = mailSender;
        this.from = from;
        this.fromName = fromName;
        this.codeMinutes = codeMinutes;
    }

    public void sendVerificationCode(String email, String fullName, String code) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("SMTP не настроен (нет SPRING_MAIL_HOST). Код подтверждения для {}: {}", email, code);
            return;
        }

        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from, fromName);
            helper.setTo(email);
            helper.setSubject("Код подтверждения: " + code);
            helper.setText(plainText(fullName, code), htmlText(fullName, code));
            sender.send(message);
        } catch (MessagingException | MailException | java.io.UnsupportedEncodingException e) {
            log.error("Не удалось отправить письмо на {}", email, e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Не удалось отправить письмо с кодом. Попробуйте позже");
        }
    }

    private String plainText(String fullName, String code) {
        return "Здравствуйте, " + fullName + "!\n\n"
                + "Ваш код подтверждения: " + code + "\n"
                + "Код действует " + codeMinutes + " минут.\n\n"
                + "Если вы не регистрировались на сайте, просто проигнорируйте это письмо.";
    }

    private String htmlText(String fullName, String code) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto;padding:24px;color:#1f2933">
                  <h2 style="margin:0 0 16px">Подтверждение email</h2>
                  <p>Здравствуйте, %s!</p>
                  <p>Введите этот код на сайте, чтобы завершить регистрацию:</p>
                  <p style="font-size:32px;font-weight:bold;letter-spacing:8px;background:#f1f3f5;padding:16px;text-align:center;border-radius:8px">%s</p>
                  <p style="color:#616e7c">Код действует %d минут. Если вы не регистрировались, просто проигнорируйте письмо.</p>
                </div>
                """.formatted(escape(fullName), code, codeMinutes);
    }

    private String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
