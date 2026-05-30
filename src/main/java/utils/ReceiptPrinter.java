package utils;

import model.CartItem;
import model.Transaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ReceiptPrinter {
    public static Path saveReceipt(Transaction trx, List<CartItem> items) throws IOException {
        Path dir = Path.of("receipts");
        Files.createDirectories(dir);
        Path file = dir.resolve(trx.getKodeTransaksi() + ".txt");
        Files.writeString(file, buildReceipt(trx, items));
        return file;
    }

    public static String buildReceipt(Transaction trx, List<CartItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("NASIGORENG 71\n");
        sb.append("Warung Makan\n\n");
        sb.append(String.format("No Transaksi : %s\n", trx.getKodeTransaksi()));
        sb.append(String.format("Tanggal      : %s\n", trx.getTanggal()));
        sb.append(String.format("Kasir        : %s\n", trx.getKasir()));
        sb.append(String.format("Tipe         : %s\n", trx.getTipeOrder()));
        sb.append(String.format("Pembayaran   : %s\n", trx.getMetodePembayaran()));
        sb.append("--------------------------------\n");
        for (CartItem item : items) {
            sb.append(String.format("%-16s x%-3d %10s\n", item.getNamaMenu(), item.getQty(), CurrencyUtil.formatRupiah(item.getTotal())));
        }
        sb.append("--------------------------------\n");
        sb.append(String.format("%-22s %10s\n", "Subtotal", CurrencyUtil.formatRupiah(trx.getSubtotal())));
        sb.append(String.format("%-22s %10s\n", "Promo", CurrencyUtil.formatRupiah(trx.getDiskon())));
        sb.append(String.format("%-22s %10s\n", "Total", CurrencyUtil.formatRupiah(trx.getTotal())));
        sb.append("\nTerima kasih\n");
        return sb.toString();
    }
}
