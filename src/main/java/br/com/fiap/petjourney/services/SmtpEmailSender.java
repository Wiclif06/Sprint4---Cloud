package br.com.fiap.petjourney.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpEmailSender {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.port:587}")
    private int mailPort;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Async("mailTaskExecutor")
    public void send(String from, String to, String subject, String body) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.error("E-mail PetJourney nao enviado: JavaMailSender indisponivel com MAIL_ENABLED=true");
            return;
        }

        try {
            log.info(
                    "Enviando e-mail PetJourney via SMTP. host={}, port={}, username={}, passwordConfigured={}, from={}, to={}, subject={}",
                    mailHost,
                    mailPort,
                    valueOrDefault(mailUsername, "nao configurado"),
                    hasText(mailPassword),
                    from,
                    to,
                    subject
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("E-mail PetJourney enviado para {} com assunto {}", to, subject);
        } catch (RuntimeException exception) {
            log.error("Falha ao enviar e-mail PetJourney para {} com assunto {}", to, subject, exception);
        }
    }

    private String valueOrDefault(String value, String defaultValue) {
        return hasText(value) ? value : defaultValue;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

