package tests;

import base.BaseTest;
import io.qameta.allure.*;
import org.testng.annotations.*;
import pages.HomePage;
import pages.ResultsPage;
import utils.AssertionsHelper;
import utils.ConfigReader;

import java.util.List;

@Epic("Flight Search")
@Feature("Round Trip + Time Filter")
@Owner("Enes AYDOGDU")                  // Allure owner
public class TC1_FlightSearchTest extends BaseTest {

    @Severity(SeverityLevel.CRITICAL)
    @Story("Basic Flight Search and Time Filter")
    @Description("""
      1- Navigate to www.enuygun.com
      2- Search for a round-trip flight between Istanbul and Ankara
      3- Departure and return dates should be parameterized
      4- Cities should be parameterized
      5- On the flight listing page:
      6- Apply departure time filter (10:00 AM- 6:00 PM)
      
         Verify that:
      - All displayed flights have departure times within the specified range
      - Flight list is properly displayed
      - Search results match the selected route
      """)

    @Test(description = "Verify flight search functionality with time filtering")
    public void basicRoundTripWithTimeFilter() {
        String url = ConfigReader.get("baseUrl");
        String from = ConfigReader.get("fromCity");
        String to = ConfigReader.get("toCity");
        String departureFlightDate = ConfigReader.get("departDate"); // yyyy-MM-dd
        String returnFlightDate = ConfigReader.get("returnDate");
        String departureTimeStart = ConfigReader.get("departureTimeStart");
        String departureTimeEnd = ConfigReader.get("departureTimeEnd");

        Allure.parameter("Base URL", url);
        Allure.parameter("From", from);
        Allure.parameter("To", to);
        Allure.parameter("Depart Date", departureFlightDate);
        Allure.parameter("Return Date", returnFlightDate);
        Allure.parameter("Departure Time Range", departureTimeStart + "–" + departureTimeEnd);

        // ---- Adımlar (Page Object çağrıları)
        Allure.step("Navigate to the homepage, configure round-trip, choose departure and arrival cities and dates, then start the search.", () -> {
            new HomePage(driver, explicitWaitSec)
                    .goTo(url)
                    .ensureRoundTrip()
                    .setFrom(from)
                    .setTo(to)
                    .setDates(departureFlightDate, returnFlightDate)
                    .search();
        });

        ResultsPage results = Allure.step(
                "Wait for flight results to load and apply departure time filter ("
                        + departureTimeStart + "–" + departureTimeEnd + ")",
                () -> new ResultsPage(driver, explicitWaitSec)
                        .waitForResults()
                        .applyDepartureTimeFilter(departureTimeStart, departureTimeEnd)
        );


        List<String> departureTimes = Allure.step(
                "Collect departure times from filtered flight cards",
                results::getFilteredDepartureTimes
        );

        List<String> departureDates = Allure.step(
                "Collect departure dates from filtered flight cards",
                results::getFilteredDepartureDates
        );

        List<String> departureCities = Allure.step(
                "Collect departure cities from filtered flight cards",
                results::getFilteredDepartureCities
        );

        Allure.step(
                "Verify that all departure times are within the range "
                        + departureTimeStart + "–" + departureTimeEnd,
                () -> AssertionsHelper.assertTimesWithinRange(departureTimes, departureTimeStart, departureTimeEnd)
        );

        Allure.step(
                "Verify that all departure dates match '" + departureFlightDate + "'",
                () -> AssertionsHelper.assertDatesEqual(departureDates, departureFlightDate)
        );

        Allure.step(
                "Verify that all departure cities match '" + from + "'",
                () -> AssertionsHelper.assertCitiesEqual(departureCities, from)
        );
    }


}
