package utils;

import dao.TransactionDAO;
import model.Transaction;
import model.TransactionDetail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CSVExporter {
    private static final String DELIMITER = ";";

    public static void exportTransactions(File file, List<Transaction> transactions) throws IOException {
        TransactionDAO dao = new TransactionDAO();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write('\uFEFF');
            writer.write("Tanggal;ID Transaksi;Nama Menu;Kategori;Jumlah;Harga Satuan;Subtotal;Metode Pembayaran;Total Transaksi");
            writer.newLine();
            for (Transaction t : transactions) {
                List<TransactionDetail> details = dao.findDetails(t.getIdTransaksi());
                if (details.isEmpty()) {
                    writeRow(writer, t.getTanggal(), t.getKodeTransaksi(), "", "", "0", "0", "0", t.getMetodePembayaran(), String.valueOf(t.getTotal()));
                } else {
                    for (TransactionDetail d : details) {
                        writeRow(writer,
                                t.getTanggal(),
                                t.getKodeTransaksi(),
                                d.getNamaMenu(),
                                d.getKategori(),
                                String.valueOf(d.getQty()),
                                String.valueOf(d.getHarga()),
                                String.valueOf(d.getTotal()),
                                t.getMetodePembayaran(),
                                String.valueOf(t.getTotal()));
                    }
                }
            }
        }
    }

    private static void writeRow(BufferedWriter writer, String... values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) writer.write(DELIMITER);
            writer.write(csv(values[i]));
        }
        writer.newLine();
    }

    private static String csv(String value) {
        if (value == null) return "";
        String safe = value.replace("\"", "\"\"");
        if (safe.contains(DELIMITER) || safe.contains(",") || safe.contains("\"") || safe.contains("\n") || safe.contains("\r")) {
            return "\"" + safe + "\"";
        }
        return safe;
    }
}
