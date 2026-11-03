package br.com.fiap.petjourney.services;

import br.com.fiap.petjourney.models.Appointment;
import br.com.fiap.petjourney.models.Clinic;
import br.com.fiap.petjourney.models.Pet;
import br.com.fiap.petjourney.models.Tutor;
import br.com.fiap.petjourney.models.Veterinarian;
import br.com.fiap.petjourney.models.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SmtpEmailSender smtpEmailSender;
    private final BrevoEmailSender brevoEmailSender;

    @Value("${petjourney.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${petjourney.mail.provider:smtp}")
    private String mailProvider;

    @Value("${petjourney.mail.from:no-reply@petjourney.com}")
    private String from;

    public void sendFirstAccessCode(String to, String tutorName, String code, LocalDateTime expiresAt) {
        String subject = "Primeiro acesso PetJourney";
        String body = """
                Ola, %s.

                Sua conta PetJourney foi criada pela clinica veterinaria.

                Codigo temporario: %s
                Validade: %s

                Acesse a opcao "Primeiro acesso" no app, informe seu e-mail e este codigo, e crie sua propria senha.

                Por seguranca, nenhuma senha e enviada por e-mail.
                """.formatted(valueOrDefault(tutorName, "tutor"), code, format(expiresAt));

        send(to, subject, body);
    }

    public void sendAppointmentCancellationToTutor(Appointment appointment, UserRole cancelledBy) {
        Tutor tutor = tutor(appointment);
        String to = tutor != null ? tutor.getEmail() : null;
        String subject = "Consulta cancelada - PetJourney";
        String body = cancellationBody(appointment, cancelledBy);

        send(to, subject, body);
    }

    public void sendAppointmentCancellationToClinicOrVet(Appointment appointment, UserRole cancelledBy) {
        Veterinarian veterinarian = appointment.getVeterinarian();
        Clinic clinic = appointment.getClinic();
        String to = veterinarian != null && hasText(veterinarian.getEmail())
                ? veterinarian.getEmail()
                : clinic != null ? clinic.getEmail() : null;
        String subject = "Consulta cancelada - PetJourney";
        String body = cancellationBody(appointment, cancelledBy);

        send(to, subject, body);
    }

    private void send(String to, String subject, String body) {
        if (!hasText(to)) {
            log.warn("E-mail PetJourney nao enviado: destinatario vazio. Assunto: {}", subject);
            return;
        }

        if (!mailEnabled) {
            log.info("""
                    E-mail PetJourney em modo local.
                    Para: {}
                    De: {}
                    Assunto: {}
                    Conteudo:
                    {}
                    """, to, from, subject, body);
            return;
        }

        if ("brevo".equalsIgnoreCase(mailProvider)) {
            brevoEmailSender.send(from, to, subject, body);
            return;
        }

        smtpEmailSender.send(from, to, subject, body);
    }

    private String cancellationBody(Appointment appointment, UserRole cancelledBy) {
        return """
                A consulta foi cancelada.

                Pet: %s
                Data/hora: %s
                Clinica: %s
                Veterinario: %s
                Cancelado por: %s
                """.formatted(
                petName(appointment),
                format(appointment.getDateTime()),
                clinicName(appointment),
                veterinarianName(appointment),
                cancelledBy != null ? cancelledBy.name() : "nao informado"
        );
    }

    private Tutor tutor(Appointment appointment) {
        Pet pet = appointment.getPet();
        return pet != null ? pet.getTutor() : null;
    }

    private String petName(Appointment appointment) {
        Pet pet = appointment.getPet();
        return pet != null ? valueOrDefault(pet.getName(), "nao informado") : "nao informado";
    }

    private String clinicName(Appointment appointment) {
        Clinic clinic = appointment.getClinic();
        return clinic != null ? valueOrDefault(clinic.getName(), "nao informada") : "nao informada";
    }

    private String veterinarianName(Appointment appointment) {
        Veterinarian veterinarian = appointment.getVeterinarian();
        return veterinarian != null ? valueOrDefault(veterinarian.getName(), "nao informado") : "nao informado";
    }

    private String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "nao informada";
    }

    private String valueOrDefault(String value, String defaultValue) {
        return hasText(value) ? value : defaultValue;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

