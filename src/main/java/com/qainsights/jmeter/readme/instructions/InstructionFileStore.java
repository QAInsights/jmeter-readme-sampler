package com.qainsights.jmeter.readme.instructions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;

public class InstructionFileStore {

    private static final byte[] UTF_8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    public LoadedInstructionFile load(DetectedInstructionFile file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.path());
        boolean byteOrderMark = hasByteOrderMark(bytes);
        byte[] contentBytes = byteOrderMark ? Arrays.copyOfRange(bytes, UTF_8_BOM.length, bytes.length) : bytes;
        String content = new String(contentBytes, StandardCharsets.UTF_8);
        return new LoadedInstructionFile(file, content, detectLineSeparator(content), byteOrderMark,
                revision(bytes), Files.isWritable(file.path()));
    }

    public LoadedInstructionFile save(LoadedInstructionFile loaded, String content) throws IOException {
        Path path = loaded.file().path();
        if (!Files.isRegularFile(path) || !revision(Files.readAllBytes(path)).equals(loaded.revision())) {
            throw new InstructionFileChangedException(path);
        }
        String normalized = applyLineSeparator(content, loaded.lineSeparator());
        byte[] contentBytes = normalized.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = loaded.byteOrderMark() ? withByteOrderMark(contentBytes) : contentBytes;
        atomicWrite(path, bytes);
        return load(loaded.file());
    }

    public LoadedInstructionFile create(Path path, InstructionFileKind kind, InstructionFileSource source)
            throws IOException {
        Path absolute = path.toAbsolutePath().normalize();
        Files.write(absolute, new byte[0], StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        return load(new DetectedInstructionFile(absolute, kind, source));
    }

    private void atomicWrite(Path path, byte[] bytes) throws IOException {
        Path temporary = Files.createTempFile(path.getParent(), "." + path.getFileName(), ".tmp");
        try {
            Files.write(temporary, bytes, StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private String applyLineSeparator(String content, String lineSeparator) {
        String normalized = content.replace("\r\n", "\n").replace('\r', '\n');
        return "\n".equals(lineSeparator) ? normalized : normalized.replace("\n", lineSeparator);
    }

    private String detectLineSeparator(String content) {
        if (content.contains("\r\n")) {
            return "\r\n";
        }
        if (content.contains("\n")) {
            return "\n";
        }
        if (content.contains("\r")) {
            return "\r";
        }
        return System.lineSeparator();
    }

    private boolean hasByteOrderMark(byte[] bytes) {
        return bytes.length >= UTF_8_BOM.length
                && bytes[0] == UTF_8_BOM[0]
                && bytes[1] == UTF_8_BOM[1]
                && bytes[2] == UTF_8_BOM[2];
    }

    private byte[] withByteOrderMark(byte[] bytes) {
        byte[] result = new byte[UTF_8_BOM.length + bytes.length];
        System.arraycopy(UTF_8_BOM, 0, result, 0, UTF_8_BOM.length);
        System.arraycopy(bytes, 0, result, UTF_8_BOM.length, bytes.length);
        return result;
    }

    private String revision(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
