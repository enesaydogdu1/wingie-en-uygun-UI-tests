package tests;

import base.BaseTest;
import io.qameta.allure.*;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.ResultsPage;
import utils.AssertionsHelper;
import utils.ConfigReader;
import analysis.CsvExporter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@Epic("Analysis & Categorization")
@Feature("Data Extraction and Analysis (CSV + Charts)")
@Owner("Enes AYDOGDU")
public class TC4_FlightDataExtractionAndAnalysisTest extends BaseTest {

    @Severity(SeverityLevel.NORMAL)
    @Story("Extract, persist, and analyze flight search results")
    @Description("""
        Case 4: Analysis and Categorization
        Develop a test for extracting and analyzing data from search results:
        • Perform a flight search on Enuygun.com for a parameterized route (from → to)
        • Extract the following data for all flights and save to CSV:
          - Departure/arrival times
          - Airline name
          - Price
          - Connection information
          - Flight duration
        With the collected data:
        • Calculate and display min/max/average prices by airline (chart)
        • Visualize price distribution across time slots (heatmap)
        • Identify most cost-effective flights via a scoring algorithm
        The test must be repeatable for different dates/routes to enable comparisons.
        """)
    @Test(description = "Extract all flights to CSV and produce charts/analytics for further insights")
    public void extractToCsvAndAnalyze() throws Exception {
        // -------- Read parameters from config.properties
        String baseUrl   = ConfigReader.get("baseUrl");
        String from      = ConfigReader.get("analysis.route.from");      // e.g., Istanbul
        String to        = ConfigReader.get("analysis.route.to");        // e.g., Nicosia
        String depart    = ConfigReader.get("analysis.departDate");      // yyyy-MM-dd
        String ret       = ConfigReader.get("analysis.returnDate");      // yyyy-MM-dd

        // Output base (default to target/analysis)
        String outputBase = ConfigReader.getOrDefault("analysis.outputBase", "target");
        String exportDir  = ConfigReader.getOrDefault("analysis.exportDir", outputBase);

        // Analysis knobs (defaults provided)
        int slotCount         = ConfigReader.getIntOrDefault("analysis.slotCount", 6);
        int weightAlphaTlPerMin = ConfigReader.getIntOrDefault("analysis.weight.alphaTlPerMin", 3);   // TL per minute
        int weightStopPenalty   = ConfigReader.getIntOrDefault("analysis.weight.stopPenalty", 250);   // TL per connection
        int directBonus         = ConfigReader.getIntOrDefault("analysis.weight.directBonus", 50);    // TL bonus for direct

        // -------- Allure parameters for reporting
        Allure.parameter("Base URL", baseUrl);
        Allure.parameter("Route", from + " → " + to);
        Allure.parameter("Depart Date", depart);
        Allure.parameter("Return Date", ret);
        Allure.parameter("Output Base", outputBase);
        Allure.parameter("Export Dir", exportDir);
        Allure.parameter("Slot Count", String.valueOf(slotCount));
        Allure.parameter("Weights", "alpha=" + weightAlphaTlPerMin + " TL/min, stop=" + weightStopPenalty + ", directBonus=" + directBonus);

        // -------- Test steps
        Allure.step("Navigate to homepage, configure round-trip with given route/dates, then start search", () -> {
            new HomePage(driver, explicitWaitSec)
                    .goTo(baseUrl)
                    .ensureRoundTrip()
                    .setFrom(from)
                    .setTo(to)
                    .ensureHotelsListUnchecked()
                    .setDates(depart, ret)
                    .search();
        });

        ResultsPage results = Allure.step("Wait for flight results to load", () ->
                new ResultsPage(driver, explicitWaitSec).waitForResults()
        );

        // Extract flight rows: [dep, arr, airline, price, currency, duration, stops]
        List<String[]> rows = Allure.step("Extract all flights from search results (rows)", results::extractFlightRows);

        AssertionsHelper.mustTrue(!rows.isEmpty(), "No rows were collected from the results.");

        // Ensure export directory exists
        Path exportPath = Path.of(exportDir);
        if (!Files.exists(exportPath)) {
            Files.createDirectories(exportPath);
        }

        String csvName = "flights_" + from + "_" + to + ".csv";
        Path exported = CsvExporter.writeFlights(
                rows,
                exportPath,
                csvName,
                true
        );
        log.info("CSV written: {}", exported.toAbsolutePath());

        // Build report directory under output base with route + today's date for comparability
        Path reportDir = Path.of(outputBase, "analysis",
                from + "_" + to + "_" + LocalDate.now());

        // Instantiate analysis services
        var loader  = new analysis.CsvLoader();
        var agg     = new analysis.PriceAggregator();
        var charts  = new analysis.ChartService();
        var scoring = new analysis.ScoringService(weightAlphaTlPerMin, weightStopPenalty, directBonus);

        // Run end-to-end analysis pipeline
        Path csvPath = exportPath.resolve(csvName);
        new analysis.AnalysisRunner(loader, agg, charts, scoring, slotCount)
                .run(csvPath, reportDir, from + " → " + to, LocalDate.now());

        log.info("Analysis completed. Outputs under: {}", reportDir.toAbsolutePath());
    }
}
