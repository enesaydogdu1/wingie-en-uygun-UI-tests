package tests;

import base.BaseTest;
import io.qameta.allure.*;
import org.testng.annotations.*;
import pages.HomePage;
import pages.PassengerFormPage;
import pages.ResultsPage;
import utils.AssertionsHelper;
import utils.ConfigReader;
import locator.PassengerFormPageLocator;
import locator.ResultsPageLocator;

@Epic("Critical Path")
@Feature("End-to-End Purchase Flow")
@Owner("Enes AYDOGDU")
public class TC3_CriticalPathTest extends BaseTest {

    @Severity(SeverityLevel.BLOCKER)
    @Story("Test the most critical user journey")
    @Description("""
            1- Navigate to enuygun.com
            2- Perform a round-trip flight search
            3- Select first outbound and return flights
            4- Choose a package
            5- Fill in passenger and contact details
            6- Verify navigation to the payment page
            """)
    @Test(description = "Validate that the user's core purchase journey works end-to-end without breaking.")
    public void criticalJourney() throws InterruptedException {
        // ---- Read parameters from config.properties
        String url = ConfigReader.get("baseUrl");
        String from = ConfigReader.get("fromCity");
        String to = ConfigReader.get("toCity");
        String departureFlightDate = ConfigReader.get("departDate");
        String returnFlightDate = ConfigReader.get("returnDate");

        // ---- Allure parameters
        Allure.parameter("Base URL", url);
        Allure.parameter("From", from);
        Allure.parameter("To", to);
        Allure.parameter("Depart Date (yyyy-MM-dd)", departureFlightDate);
        Allure.parameter("Return Date (yyyy-MM-dd)", returnFlightDate);

        // ---- Test Steps
        Allure.step("Navigate to the homepage, set round-trip, select cities and dates, then start the search", () -> {
            new HomePage(driver, explicitWaitSec)
                    .goTo(url)
                    .ensureRoundTrip()
                    .setFrom(from)
                    .setTo(to)
                    .setDates(departureFlightDate, returnFlightDate)
                    .search();
        });

        Allure.step("Wait for flight results to load", () ->
                new ResultsPage(driver, explicitWaitSec).waitForResults()
        );

        Allure.step("Select the first outbound flight card", () ->
            driver.findElement(ResultsPageLocator.clickFirstFlightCard).click()
        );

        Allure.step("Select provider and continue", () ->
            driver.findElement(ResultsPageLocator.selectFirstFlightCardBtn).click()
        );

        Allure.step("Select the first return flight card", () ->
            driver.findElements(ResultsPageLocator.clickReturnFlightCard).get(0).click()
        );

        Allure.step("Choose the 'Super Eco' package", () ->
            driver.findElement(ResultsPageLocator.selectSuperEkoPackageBtn).click()
        );

        Allure.step("Verify that the passenger form page is displayed", () ->
                AssertionsHelper.assertElementVisible(driver, PassengerFormPageLocator.passengerForm, 10, "Passenger Information Page")
        );

        PassengerFormPage passengerFormPage = Allure.step("Initialize PassengerFormPage object", () ->
                new PassengerFormPage(driver)
        );

        Allure.step("Fill in contact information (email & phone)", () ->
                passengerFormPage.fillContactInfo(ConfigReader.get("mail"), ConfigReader.get("phoneNumber"))
        );

        Allure.step("Fill in passenger identity information", () ->
                passengerFormPage.fillPassengerInfo(
                        ConfigReader.get("name"),
                        ConfigReader.get("lastName"),
                        ConfigReader.get("birthDay"),
                        ConfigReader.getInt("birthMonth"),
                        ConfigReader.get("birthYear"),
                        ConfigReader.get("gender"),
                        ConfigReader.get("nationalID")
                )
        );

        Allure.step("Click 'Continue to Payment' button", passengerFormPage::clickProceedToPayment);

        Allure.step("Verify that the payment page is displayed", () ->
                AssertionsHelper.assertElementVisible(driver, PassengerFormPageLocator.paymentForm, 10, "Payment Information Page")
        );
    }
}
