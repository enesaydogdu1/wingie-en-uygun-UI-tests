package tests;

import base.BaseTest;
import io.qameta.allure.*;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.ResultsPage;
import utils.AssertionsHelper;
import utils.ConfigReader;

import java.util.List;

@Epic("Flight Search")
@Feature("Price Sorting + Airline Verification")
@Owner("Enes AYDOGDU")
public class TC2_PriceSortByAirlineTest extends BaseTest {

    @Severity(SeverityLevel.CRITICAL)
    @Story("Price Sorting for Turkish Airlines")
    @Description("""
           1- Perform a round-trip flight search (cities and dates are parameterized)
           2- Apply departure time filter
           3- Filter by Turkish Airlines only
           
              Verify that:
              - Prices are sorted in ascending order
              - All displayed flights belong to Turkish Airlines
              - Sorting accuracy is correct
           """)
    @Test(description = "Validate price sorting functionality for Turkish Airlines")
    public void priceSortForTHY() {
        // ---- Read parameters from config.properties
        String url                 = ConfigReader.get("baseUrl");
        String from                = ConfigReader.get("fromCity");
        String to                  = ConfigReader.get("toCity");
        String departDate          = ConfigReader.get("departDate");   // yyyy-MM-dd
        String returnDate          = ConfigReader.get("returnDate");
        String departureTimeStart  = ConfigReader.get("departureTimeStart");
        String departureTimeEnd    = ConfigReader.get("departureTimeEnd");

        // ---- Allure parameters
        Allure.parameter("Base URL", url);
        Allure.parameter("From", from);
        Allure.parameter("To", to);
        Allure.parameter("Depart Date", departDate);
        Allure.parameter("Return Date", returnDate);
        Allure.parameter("Departure Time Range", departureTimeStart + "–" + departureTimeEnd);

        // ---- Steps
        Allure.step("Navigate to the homepage, configure round-trip, choose cities and dates, then start the search", () -> {
            new HomePage(driver, explicitWaitSec)
                    .goTo(url)
                    .ensureRoundTrip()
                    .setFrom(from)
                    .setTo(to)
                    .setDates(departDate, returnDate)
                    .search();
        });

        ResultsPage results = Allure.step(
                "Wait for results, apply time filter, select Turkish Airlines only, then sort by price (ascending)",
                () -> new ResultsPage(driver, explicitWaitSec)
                        .waitForResults()
                        .applyDepartureTimeFilter(departureTimeStart, departureTimeEnd)
                        .filterOnlyTHY()
                        .sortByPriceAscending()
        );

        List<Integer> prices  = Allure.step("Collect all visible prices from the result cards", results::getAllPrices);
        List<String> airlines = Allure.step("Collect all visible airline names from the result cards", results::getAllAirlines);

        // ---- Assertions
        Allure.step("Verify that prices are sorted in ascending order", () ->
                AssertionsHelper.assertPricesSortedAscending(prices)
        );

        // Only show a single sample airline in the step text (not the whole list)
        String sampleAirline = airlines.isEmpty() ? "—" : airlines.get(0);
        Allure.step(String.format("Verify that all flights are '%s'", sampleAirline), () ->
                AssertionsHelper.assertAllAirlinesAreSelected(airlines)
        );
    }
}
