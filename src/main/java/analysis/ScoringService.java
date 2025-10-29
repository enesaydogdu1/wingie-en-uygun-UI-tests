package analysis;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/** Fiyat + süre + aktarma sayısına göre maliyet skoru hesaplar ve ilk N uçuşu döndürür. */
public class ScoringService {

    // skor = price + α*durationMin + β*stopsCount - γ*(isDirect?1:0)
    private final int alphaPerMinute;   // dakika başına ceza (TL)
    private final int stopPenalty;      // her aktarma için ceza (TL)
    private final int directBonus;      // direkt uçuş bonusu (TL)

    public ScoringService(int alphaPerMinute, int stopPenalty, int directBonus) {
        this.alphaPerMinute = alphaPerMinute;
        this.stopPenalty = stopPenalty;
        this.directBonus = directBonus;
    }

    public double score(FlightCsvRow r) {
        return r.price
                + alphaPerMinute * r.durationMin
                + stopPenalty * r.stopsCount
                - (r.isDirect ? directBonus : 0);
    }

    /** Skorları ekleyip artan sırada sıralar. */
    public List<ScoredFlight> rank(List<FlightCsvRow> rows) {
        return rows.stream()
                .map(r -> new ScoredFlight(r, score(r)))
                .sorted(Comparator.comparingDouble(sf -> sf.score))
                .collect(Collectors.toList());
    }

    /** İlk N sonucu döndür. */
    public List<ScoredFlight> topN(List<FlightCsvRow> rows, int n) {
        List<ScoredFlight> all = rank(rows);
        return all.subList(0, Math.min(n, all.size()));
    }

    /** CSV yazımı için satır dizisi üretir (header ile uyumlu). */
    public static String[] toCsvRow(ScoredFlight s) {
        FlightCsvRow r = s.row;
        return new String[] {
                r.departureTime,
                r.arrivalTime,
                r.airline,
                String.valueOf(r.price),
                r.currency,
                String.valueOf(r.durationMin),
                String.valueOf(r.stopsCount),
                String.valueOf(s.score)
        };
    }

    /** En üst seviyede taşımak için küçük DTO. */
    public static class ScoredFlight {
        public final FlightCsvRow row;
        public final double score;
        public ScoredFlight(FlightCsvRow row, double score) {
            this.row = row; this.score = score;
        }
    }
}
