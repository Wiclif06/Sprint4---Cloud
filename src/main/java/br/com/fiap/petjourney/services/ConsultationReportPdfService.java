package br.com.fiap.petjourney.services;

import br.com.fiap.petjourney.models.MedicalRecord;
import br.com.fiap.petjourney.models.Medication;
import br.com.fiap.petjourney.repositories.MedicationRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class ConsultationReportPdfService {

    private final MedicationRepository medicationRepository;

    public byte[] generate(MedicalRecord record) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, output);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        Paragraph title = new Paragraph("Relatório de Consulta - PetJourney", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(16);
        document.add(title);

        addSection(document, "Dados da consulta", sectionFont);
        addLine(document, "Clínica: " + value(record.getVeterinarian().getClinic().getName()), normalFont);
        addLine(document, "Data do relatório: " + value(record.getRegistrationDate()), normalFont);
        addLine(document, "Agendamento: " + (record.getAppointment() != null ? record.getAppointment().getId() : "Não vinculado"), normalFont);
        addLine(document, "Veterinário: " + value(record.getVeterinarian().getName()), normalFont);
        addLine(document, "Tutor: " + value(record.getPet().getTutor().getName()), normalFont);
        addLine(document, "Pet: " + value(record.getPet().getName()), normalFont);

        addSection(document, "Relatório clínico", sectionFont);
        addLine(document, "Queixa principal: " + value(record.getMainComplaint()), normalFont);
        addLine(document, "Diagnóstico/Avaliação: " + value(record.getDiagnosis()), normalFont);
        addLine(document, "Conduta: " + value(record.getConduct()), normalFont);
        addLine(document, "Anotações: " + value(record.getClinicalNotes()), normalFont);
        addLine(document, "Recomendações: " + value(record.getRecommendations()), normalFont);
        addLine(document, "Prescrição textual: " + value(record.getPrescriptionNotes()), normalFont);
        addLine(document, "Observações: " + value(record.getObservations()), normalFont);

        addSection(document, "Prescrições", sectionFont);
        var medications = medicationRepository.findByMedicalRecordId(record.getId(), org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .toList();

        if (medications.isEmpty()) {
            addLine(document, "Nenhum medicamento prescrito.", normalFont);
        } else {
            for (Medication medication : medications) {
                addLine(document, "- " + medication.getName()
                        + " | Dosagem: " + value(medication.getDosage())
                        + " | Frequência: " + value(medication.getFrequency())
                        + " | Início: " + value(medication.getStartDate())
                        + " | Fim: " + value(medication.getEndDate())
                        + " | Obs: " + value(medication.getObservations()), normalFont);
            }
        }

        document.close();
        return output.toByteArray();
    }

    private void addSection(Document document, String text, Font font) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setSpacingBefore(10);
        paragraph.setSpacingAfter(6);
        document.add(paragraph);
    }

    private void addLine(Document document, String text, Font font) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setSpacingAfter(4);
        document.add(paragraph);
    }

    private String value(Object value) {
        return value == null || value.toString().isBlank() ? "Não informado" : value.toString();
    }
}

