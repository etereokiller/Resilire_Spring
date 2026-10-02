package com.resilire.backend.prescription;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Rasterizes the Resilire brand SVG ({@code branding/resilire-logo.svg}) to PNG once, at 3x its
 * native 420x140 size for print sharpness, so it can be embedded into the prescription PDF header
 * without shipping a headless SVG renderer into every PDF-generation call.
 */
final class LogoImage {

    private static final int NATIVE_WIDTH = 420;
    private static final int NATIVE_HEIGHT = 140;
    private static final int SCALE = 3;

    private static volatile byte[] pngBytes;

    private LogoImage() {
    }

    static PDImageXObject load(PDDocument document) throws IOException {
        return PDImageXObject.createFromByteArray(document, pngBytesCached(), "resilire-logo");
    }

    private static byte[] pngBytesCached() throws IOException {
        byte[] cached = pngBytes;
        if (cached != null) {
            return cached;
        }
        synchronized (LogoImage.class) {
            if (pngBytes == null) {
                pngBytes = rasterize();
            }
            return pngBytes;
        }
    }

    private static byte[] rasterize() throws IOException {
        try (InputStream svg = LogoImage.class.getResourceAsStream("/branding/resilire-logo.svg")) {
            if (svg == null) {
                throw new IOException("branding/resilire-logo.svg not found on classpath");
            }
            PNGTranscoder transcoder = new PNGTranscoder();
            transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) (NATIVE_WIDTH * SCALE));
            transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) (NATIVE_HEIGHT * SCALE));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            transcoder.transcode(new TranscoderInput(svg), new TranscoderOutput(out));
            return out.toByteArray();
        } catch (TranscoderException e) {
            throw new IOException("Failed to rasterize Resilire logo SVG", e);
        }
    }
}
