package locator;

import org.openqa.selenium.By;

public class HomePageLocator {

    public static final By fromInput =
            By.cssSelector("input[placeholder*='Nereden'], input[name='origin']");
    public static final By toInput =
            By.cssSelector("input[placeholder*='Nereye'], input[name='destination']");
    public static final By departDateInput =
            By.cssSelector("[data-testid='enuygun-homepage-flight-departureDate-datepicker-input']");
    public static final By returnDateInput =
            By.cssSelector("[data-testid='enuygun-homepage-flight-returnDate-datepicker-input']");
    public static final By searchButton =
            By.cssSelector("button[data-testid='enuygun-homepage-flight-submitButton']");
    public static final By roundTripToggle =
            By.cssSelector("[data-testid='search-round-trip-text']");

    public static final By checkboxInput =
            By.cssSelector("label[for^='radio-showListHotel'] input[data-testid='flight-oneWayCheckbox-input']");
    public static final By checkboxSpan =
            By.cssSelector("label[for^='radio-showListHotel'] input[data-testid='flight-oneWayCheckbox-input'] + span[data-testid='flight-oneWayCheckbox-span']");

    public static final By acceptCookiesBtn = By.xpath("//*[@id=\"onetrust-accept-btn-handler\"]");
}
