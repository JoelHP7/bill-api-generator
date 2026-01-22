package com.bill_api_generator.bill_api_generator.constants;

public final class InvoiceStyle {
    private InvoiceStyle() {}
    // Tipografía
    public static final String FONT = "Calibri";       // cámbialo si tu doc usa otra
    public static final int FONT_SIZE = 11;
    public static final int FONT_SIZE_TITLE = 14;

    // Colores (hex RGB sin # para DOCX)
    public static final String COLOR_BG_EMISOR = "F2F2F2";
    public static final String COLOR_BG_PARA   = "E8F3FF";
    public static final String COLOR_BG_TH     = "F2F2F2";

    // Márgenes y tab stops (DOCX usa twips: 1pt=20 twips, 1cm~567 twips)
    public static final int PAGE_LEFT_TWIPS = 1134;  // ~2cm
    public static final int PAGE_RIGHT_TWIPS = 1134; // ~2cm
    public static final int TAB_RIGHT_TWIPS = 9000;  // posición del tab de totales a la derecha
}
