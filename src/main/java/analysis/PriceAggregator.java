package analysis;

import java.util.*;
import java.util.stream.Collectors;

/** Havayoluna göre min/max/avg fiyat özetleri ve heatmap verisi üretir. */
public class PriceAggregator {

    /** Tek havayolu için özet istatistik */
    public static class PriceStats {
        public final String airline;
        public final int count;
        public final int min;
        public final int max;
        public final double avg;

        public PriceStats(String airline, int count, int min, int max, double avg) {
            this.airline = airline;
            this.count = count;
            this.min = min;
            this.max = max;
            this.avg = avg;
        }
    }

    /** Havayoluna göre min/max/avg hesapla. */
    public Map<String, PriceStats> summarizeByAirline(List<FlightCsvRow> rows) {
        Map<String, List<FlightCsvRow>> byAirline = rows.stream()
                .collect(Collectors.groupingBy(r -> safe(r.airline)));

        Map<String, PriceStats> out = new LinkedHashMap<>();
        byAirline.forEach((airline, list) -> {
            int count = list.size();
            int min = list.stream().mapToInt(r -> r.price).min().orElse(0);
            int max = list.stream().mapToInt(r -> r.price).max().orElse(0);
            double avg = list.stream().mapToInt(r -> r.price).average().orElse(0.0);
            out.put(airline, new PriceStats(airline, count, min, max, avg));
        });

        // Havayolu adlarına göre deterministik sırala
        return out.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a,b) -> a, LinkedHashMap::new));
    }

    /** Heatmap için: Y=airline listesi, X=slot(0..slotCount-1). Hücre=ortalama fiyat (yoksa NaN). */
    public double[][] avgPriceMatrixByAirlineAndSlot(List<FlightCsvRow> rows,
                                                     List<String> airlinesInOrder,
                                                     int slotCount) {
        int A = airlinesInOrder.size();
        double[][] sum = new double[A][slotCount];
        int[][] cnt = new int[A][slotCount];

        Map<String, Integer> aIndex = new HashMap<>();
        for (int i = 0; i < A; i++) aIndex.put(airlinesInOrder.get(i), i);

        for (FlightCsvRow r : rows) {
            String a = safe(r.airline);
            Integer ai = aIndex.get(a);
            if (ai == null) continue;
            int s = Math.max(0, Math.min(slotCount - 1, r.timeSlot));
            sum[ai][s] += r.price;
            cnt[ai][s] += 1;
        }

        double[][] avg = new double[A][slotCount];
        for (int i = 0; i < A; i++) {
            for (int s = 0; s < slotCount; s++) {
                avg[i][s] = cnt[i][s] == 0 ? Double.NaN : sum[i][s] / cnt[i][s];
            }
        }
        return avg;
    }

    /** Mevcut veriden havayollarını deterministik sırayla döndür. */
    public List<String> airlinesSorted(List<FlightCsvRow> rows) {
        return rows.stream()
                .map(r -> safe(r.airline))
                .filter(s -> !s.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
}
