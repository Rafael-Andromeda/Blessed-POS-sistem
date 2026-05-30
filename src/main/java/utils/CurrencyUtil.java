package utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtil {
    public static String formatRupiah(int amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        formatter.setMaximumFractionDigits(0);
        return formatter.format(amount).replace("Rp", "Rp ");
    }
}
