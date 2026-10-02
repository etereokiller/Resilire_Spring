package com.resilire.backend.prescription;

import com.resilire.backend.appointment.Appointment;
import com.resilire.backend.consultation.Consultation;
import com.resilire.backend.doctor.DoctorProfile;
import com.resilire.backend.patient.PatientProfile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;

/**
 * Renders a {@code GENERATED} prescription (Release 5) as a downloadable PDF, on demand -
 * nothing is persisted to disk, since the source of truth is the {@link Prescription} row and
 * its medicines/tests.
 */
@Service
public class PrescriptionPdfService {

    private static final PDType1Font FONT = PDType1Font.HELVETICA;
    private static final PDType1Font FONT_BOLD = PDType1Font.HELVETICA_BOLD;
    private static final float MARGIN = 50f;
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;

    public byte[] generate(Prescription prescription) {
        Consultation consultation = prescription.getConsultation();
        Appointment appointment = consultation.getAppointment();
        DoctorProfile doctor = appointment.getDoctor();
        PatientProfile patient = appointment.getPatient();

        try (PDDocument document = new PDDocument()) {
            PDImageXObject logo = LogoImage.load(document);
            Cursor cursor = new Cursor(document);

            cursor.drawLetterhead(logo, doctor, "PRESCRIPTION / RECETA M\u00c9DICA");
            cursor.patientBox(patient, appointment);

            if (!isBlank(consultation.getChiefComplaint())) {
                cursor.writeSection("REASON FOR CONSULTATION / MOTIVO DE CONSULTA", consultation.getChiefComplaint());
            }
            if (!isBlank(consultation.getDiagnosis())) {
                cursor.writeSection("DIAGNOSIS / DIAGN\u00d3STICO", consultation.getDiagnosis());
            }

            cursor.writeLabel("MEDICATION / MEDICAMENTOS");
            if (prescription.getMedicines().isEmpty()) {
                cursor.writeWrapped(MARGIN + 10, "No medication prescribed.");
            } else {
                for (PrescriptionMedicine m : prescription.getMedicines()) {
                    StringBuilder line = new StringBuilder("- ").append(m.getMedicineName());
                    appendIfPresent(line, " | Dosage: ", m.getDosage());
                    appendIfPresent(line, " | Frequency: ", m.getFrequency());
                    appendIfPresent(line, " | Duration: ", m.getDuration());
                    cursor.writeWrapped(MARGIN + 10, line.toString());
                    if (!isBlank(m.getInstructions())) {
                        cursor.writeWrapped(MARGIN + 20, "Instructions: " + m.getInstructions());
                    }
                }
            }
            cursor.gap(8);

            cursor.writeLabel("DIAGNOSTIC TESTS / EX\u00c1MENES");
            if (prescription.getTests().isEmpty()) {
                cursor.writeWrapped(MARGIN + 10, "None recommended.");
            } else {
                for (PrescriptionTest t : prescription.getTests()) {
                    cursor.writeWrapped(MARGIN + 10, "- " + t.getTestName());
                    if (!isBlank(t.getInstructions())) {
                        cursor.writeWrapped(MARGIN + 20, "Instructions: " + t.getInstructions());
                    }
                }
            }

            if (!isBlank(prescription.getNotes())) {
                cursor.gap(8);
                cursor.writeSection("Additional Notes", prescription.getNotes());
            }

            cursor.signature(doctor);
            cursor.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate prescription PDF", e);
        }
    }

    public byte[] generateCertificate(Consultation consultation) {
        Appointment appointment = consultation.getAppointment();
        DoctorProfile doctor = appointment.getDoctor();
        PatientProfile patient = appointment.getPatient();
        try (PDDocument document = new PDDocument()) {
            Cursor cursor = new Cursor(document);
            cursor.drawLetterhead(LogoImage.load(document), doctor, "MEDICAL CERTIFICATE / CERTIFICADO M\u00c9DICO");
            cursor.patientBox(patient, appointment);
            cursor.gap(12);
            cursor.writeSection("CERTIFICATION / CERTIFICACI\u00d3N", "I certify that " + fullName(patient.getFirstName(), patient.getSurname1(), patient.getSurname2())
                    + " attended a medical consultation on " + appointment.getAppointmentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".");
            if (!isBlank(consultation.getCertificateReason())) {
                cursor.writeSection("PURPOSE / MOTIVO", consultation.getCertificateReason());
            }
            cursor.signature(doctor);
            cursor.close();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate medical certificate PDF", e);
        }
    }

    private static String fullName(String first, String surname1, String surname2) {
        return String.join(" ", first, surname1, surname2).trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static void appendIfPresent(StringBuilder builder, String label, String value) {
        if (!isBlank(value)) {
            builder.append(label).append(value);
        }
    }

    /**
     * Tracks the current write position/page for a document and starts a new page whenever the
     * next line would run past the bottom margin - a prescription with many medicines/tests can
     * legitimately overflow a single A4 page.
     */
    private static final class Cursor {
        private final PDDocument document;
        private PDPageContentStream stream;
        private float y;

        Cursor(PDDocument document) throws IOException {
            this.document = document;
            newPage();
        }

        void writeTitle(String text) throws IOException {
            writeLine(text, FONT_BOLD, 16, 22);
        }

        void drawLogo(PDImageXObject logo) throws IOException {
            float width = 150f;
            float height = width * logo.getHeight() / (float) logo.getWidth();
            if (y - height < MARGIN) {
                newPage();
            }
            stream.drawImage(logo, MARGIN, y - height, width, height);
            y -= height;
        }

        void drawLetterhead(PDImageXObject logo, DoctorProfile doctor, String title) throws IOException {
            float width = 105f;
            float height = width * logo.getHeight() / (float) logo.getWidth();
            stream.drawImage(logo, MARGIN, y - height, width, height);
            float textX = MARGIN + width + 18;
            writeAt(textX, y - 12, "DR(A). " + fullName(doctor.getFirstName(), doctor.getSurname1(), doctor.getSurname2()), FONT_BOLD, 13);
            writeAt(textX, y - 30, isBlank(doctor.getSpecialization()) ? "MEDICAL PROFESSIONAL" : doctor.getSpecialization().toUpperCase(), FONT_BOLD, 10);
            writeAt(textX, y - 46, "RUT: " + doctor.getRut(), FONT, 10);
            y -= Math.max(height, 58f) + 10;
            writeCentered(title, FONT_BOLD, 16);
            rule();
        }

        void patientBox(PatientProfile patient, Appointment appointment) throws IOException {
            gap(8);
            writeLabel("PATIENT INFORMATION / DATOS DEL PACIENTE");
            writeLine("Name / Nombre: " + fullName(patient.getFirstName(), patient.getSurname1(), patient.getSurname2()), FONT_BOLD, 11, 15);
            writeLine("RUT: " + patient.getRut() + "     Date / Fecha: " + appointment.getAppointmentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), FONT, 11, 15);
            String demographic = "Address / Direcci\u00f3n: " + (patient.getAddress() == null ? "-" : patient.getAddress());
            if (patient.getDateOfBirth() != null) demographic += "     DOB / Nacimiento: " + patient.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            writeWrapped(MARGIN, demographic);
            rule();
        }

        void signature(DoctorProfile doctor) throws IOException {
            gap(36);
            ruleAt(PAGE_WIDTH - MARGIN - 190, 190);
            writeAt(PAGE_WIDTH - MARGIN - 190, y - 14, "DR(A). " + fullName(doctor.getFirstName(), doctor.getSurname1(), doctor.getSurname2()), FONT_BOLD, 10);
            writeAt(PAGE_WIDTH - MARGIN - 190, y - 28, "RUT: " + doctor.getRut(), FONT, 10);
            y -= 34;
        }

        void rule() throws IOException { ruleAt(MARGIN, CONTENT_WIDTH); y -= 8; }
        void ruleAt(float x, float width) throws IOException { stream.moveTo(x, y); stream.lineTo(x + width, y); stream.stroke(); }
        void writeCentered(String text, PDType1Font font, float size) throws IOException { writeAt((PAGE_WIDTH - textWidth(font, size, text)) / 2, y, text, font, size); y -= size + 8; }
        void writeAt(float x, float baseline, String text, PDType1Font font, float size) throws IOException { stream.beginText(); stream.setFont(font, size); stream.newLineAtOffset(x, baseline); stream.showText(sanitize(text)); stream.endText(); }

        void writeLabel(String text) throws IOException {
            gap(4);
            writeLine(text, FONT_BOLD, 12, 16);
        }

        void writeLine(String text) throws IOException {
            writeLine(text, FONT, 11, 15);
        }

        void writeSection(String label, String body) throws IOException {
            writeLabel(label);
            writeWrapped(MARGIN + 10, body);
            gap(4);
        }

        void writeWrapped(float x, String text) throws IOException {
            float width = PAGE_WIDTH - MARGIN - x;
            for (String line : wrap(text, FONT, 10, width)) {
                writeLine(line, x, FONT, 10, 14);
            }
        }

        void gap(float amount) {
            y -= amount;
        }

        private void writeLine(String text, PDType1Font font, float size, float lineHeight) throws IOException {
            writeLine(text, MARGIN, font, size, lineHeight);
        }

        private void writeLine(String text, float x, PDType1Font font, float size, float lineHeight)
                throws IOException {
            if (y - lineHeight < MARGIN) {
                newPage();
            }
            stream.beginText();
            stream.setFont(font, size);
            stream.newLineAtOffset(x, y);
            stream.showText(sanitize(text));
            stream.endText();
            y -= lineHeight;
        }

        private void newPage() throws IOException {
            if (stream != null) {
                stream.close();
            }
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = PAGE_HEIGHT - MARGIN;
        }

        void close() throws IOException {
            stream.close();
        }

        private List<String> wrap(String text, PDType1Font font, float size, float maxWidth) throws IOException {
            List<String> lines = new ArrayList<>();
            for (String paragraph : text.split("\n")) {
                StringBuilder current = new StringBuilder();
                for (String word : paragraph.split(" ")) {
                    String candidate = current.isEmpty() ? word : current + " " + word;
                    if (!current.isEmpty() && textWidth(font, size, candidate) > maxWidth) {
                        lines.add(current.toString());
                        current = new StringBuilder(word);
                    } else {
                        current = new StringBuilder(candidate);
                    }
                }
                lines.add(current.toString());
            }
            return lines;
        }

        private float textWidth(PDType1Font font, float size, String text) throws IOException {
            return font.getStringWidth(sanitize(text)) / 1000 * size;
        }

        /**
         * Helvetica/WinAnsiEncoding covers Latin-1 (accented Spanish characters, ñ, ¿, ¡
         * included), so only normalize a few "smart" punctuation characters and fall back to
         * "?" for anything genuinely outside that range (e.g. emoji), rather than stripping
         * every non-ASCII character - this app is bilingual EN/ES.
         */
        private String sanitize(String text) {
            if (text == null) {
                return "";
            }
            String cleaned = text.replace("\r", "")
                    .replace('‘', '\'').replace('’', '\'')
                    .replace('“', '"').replace('”', '"')
                    .replace('–', '-').replace('—', '-')
                    .replace("…", "...");
            StringBuilder sb = new StringBuilder(cleaned.length());
            for (char c : cleaned.toCharArray()) {
                sb.append(c <= 0xFF ? c : '?');
            }
            return sb.toString();
        }
    }
}
