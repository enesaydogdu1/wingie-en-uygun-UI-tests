package analysis;

import com.opencsv.CSVWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * CSV yazıcı — UTF-8 BOM + ';' ayraç (Excel TR uyumu)
 */
public class CsvExporter {

    /**
     * Satırları CSV'ye yazar.
     * Eğer includeHeader=true ise, header şu sıradadır:
     *   departure_time, arrival_time, airline, price, currency, duration, stops, score
     */
    public static Path writeFlights(List<String[]> rows, Path exportDir,
                                    String fileName, boolean includeHeader) throws IOException {
        Files.createDirectories(exportDir);
        Path out = exportDir.resolve(fileName);

        try (OutputStream os = new FileOutputStream(out.toFile());
             OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
             CSVWriter writer = new CSVWriter(
                     osw,
                     ';',                                     // ayraç
                     CSVWriter.DEFAULT_QUOTE_CHARACTER,
                     CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                     CSVWriter.DEFAULT_LINE_END)) {

            // UTF-8 BOM (Excel Türkçe karakterler için)
            osw.write('\uFEFF');

            if (includeHeader) {
                writer.writeNext(new String[]{
                        "departure_time","arrival_time","airline","price","currency","duration","stops","score"
                });
            }

            for (String[] r : rows) {
                // null hücreleri boş string yap
                if (r != null) {
                    for (int i = 0; i < r.length; i++) {
                        if (r[i] == null) r[i] = "";
                    }
                }
                writer.writeNext(r);
            }
        }
        return out;
    }
}
