package com.resilire.backend.prescription.dto;

public record DownloadableFile(byte[] content, String contentType, String fileName) {
}
