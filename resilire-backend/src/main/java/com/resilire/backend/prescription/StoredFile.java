package com.resilire.backend.prescription;

record StoredFile(String originalFileName, String contentType, long size, String storagePath) {
}
