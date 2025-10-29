package pages;

import base.BasePage;
import io.qameta.allure.Step;
import locator.HomePageLocator;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class HomePage extends BasePage {

    public HomePage(WebDriver driver, long explicitWaitSec) { super(driver, explicitWaitSec); }

    @Step("Homepage: {baseUrl}")
    public HomePage goTo(String baseUrl) {
        driver.get(baseUrl);
        acceptCookiesIfPresent();
        return this;
    }

    @Step("Round-trip mode")
    public HomePage ensureRoundTrip() {
        try {
            click(HomePageLocator.roundTripToggle);
        } catch (Exception ignored) {
        }
        return this;
    }

    @Step("From: {from}")
    public HomePage setFrom(String from) {
        type(HomePageLocator.fromInput, from);
        pressEnter(HomePageLocator.fromInput);
        return this;
    }

    @Step("To: {to}")
    public HomePage setTo(String to) {
        type(HomePageLocator.toInput, to);
        pressEnter(HomePageLocator.toInput);
        return this;
    }

    @Step("Select dates: {depart} → {ret}")
    public HomePage setDates(String depart, String ret) {
        // 🟢 Select departure date
        click(HomePageLocator.departDateInput);
        shortWait(0.2);
        selectDateFromCalendar(depart, "Departure");

        // 🟢 Select return date
        click(HomePageLocator.returnDateInput);
        shortWait(0.2);
        selectDateFromCalendar(ret, "Return");

        return this;
    }

    private void selectDateFromCalendar(String date, String type) {
        // Expected format: "2025-10-29"
        String locator = String.format("//button[@title='%s']", date);
        By dayButton = By.xpath(locator);

        try {
            wait.until(ExpectedConditions.elementToBeClickable(dayButton));
            WebElement element = driver.findElement(dayButton);
            element.click();
            log.info("{} date selected: {}", type, date);
        } catch (Exception e) {
            log.error("{} date selection failed: {} - {}", type, date, e.getMessage());
            throw e;
        }
    }

    @Step("Disable hotels option (if present)")
    public HomePage ensureHotelsListUnchecked() {
        try {
            WebElement cbInput = wait.until(ExpectedConditions.presenceOfElementLocated(HomePageLocator.checkboxInput));
            Thread.sleep(1000);
            if (cbInput.isSelected()) {
                WebElement span = driver.findElement(HomePageLocator.checkboxSpan);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", span);
                span.click();
                log.info("Hotels option turned off (it was previously selected).");
            } else {
                log.info("Hotels option is already off.");
            }
        } catch (NoSuchElementException e) {
            log.warn("Hotels option not found; page structure may differ: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error while checking the hotels option: {}", e.getMessage());
        }
        return this;
    }

    @Step("Search")
    public void search() {
        click(HomePageLocator.searchButton);
    }
}
