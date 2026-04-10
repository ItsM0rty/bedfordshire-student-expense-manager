package com.studentexpensetracker.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyUtil {
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.UK);

    private MoneyUtil() {
    }

    public static String formatCurrency(BigDecimal anAmount) {
        if (anAmount == null) {
            return CURRENCY_FORMAT.format(0);
        }
        return CURRENCY_FORMAT.format(anAmount);
    }
}

