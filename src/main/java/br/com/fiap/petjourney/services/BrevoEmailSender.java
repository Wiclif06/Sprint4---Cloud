package br.com.fiap.petjourney.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrevoEmailSender {

    private final ObjectMapper objectMapper;

    @Value("${brevo.api-key:}")
    private String apiKey;

    @Value("${brevo.api-url:https://api.brevo.com/v3/smtp/email}")
    private String apiUrl;

    @Value("${brevo.timeout-ms:10000}")
    private int timeoutMs;

    @Async("mailTaskExecutor")
    public void send(String from, String to, String subject, String body) {
        if (!hasText(apiKey)) {
            log.error("E-mail PetJourney nao enviado via Brevo: BREVO_API_KEY nao configurada");
            return;
        }

        try {
            EmailAddress sender = EmailAddress.parse(from);
            EmailAddress recipient = EmailAddress.parse(to);
            BrevoEmailRequest requestBody = new BrevoEmailRequest(
                    new Sender(sender.name(), sender.email()),
                    List.of(new Recipient(recipient.email(), recipient.name())),
                    subject,
                    body
            );

            String json = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("accept", "application/json")
                    .header("api-key", apiKey)
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            log.info(
                    "Enviando e-mail PetJourney via Brevo. apiUrl={}, timeoutMs={}, from={}, to={}, subject={}",
                    apiUrl,
                    timeoutMs,
                    from,
                    to,
                    subject
            );
            HttpResponse<String> response = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(timeoutMs))
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("E-mail PetJourney enviado via Brevo para {} com assunto {}", to, subject);
                return;
            }

            log.error(
                    "Falha ao enviar e-mail PetJourney via Brevo para {}. status={}, body={}",
                    to,
                    response.statusCode(),
                    response.body()
            );
        } catch (Exception exception) {
            log.error("Falha ao enviar e-mail PetJourney via Brevo para {} com assunto {}", to, subject, exception);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record BrevoEmailRequest(Sender sender, List<Recipient> to, String subject, String textContent) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record Sender(String name, String email) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record Recipient(String email, String name) {
    }

    private record EmailAddress(String name, String email) {
        private static EmailAddress parse(String value) {
            String trimmed = value == null ? "" : value.trim();
            int start = trimmed.indexOf('<');
            int end = trimmed.indexOf('>');

            if (start >= 0 && end > start) {
                String parsedName = trimmed.substring(0, start).trim();
                String parsedEmail = trimmed.substring(start + 1, end).trim();
                return new EmailAddress(blankToNull(parsedName), parsedEmail);
            }

            return new EmailAddress(null, trimmed);
        }

        private static String blankToNull(String value) {
            return value == null || value.isBlank() ? null : value;
        }
    }
}

