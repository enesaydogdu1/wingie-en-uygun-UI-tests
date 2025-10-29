package analysis;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.*;

/** flights CSV'sini (delimiter=';') okuyup FlightCsvRow listesine çevirir. */
public class CsvLoader {

    /** CSV'yi okur ve satırları parse eder. */
    public List<FlightCsvRow> load(Path csvPath, int slotCount) throws IOException, CsvException {
        try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            // UTF-8 BOM temizle
            br.mark(1);
            if (br.read() != '\uFEFF') br.reset();

            CSVReader reader = new CSVReaderBuilder(br)
                    .withSkipLines(0)
                    .withCSVParser(new com.opencsv.CSVParserBuilder()
                            .withSeparator(';')
                            .withIgnoreLeadingWhiteSpace(true)
                            .withIgnoreQuotations(false)
                            .build())
                    .build();

            List<String[]> all = reader.readAll();
            if (all.isEmpty()) return List.of();

            // Başlıkları sütun ismine göre bul (esnek sıraya izin ver)
            Map<String, Integer> idx = mapHeaderIndexes(all.get(0));
            List<FlightCsvRow> out = new ArrayList<>();

            for (int i = 1; i < all.size(); i++) {
                String[] r = all.get(i);
                if (r == null || r.length == 0) continue;

                String dep   = get(r, idx, "departure_time");
                String arr   = get(r, idx, "arrival_time");
                String al    = get(r, idx, "airline");
                String price = get(r, idx, "price");
                String cur   = get(r, idx, "currency");   // olabilir/olmayabilir
                String dur   = get(r, idx, "duration");
                String st    = get(r, idx, "stops");

                int priceInt   = FlightCsvRow.parsePrice(price);
                String curr    = FlightCsvRow.normalizeCurrency(cur);
                int duration   = FlightCsvRow.parseDurationToMin(dur);
                int stopsCount = FlightCsvRow.parseStops(st);
                int slot       = FlightCsvRow.timeToSlot(dep, Math.max(1, slotCount));

                out.add(new FlightCsvRow(dep, arr, al, priceInt, curr, duration, stopsCount, slot));
            }
            return out;
        }
    }

    // ---------- helpers ----------

    private Map<String, Integer> mapHeaderIndexes(String[] headerRow) {
        Map<String, Integer> m = new HashMap<>();
        for (int i = 0; i < headerRow.length; i++) {
            String h = (headerRow[i] == null ? "" : headerRow[i]).trim().toLowerCase(Locale.ROOT);
            // olası varyasyonları destekle
            switch (h) {
                case "departure_time" -> m.put("departure_time", i);
                case "arrival_time"   -> m.put("arrival_time", i);
                case "airline"        -> m.put("airline", i);
                case "price"          -> m.put("price", i);
                case "currency"       -> m.put("currency", i);
                case "duration"       -> m.put("duration", i);
                case "stops"          -> m.put("stops", i);
                default -> { /* ignore */ }
            }
        }
        return m;
    }

    private String get(String[] row, Map<String, Integer> idx, String key) {
        Integer i = idx.get(key);
        if (i == null || i < 0 || i >= row.length) return "";
        String v = row[i];
        return v == null ? "" : v.trim();
    }
}
