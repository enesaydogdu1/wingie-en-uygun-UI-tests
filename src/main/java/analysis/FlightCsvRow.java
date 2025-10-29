package analysis;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/** CSV'deki tek satırı temsil eder ve metinleri sayısal alanlara dönüştürür. */
public class FlightCsvRow {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    public final String departureTime;   // "06:45"
    public final String arrivalTime;     // "07:55"
    public final String airline;         // "Pegasus"
    public final int    price;           // 838 (TRY)
    public final String currency;        // "TRY" veya "TL"
    public final int    durationMin;     // 1sa 30dk -> 90
    public final int    stopsCount;      // "Direkt Uçuş" -> 0, "2 Aktarma" -> 2
    public final boolean isDirect;       // stopsCount == 0
    public final int    timeSlot;        // gün içi zaman dilimi index'i (0..slotCount-1)

    public FlightCsvRow(String departureTime,
                        String arrivalTime,
                        String airline,
                        int price,
                        String currency,
                        int durationMin,
                        int stopsCount,
                        int timeSlot) {
        this.departureTime = departureTime;
        this.arrivalTime   = arrivalTime;
        this.airline       = airline;
        this.price         = price;
        this.currency      = currency == null ? "" : currency.trim();
        this.durationMin   = durationMin;
        this.stopsCount    = stopsCount;
        this.isDirect      = stopsCount == 0;
        this.timeSlot      = timeSlot;
    }

    // ---------- statik parser yardımcıları ----------

    /** "1sa 35dk" → 95, "2sa"→120, "50dk"→50 */
    public static int parseDurationToMin(String s) {
        if (s == null) return 0;
        String t = s.toLowerCase(new Locale("tr", "TR")).replaceAll("\\s+", "");
        int h = 0, m = 0;
        // saat
        var mh = java.util.regex.Pattern.compile("(\\d+)\\s*sa").matcher(t);
        if (mh.find()) h = Integer.parseInt(mh.group(1));
        // dakika
        var mm = java.util.regex.Pattern.compile("(\\d+)\\s*dk").matcher(t);
        if (mm.find()) m = Integer.parseInt(mm.group(1));
        if (h == 0 && m == 0) {
            // "1g 2sa 5dk" gibi beklenmeyen bir değer gelirse; yalnızca sayıları topla (emin değilsek)
            var numbers = java.util.regex.Pattern.compile("\\d+").matcher(t);
            int sum = 0; while (numbers.find()) sum += Integer.parseInt(numbers.group());
            return sum; // worst-case
        }
        return h * 60 + m;
    }

    /** "Direkt Uçuş"->0, "1 Aktarma"->1, "2 Aktarma"->2 ... */
    public static int parseStops(String s) {
        if (s == null || s.isBlank()) return 0;
        String t = s.toLowerCase(new Locale("tr", "TR")).trim();
        if (t.contains("direkt")) return 0;
        var m = java.util.regex.Pattern.compile("(\\d+)\\s*aktarma").matcher(t);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    /** "19:05" → slot index. slotCount=6 ise 4 saatlik bloklar (00-04,04-08,...) */
    public static int timeToSlot(String hhmm, int slotCount) {
        try {
            LocalTime lt = LocalTime.parse(hhmm, HH_MM);
            int minutes = lt.getHour() * 60 + lt.getMinute();
            int bucketSize = (24 * 60) / Math.max(1, slotCount);
            return Math.min(slotCount - 1, minutes / bucketSize);
        } catch (Exception e) {
            return 0;
        }
    }

    /** "TL"/"TRY" normalize. Boşsa "TRY". */
    public static String normalizeCurrency(String s) {
        if (s == null || s.isBlank()) return "TRY";
        String t = s.trim().toUpperCase(Locale.ROOT);
        if (Objects.equals(t, "TL")) return "TRY";
        return t;
    }

    /** "838", "1.010", "1 010" → 838/1010. */
    public static int parsePrice(String s) {
        if (s == null) return 0;
        String digits = s.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }
}
