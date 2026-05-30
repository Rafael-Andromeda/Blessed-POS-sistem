package utils;

import model.Transaction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVExporter {
    public static void exportTransactions(File file, List<Transaction> transactions) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Kode Transaksi,Tanggal,Kasir,Items,Tipe Order,Metode Pembayaran,Subtotal,Diskon,Total\n");
            for (Transaction t : transactions) {
                writer.write(csv(t.getKodeTransaksi()) + "," +
                        csv(t.getTanggal()) + "," +
                        csv(t.getKasir()) + "," +
                        csv(t.getItems()) + "," +
                        csv(t.getTipeOrder()) + "," +
                        csv(t.getMetodePembayaran()) + "," +
                        t.getSubtotal() + "," +
                        t.getDiskon() + "," +
                        t.getTotal() + "\n");
            }
        }
    }

    private static String csv(String value) {
        if (value == null) return "";
        String safe = value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }
}
