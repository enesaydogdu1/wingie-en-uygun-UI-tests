package analysis;

import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/** CSV'yi oku → özetle → görselleştir → en-uygunları yaz. Tek çağrıda hepsi. */
public class AnalysisRunner {

    private final CsvLoader csvLoader;
    private final PriceAggregator aggregator;
    private final ChartService chartService;
    private final ScoringService scoring;

    private final int slotCount;

    public AnalysisRunner(CsvLoader csvLoader,
                          PriceAggregator aggregator,
                          ChartService chartService,
                          ScoringService scoring,
                          int slotCount) {
        this.csvLoader = csvLoader;
        this.aggregator = aggregator;
        this.chartService = chartService;
        this.scoring = scoring;
        this.slotCount = slotCount;
    }

    /**
     * @param csvPath   DataExtractionTest’in ürettiği CSV
     * @param outDir    Sonuçların yazılacağı klasör (ör: exports/analysis/IST_ESB_20251113)
     * @param route     Başlık için "İstanbul → Lefkoşa" gibi
     * @param date      Başlık için tarih
     */
    public void run(Path csvPath, Path outDir, String route, LocalDate date) throws IOException, CsvException {
        // 1) Oku
        List<FlightCsvRow> rows = csvLoader.load(csvPath, slotCount);
        if (rows.isEmpty()) return;

        // 2) Özetler
        Map<String, PriceAggregator.PriceStats> stats = aggregator.summarizeByAirline(rows);
        List<String> airlines = aggregator.airlinesSorted(rows);

        // 3) Grafikler
        var slotLabels = chartService.buildSlotLabels(slotCount);
        double[][] matrix = aggregator.avgPriceMatrixByAirlineAndSlot(rows, airlines, slotCount);

        chartService.saveMinAvgMaxBar(
                stats,
                outDir,
                "min_max_avg_by_airline.png",
                route + " | " + date + " | Min/Ort/Max"
        );
        chartService.saveHeatmap(
                matrix, airlines, slotLabels, outDir,
                "heatmap_price_by_airline_timeslot.png",
                route + " | " + date + " | Saat Dilimine Göre Ortalama Fiyat"
        );

        // 4) En uygun maliyetli uçuşlar
        List<ScoringService.ScoredFlight> top = scoring.topN(rows, 15);
        writeTopCostEffectiveCsv(top, outDir.resolve("top_cost_effective.csv"));

        // 5) Özet tabloyu da CSV olarak bırak (havayoluna göre min/avg/max)
        writeSummaryCsv(stats, outDir.resolve("summary_stats.csv"));
    }

    // ------------ CSV yazıcılar ------------

    private void writeTopCostEffectiveCsv(List<ScoringService.ScoredFlight> top, Path out) throws IOException {
        List<String[]> rows = new ArrayList<>(top.size());
        for (var s : top) rows.add(ScoringService.toCsvRow(s));
        CsvExporter.writeFlights( // var olan yazarı kullanalım
                rows,
                out.getParent(),
                out.getFileName().toString(),
                true // header
        );
    }

    private void writeSummaryCsv(Map<String, PriceAggregator.PriceStats> stats, Path out) throws IOException {
        List<String[]> rows = new ArrayList<>();
        for (var e : stats.entrySet()) {
            var st = e.getValue();
            rows.add(new String[]{
                    st.airline,
                    String.valueOf(st.count),
                    String.valueOf(st.min),
                    String.format(Locale.US, "%.2f", st.avg),
                    String.valueOf(st.max)
            });
        }
        // küçük bir header farkı:
        analysis.CsvExporter.writeFlights(
                prependHeader(rows, new String[]{"airline","count","min","avg","max"}),
                out.getParent(),
                out.getFileName().toString(),
                false // header'ı biz ekledik
        );
    }

    private static List<String[]> prependHeader(List<String[]> rows, String[] header) {
        List<String[]> all = new ArrayList<>(rows.size() + 1);
        all.add(header);
        all.addAll(rows);
        return all;
    }
}
