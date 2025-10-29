package analysis;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ChartService {

    /** Havayoluna göre min/avg/max bar chart (PNG) */
    public Path saveMinAvgMaxBar(Map<String, PriceAggregator.PriceStats> statsByAirline,
                                 Path outDir, String fileName, String title) throws IOException {

        Files.createDirectories(outDir);

        List<String> categories = new ArrayList<>(statsByAirline.keySet());
        List<Integer> mins = new ArrayList<>();
        List<Double> avgs = new ArrayList<>();
        List<Integer> maxs = new ArrayList<>();

        for (String a : categories) {
            var st = statsByAirline.get(a);
            mins.add(st.min);
            avgs.add(st.avg);
            maxs.add(st.max);
        }

        CategoryChart chart = new CategoryChartBuilder()
                .width(1100).height(650)
                .title(title)
                .xAxisTitle("Havayolu")
                .yAxisTitle("Fiyat (TRY)")
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        chart.getStyler().setHasAnnotations(true);
        chart.getStyler().setXAxisLabelRotation(20);

        chart.addSeries("Min", categories, mins);
        chart.addSeries("Ortalama", categories, avgs);
        chart.addSeries("Max", categories, maxs);

        Path png = outDir.resolve(fileName);
        BitmapEncoder.saveBitmap(chart, png.toString(), BitmapEncoder.BitmapFormat.PNG);
        return png;
    }

    /** Isı haritası: X = zaman dilimi label'ları, Y = havayolu; hücre = ortalama fiyat (TRY) */
    public Path saveHeatmap(double[][] avgMatrix, List<String> airlines,
                            List<String> slotLabels, Path outDir,
                            String fileName, String title) throws IOException {

        Files.createDirectories(outDir);

        HeatMapChart chart = new HeatMapChartBuilder()
                .width(1100).height(650)
                .title(title)
                .xAxisTitle("Zaman dilimi")
                .yAxisTitle("Havayolu")
                .build();

        // --- 1) Modern imza: List<Number[]> (NaN -> null) ---
        boolean done = false;
        try {
            // sanity
            int rows = avgMatrix.length;                 // airline sayısı
            int cols = rows == 0 ? 0 : avgMatrix[0].length; // slot sayısı
            if (cols != slotLabels.size()) {
                throw new IllegalArgumentException("slotLabels.size()=" + slotLabels.size() + " but matrix cols=" + cols);
            }
            if (rows != airlines.size()) {
                throw new IllegalArgumentException("airlines.size()=" + airlines.size() + " but matrix rows=" + rows);
            }

            List<Number[]> zData = new ArrayList<>(rows);
            for (int i = 0; i < rows; i++) {
                Number[] zr = new Number[cols];
                for (int j = 0; j < cols; j++) {
                    double v = avgMatrix[i][j];
                    zr[j] = (Double.isNaN(v) ? null : v); // NaN -> null
                }
                zData.add(zr);
            }

            // addSeries(String name, List<?> xKeys, List<?> yKeys, List<Number[]> zData)
            chart.addSeries("avg_price", slotLabels, airlines, zData);
            done = true;

        } catch (IllegalArgumentException | NoSuchMethodError | UnsupportedOperationException e) {
            // 3.8.0’a düş: int[] imzası
        }

        if (!done) {
            // --- 2) Eski imza: int[] xKeys, int[] yKeys, int[][] z ---
            int A = airlines.size();
            int S = slotLabels.size();

            int[] xKeys = new int[S];
            for (int s = 0; s < S; s++) xKeys[s] = s; // 0..S-1

            int[] yKeys = new int[A];
            for (int a = 0; a < A; a++) yKeys[a] = a; // 0..A-1

            int[][] z = new int[A][S];
            for (int i = 0; i < A; i++) {
                for (int j = 0; j < S; j++) {
                    double v = (i < avgMatrix.length && j < avgMatrix[i].length) ? avgMatrix[i][j] : Double.NaN;
                    z[i][j] = Double.isNaN(v) ? -1 : (int) Math.round(v); // NaN -> -1 (boş gibi)
                }
            }

            // addSeries(String name, int[] xKeys, int[] yKeys, int[][] zData)
            chart.addSeries("avg_price", xKeys, yKeys, z);

            // Eski imzada eksen etiketlerini sayıya çeker; label’ı açıklayıcı yapmak istersek:
            // X ekseninde "0..S-1" yerine slotLabels yazdırma desteği 3.8.0'da sınırlı.
            // En pratik: başlığa slot bilgisini ekliyoruz; gerekirse sürümü yükseltince gerçek etiketlere geçeriz.
        }

        Path png = outDir.resolve(fileName);
        BitmapEncoder.saveBitmap(chart, png.toString(), BitmapEncoder.BitmapFormat.PNG);
        return png;
    }

    /** slot sayısına göre 24 saati etiketlere çevirir (örn 6 → 00-04, 04-08, ...). */
    public List<String> buildSlotLabels(int slotCount) {
        List<String> labels = new ArrayList<>(slotCount);
        int minutesPerSlot = 24 * 60 / Math.max(1, slotCount);
        for (int s = 0; s < slotCount; s++) {
            int startM = s * minutesPerSlot;
            int endM = Math.min(24 * 60, (s + 1) * minutesPerSlot);
            labels.add(toHHmm(startM) + "-" + toHHmm(endM));
        }
        return labels;
    }

    private String toHHmm(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return String.format("%02d:%02d", h, m);
    }
}
