package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseBackupUtil {
    private static final File DB_FILE = new File("nasigoreng71.db");

    public static File backupTo(File directory) throws IOException {
        if (!DB_FILE.exists()) throw new IOException("Database nasigoreng71.db tidak ditemukan.");
        if (!directory.exists()) directory.mkdirs();
        String name = "backup_nasigoreng71_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".db";
        File target = new File(directory, name);
        Files.copy(DB_FILE.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    public static void restoreFrom(File source) throws IOException {
        if (source == null || !source.exists()) throw new IOException("File backup tidak ditemukan.");
        Files.copy(source.toPath(), DB_FILE.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
