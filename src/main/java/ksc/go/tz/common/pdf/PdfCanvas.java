package ksc.go.tz.common.pdf;

import ksc.go.tz.common.LineItem;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A4 drawing surface shared by KSC's generated documents. Tracks the current page and vertical position
 * ({@link #y}) and starts a new page when content runs out of room.
 */
public final class PdfCanvas implements AutoCloseable {

    public static final Color ACCENT = new Color(11, 107, 99);
    public static final Color INK = new Color(20, 34, 40);
    public static final Color MUTED = new Color(86, 104, 109);
    public static final Color LINE = new Color(214, 223, 220);
    public static final Color HEADER_BG = new Color(234, 240, 238);

    public static final PDFont REGULAR = PDType1Font.HELVETICA;
    public static final PDFont BOLD = PDType1Font.HELVETICA_BOLD;

    public static final float MARGIN = 40;
    public static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    public static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    public static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;
    public static final float RIGHT = PAGE_WIDTH - MARGIN;
    public static final float BODY_SIZE = 10;
    public static final float ROW_LEADING = 13;

    // Line-item table columns as fractions of the content width: #, Description, Qty, Unit price, Amount.
    private static final float[] ITEM_COLUMNS = {0.06f, 0.50f, 0.08f, 0.18f, 0.18f};
    private static final float CELL_PADDING = 6;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    private final PDDocument pdf;
    private PDPageContentStream stream;

    /** Current baseline position, measured from the bottom of the page. */
    public float y;

    public PdfCanvas(PDDocument pdf) throws IOException {
        this.pdf = pdf;
        newPage();
    }

    // ------------------------------------------------------------------ primitives

    public void newPage() throws IOException {
        if (stream != null) {
            stream.close();
        }
        PDPage page = new PDPage(PDRectangle.A4);
        pdf.addPage(page);
        stream = new PDPageContentStream(pdf, page);
        y = PAGE_HEIGHT - MARGIN;
    }

    public void ensureSpace(float needed) throws IOException {
        if (y - needed < MARGIN) {
            newPage();
        }
    }

    public float width(String text, PDFont font, float size) throws IOException {
        return font.getStringWidth(sanitize(text, font)) / 1000 * size;
    }

    public void text(String text, PDFont font, float size, Color color, float x, float baseline) throws IOException {
        stream.beginText();
        stream.setFont(font, size);
        stream.setNonStrokingColor(color);
        stream.newLineAtOffset(x, baseline);
        stream.showText(sanitize(text, font));
        stream.endText();
    }

    public void textRight(String text, PDFont font, float size, Color color, float rightX, float baseline) throws IOException {
        text(text, font, size, color, rightX - width(text, font, size), baseline);
    }

    public void rule(float x1, float x2, float atY, Color color, float thickness) throws IOException {
        stream.setStrokingColor(color);
        stream.setLineWidth(thickness);
        stream.moveTo(x1, atY);
        stream.lineTo(x2, atY);
        stream.stroke();
    }

    public void fill(float x, float bottom, float w, float h, Color color) throws IOException {
        stream.setNonStrokingColor(color);
        stream.addRect(x, bottom, w, h);
        stream.fill();
    }

    @Override
    public void close() throws IOException {
        if (stream != null) {
            stream.close();
            stream = null;
        }
    }

    // ------------------------------------------------------------------ shared sections

    /** Company block on the left; title, number and meta lines (e.g. "Date: …") on the right; accent rule below. */
    public void header(CompanyDetails company, String title, String number, List<String> metaLines) throws IOException {
        float top = y;

        float ly = top - 16;
        text(company.getName(), BOLD, 16, ACCENT, MARGIN, ly);
        ly -= 15;
        for (String line : company.headerLines()) {
            text(line, REGULAR, 9, MUTED, MARGIN, ly);
            ly -= 12;
        }

        float ry = top - 20;
        textRight(title, BOLD, 22, INK, RIGHT, ry);
        ry -= 18;
        textRight(number, BOLD, 10, INK, RIGHT, ry);
        ry -= 16;
        for (String line : metaLines) {
            textRight(line, REGULAR, 9, MUTED, RIGHT, ry);
            ry -= 12;
        }

        y = Math.min(ly, ry) - 4;
        rule(MARGIN, RIGHT, y, ACCENT, 1.5f);
        y -= 26;
    }

    /** Small uppercase label followed by wrapped body lines. */
    public void labelledBlock(String label, List<String> lines, float x, float maxWidth) throws IOException {
        ensureSpace(14 + ROW_LEADING);
        text(label, BOLD, 8, MUTED, x, y);
        y -= 14;
        for (String line : lines) {
            for (String wrapped : wrap(line, REGULAR, BODY_SIZE, maxWidth)) {
                ensureSpace(ROW_LEADING);
                text(wrapped, REGULAR, BODY_SIZE, INK, x, y);
                y -= ROW_LEADING;
            }
        }
    }

    /** Two-column label/value rows, e.g. contract details. */
    public void keyValueRows(List<String[]> rows) throws IOException {
        float labelWidth = CONTENT_WIDTH * 0.30f;
        for (String[] row : rows) {
            List<String> value = wrap(row[1], REGULAR, BODY_SIZE, CONTENT_WIDTH - labelWidth - CELL_PADDING);
            float height = value.size() * ROW_LEADING + 8;
            ensureSpace(height);
            float baseline = y - BODY_SIZE;
            text(row[0], BOLD, 9, MUTED, MARGIN, baseline);
            float vy = baseline;
            for (String line : value) {
                text(line, REGULAR, BODY_SIZE, INK, MARGIN + labelWidth, vy);
                vy -= ROW_LEADING;
            }
            y -= height;
            rule(MARGIN, RIGHT, y + 3, LINE, 0.5f);
        }
    }

    /** Line-item table with a header row that repeats on every page. */
    public void itemsTable(List<LineItem> items) throws IOException {
        itemsHeader();
        int lineNo = 1;
        for (LineItem item : items) {
            List<String> description = wrap(item.getDescription(), REGULAR, BODY_SIZE, columnWidth(1) - 2 * CELL_PADDING);
            float rowHeight = description.size() * ROW_LEADING + 2 * CELL_PADDING;
            if (y - rowHeight < MARGIN + 40) {
                newPage();
                itemsHeader();
            }
            float baseline = y - CELL_PADDING - BODY_SIZE + 1;
            text(String.valueOf(lineNo++), REGULAR, BODY_SIZE, INK, columnX(0) + CELL_PADDING, baseline);
            float dy = baseline;
            for (String line : description) {
                text(line, REGULAR, BODY_SIZE, INK, columnX(1) + CELL_PADDING, dy);
                dy -= ROW_LEADING;
            }
            textRight(String.valueOf(item.getQuantity()), REGULAR, BODY_SIZE, INK, columnX(3) - CELL_PADDING, baseline);
            textRight(money(item.getUnitPrice()), REGULAR, BODY_SIZE, INK, columnX(4) - CELL_PADDING, baseline);
            textRight(money(item.getAmount()), REGULAR, BODY_SIZE, INK, RIGHT - CELL_PADDING, baseline);
            y -= rowHeight;
            rule(MARGIN, RIGHT, y, LINE, 0.75f);
        }
    }

    public void total(String label, BigDecimal amount) throws IOException {
        ensureSpace(40);
        y -= 4;
        rule(columnX(3), RIGHT, y, INK, 1);
        float baseline = y - 16;
        textRight(label, BOLD, BODY_SIZE, INK, columnX(4) - CELL_PADDING, baseline);
        textRight("TZS " + money(amount), BOLD, BODY_SIZE, ACCENT, RIGHT - CELL_PADDING, baseline);
        y = baseline - 26;
    }

    public void notes(List<String> notes) throws IOException {
        for (String note : notes) {
            for (String line : wrap(note, REGULAR, 9, CONTENT_WIDTH)) {
                ensureSpace(14);
                text(line, REGULAR, 9, MUTED, MARGIN, y);
                y -= 14;
            }
        }
    }

    public void sectionTitle(String title) throws IOException {
        ensureSpace(40);
        y -= 6;
        text(title, BOLD, 11, ACCENT, MARGIN, y);
        y -= 16;
    }

    public void footer(CompanyDetails company) throws IOException {
        String footer = company.footerLine();
        ensureSpace(40);
        y -= 20;
        float w = width(footer, REGULAR, 8);
        text(footer, REGULAR, 8, MUTED, MARGIN + (CONTENT_WIDTH - w) / 2, y);
    }

    private void itemsHeader() throws IOException {
        float height = 22;
        fill(MARGIN, y - height, CONTENT_WIDTH, height, HEADER_BG);
        float baseline = y - 14;
        text("#", BOLD, 8.5f, INK, columnX(0) + CELL_PADDING, baseline);
        text("Description", BOLD, 8.5f, INK, columnX(1) + CELL_PADDING, baseline);
        textRight("Qty", BOLD, 8.5f, INK, columnX(3) - CELL_PADDING, baseline);
        textRight("Unit price (TZS)", BOLD, 8.5f, INK, columnX(4) - CELL_PADDING, baseline);
        textRight("Amount (TZS)", BOLD, 8.5f, INK, RIGHT - CELL_PADDING, baseline);
        y -= height;
    }

    private static float columnWidth(int index) {
        return ITEM_COLUMNS[index] * CONTENT_WIDTH;
    }

    private static float columnX(int index) {
        float x = MARGIN;
        for (int i = 0; i < index; i++) {
            x += columnWidth(i);
        }
        return x;
    }

    // ------------------------------------------------------------------ text helpers

    public static String money(BigDecimal value) {
        if (value == null) {
            return "-";
        }
        return new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format(value);
    }

    public static String format(LocalDate date) {
        return date == null ? "-" : date.format(DATE);
    }

    /** Word-wraps text so each line fits within {@code maxWidth} points. */
    public static List<String> wrap(String text, PDFont font, float size, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String safe = sanitize(text, font);
        StringBuilder line = new StringBuilder();
        for (String word : safe.split(" ")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (line.isEmpty() || font.getStringWidth(candidate) / 1000 * size <= maxWidth) {
                line.setLength(0);
                line.append(candidate);
            } else {
                lines.add(line.toString());
                line.setLength(0);
                line.append(word);
            }
        }
        lines.add(line.toString());
        return lines;
    }

    /** Replaces characters the standard PDF fonts cannot encode, so user text never breaks rendering. */
    public static String sanitize(String text, PDFont font) {
        if (text == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(text.length());
        text.codePoints().forEach(cp -> {
            String ch = new String(Character.toChars(cp));
            try {
                font.encode(ch);
                out.append(ch);
            } catch (IllegalArgumentException | IOException e) {
                out.append('?');
            }
        });
        return out.toString();
    }
}
