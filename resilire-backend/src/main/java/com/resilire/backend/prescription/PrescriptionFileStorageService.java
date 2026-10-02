package com.resilire.backend.prescription;

import com.resilire.backend.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * Stores patient-uploaded offline prescription files (Release 5) on local disk under
 * {@code resilire.storage.prescriptions-dir}. Stored file names are random UUIDs, decoupled
 * from the patient-supplied original name, which is kept only in the {@code Prescription} row
 * for display/download purposes.
 */
@Service
public class PrescriptionFileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("application/pdf", "image/jpeg", "image/png");

    private final Path rootDir;
    private final long maxFileSizeBytes;

    public PrescriptionFileStorageService(
            @Value("${resilire.storage.prescriptions-dir:uploads/prescriptions}") String dir,
            @Value("${resilire.storage.prescriptions-max-size-bytes:10485760}") long maxFileSizeBytes) {
        this.rootDir = Paths.get(dir).toAbsolutePath().normalize();
        this.maxFileSizeBytes = maxFileSizeBytes;
        try {
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create prescription upload directory", e);
        }
    }

    StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("error.prescription.fileRequired");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new IllegalArgumentException("error.prescription.fileTooLarge");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("error.prescription.fileTypeNotAllowed");
        }

        String storedFileName = UUID.randomUUID() + extensionFor(contentType);
        Path target = rootDir.resolve(storedFileName);
        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store prescription upload", e);
        }
        return new StoredFile(sanitizeFileName(file.getOriginalFilename()), contentType, file.getSize(), storedFileName);
    }

    byte[] load(String storedFileName) {
        Path target = rootDir.resolve(storedFileName).normalize();
        if (!target.startsWith(rootDir)) {
            throw new IllegalArgumentException("error.prescription.invalidFile");
        }
        try {
            return Files.readAllBytes(target);
        } catch (IOException e) {
            throw new ResourceNotFoundException("error.prescription.fileNotFound");
        }
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "application/pdf" -> ".pdf";
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            default -> "";
        };
    }

    private String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) {
            return "prescription";
        }
        return Paths.get(name).getFileName().toString();
    }
}
