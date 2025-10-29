package pages;

import base.BasePage;
import io.qameta.allure.Step;
import locator.ResultsPageLocator;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.List;

public class ResultsPage extends BasePage {

    public ResultsPage(WebDriver driver, long explicitWaitSec) {
        super(driver, explicitWaitSec);
    }

    @Step("Wait for flight results to load")
    public ResultsPage waitForResults() {
        // 1) Wait until redirected to the search results page (URL)
        try {
            wait.until(ExpectedConditions.urlContains("/ucak-bileti/arama"));
        } catch (Exception ignored) {}

        // 2) Wait until the container is present in the DOM (presence, not visibility)
        wait.until(ExpectedConditions.presenceOfElementLocated(ResultsPageLocator.resultsContainer));

        // 3) Wait until cards are loaded (manual polling)
        long end = System.currentTimeMillis() + 20000; // 20 sec timeout
        List<WebElement> cards = new ArrayList<>();
        while (System.currentTimeMillis() < end) {
            cards = driver.findElements(ResultsPageLocator.anyResultItem);
            if (cards != null && !cards.isEmpty()) break;
            shortWait(0.3);
        }
        if (cards == null || cards.isEmpty()) {
            throw new TimeoutException("Flight cards did not load (anyResultItem).");
        }

        // 4) Ensure first card is visible (scroll adjustment)
        try {
            WebElement first = cards.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", first);
            wait.until(ExpectedConditions.visibilityOf(first));
        } catch (Exception ignored) {}

        log.info("Flight results loaded. Total flight count: {}", cards.size());
        return this;
    }

    @Step("Apply departure time filter: {from}-{to}")
    public ResultsPage applyDepartureTimeFilter(String fromHHmm, String toHHmm) {

        openDepartureTimeSectionIfCollapsed();

        int fromMin = parseHHmmToMinutes(fromHHmm);  // e.g. 10:00 → 600
        int toMin = parseHHmmToMinutes(toHHmm);      // e.g. 18:00 → 1080

        WebElement container = wait.until(ExpectedConditions.visibilityOfElementLocated(ResultsPageLocator.sliderContainer));
        WebElement left = wait.until(ExpectedConditions.visibilityOfElementLocated(ResultsPageLocator.leftHandle));
        WebElement right = wait.until(ExpectedConditions.visibilityOfElementLocated(ResultsPageLocator.rightHandle));

        // Drag left and right handles to target positions
        moveHandleToMinutes(container, left, fromMin);
        shortWait(1);
        moveHandleToMinutes(container, right, toMin);

        // Verify
        wait.until(d -> String.valueOf(fromMin).equals(left.getAttribute("aria-valuenow")));
        wait.until(d -> String.valueOf(toMin).equals(right.getAttribute("aria-valuenow")));
        log.info("Departure time filter applied: {} - {}", fromHHmm, toHHmm);
        return this;
    }

    @Step("Get departure times from filtered flights")
    public List<String> getFilteredDepartureTimes() {
        int n = getCardsCount();
        List<String> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            openDetailsIfNeeded(i);
            String t = wait.until(ExpectedConditions.visibilityOfElementLocated(ResultsPageLocator.timeInCardByIndex(i)))
                    .getText().replace(" -", "").trim();
            out.add(t);
        }
        return out;
    }

    @Step("Get departure dates from filtered flights")
    public List<String> getFilteredDepartureDates() {
        int n = getCardsCount();
        List<String> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            openDetailsIfNeeded(i);
            String d = wait.until(ExpectedConditions.visibilityOfElementLocated(ResultsPageLocator.dateInCardByIndex(i)))
                    .getText().replace(" -", "").trim();
            out.add(d);
        }
        return out;
    }

    @Step("Get departure cities from filtered flights")
    public List<String> getFilteredDepartureCities() {
        int n = getCardsCount();
        List<String> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            openDetailsIfNeeded(i);
            String info = wait.until(ExpectedConditions.visibilityOfElementLocated(ResultsPageLocator.airportInfoInCardByIndex(i)))
                    .getText().trim();
            out.add(info.split(",")[0].trim());
        }
        return out;
    }

    @Step("Filter only Turkish Airlines flights")
    public ResultsPage filterOnlyTHY() {
        try {
            click(ResultsPageLocator.airlinesDropdown);
            shortWait(0.5);
            click(ResultsPageLocator.thyFilterCheckbox);
            log.info("Filtered for Turkish Airlines flights only.");
        } catch (Exception ignored) {}
        return this;
    }

    @Step("Sort by price ascending")
    public ResultsPage sortByPriceAscending() {
        try {
            click(ResultsPageLocator.sortByPriceAsc);
            log.info("Sorted flights by ascending price.");
        } catch (Exception ignored) {}
        return this;
    }

    @Step("Get all airline names from cards")
    public List<String> getAllAirlines() {
        List<String> out = new ArrayList<>();
        List<WebElement> elements = driver.findElements(ResultsPageLocator.airlineLabels);
        for (WebElement el : elements) {
            String name = el.getText().trim();
            if (!name.isEmpty()) out.add(name);
        }
        log.info("{} airline names captured: {}", out.size(), out.isEmpty() ? "none" : out.get(0));
        return out;
    }

    @Step("Get all prices (numeric values) from cards")
    public List<Integer> getAllPrices() {
        List<Integer> prices = new ArrayList<>();
        List<WebElement> elements = driver.findElements(ResultsPageLocator.priceMoneyInt);
        for (WebElement el : elements) {
            String raw = el.getText().replaceAll("[^0-9]", "").trim();
            if (!raw.isEmpty()) {
                try {
                    prices.add(Integer.parseInt(raw));
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse numeric price: '{}'", raw);
                }
            }
        }
        log.info("{} prices captured: {}", prices.size(), prices);
        return prices;
    }

    @Step("Extract flight data rows from cards")
    public List<String[]> extractFlightRows() {
        List<WebElement> cards = driver.findElements(ResultsPageLocator.anyResultItem);
        List<String[]> rows = new ArrayList<>();

        for (WebElement card : cards) {
            String dep = getTextSafe(card, ".flight-summary-time .flight-departure-time");
            String arr = getTextSafe(card, ".flight-summary-time .flight-arrival-time");
            String airline = getTextSafe(card, ".summary-marketing-airlines[data-testid]");
            String price = getTextSafe(card, ".summary-average-price[data-testid='flightInfoPrice'] .money-int");
            String currency = getAttributeSafe(card);
            String duration = getTextSafe(card, ".summary-duration");
            String stops = getTextSafe(card, ".summary-transit");

            dep = dep.replace(" -", "").trim();
            arr = arr.replace(" -", "").trim();
            price = price.replaceAll("[^0-9]", "").trim();
            duration = duration.trim();
            stops = stops.trim();
            airline = airline.trim();
            currency = currency.isEmpty() ? "TRY" : currency;

            rows.add(new String[]{dep, arr, airline, price, currency, duration, stops});
        }
        return rows;
    }

    // ====== HELPERS ======

    private double readHandleLeftPercent(WebElement handle) {
        try {
            String style = handle.getAttribute("style"); // e.g. "left: 41.6956%;"
            if (style == null) return Double.NaN;
            for (String part : style.split(";")) {
                String s = part.trim();
                if (s.startsWith("left:")) {
                    String v = s.replace("left:", "").replace("%", "").trim();
                    return Double.parseDouble(v);
                }
            }
            return Double.NaN;
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private String getTextSafe(WebElement context, String css) {
        try {
            return context.findElement(By.cssSelector(css)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private int getCardsCount() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(ResultsPageLocator.resultsContainer));
        return driver.findElements(ResultsPageLocator.anyResultItem).size();
    }

    private void openDetailsIfNeeded(int i) {
        int retries = 2;
        for (int attempt = 0; attempt <= retries; attempt++) {
            try {
                List<WebElement> origins = driver.findElements(ResultsPageLocator.originInCardByIndex(i));
                if (!origins.isEmpty() && origins.get(0).isDisplayed()) return;

                WebElement btn = driver.findElement(ResultsPageLocator.detayBtnInCardByIndex(i));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
                btn.click();

                wait.until(ExpectedConditions.refreshed(
                        ExpectedConditions.visibilityOfElementLocated(ResultsPageLocator.originInCardByIndex(i))
                ));
                return;
            } catch (StaleElementReferenceException | ElementClickInterceptedException e) {
                if (attempt == retries) throw e;
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    private String getAttributeSafe(WebElement context) {
        try {
            return context.findElement(ResultsPageLocator.summaryAveragePrice).getAttribute("data-currency").trim();
        } catch (Exception e) {
            return "";
        }
    }

    private void openDepartureTimeSectionIfCollapsed() {
        WebElement header = wait.until(ExpectedConditions.elementToBeClickable(ResultsPageLocator.departureTimeFilterOpen));
        header.click();
    }

    private int parseHHmmToMinutes(String hhmm) {
        String[] parts = hhmm.trim().split(":");
        if (parts.length != 2) throw new IllegalArgumentException("Time format must be HH:mm, e.g., 10:00");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        if (h < 0 || h > 23 || m < 0 || m > 59)
            throw new IllegalArgumentException("Time must be within 00:00–23:59");
        return h * 60 + m;
    }

    private void moveHandleToMinutes(WebElement container, WebElement handle, int targetMinutes) {
        int containerWidth = container.getSize().getWidth();
        int containerX = container.getLocation().getX();

        double currentPercent = readHandleLeftPercent(handle);
        if (Double.isNaN(currentPercent)) {
            String nowAttr = handle.getAttribute("aria-valuenow");
            if (nowAttr != null && nowAttr.matches("\\d+")) {
                int nowMin = Integer.parseInt(nowAttr);
                currentPercent = (nowMin / 1439.0) * 100.0;
            } else {
                int handleCenterX = handle.getLocation().getX() - containerX;
                currentPercent = (handleCenterX * 100.0) / containerWidth;
            }
        }

        double targetPercent = (targetMinutes / 1439.0) * 100.0;
        int deltaX = (int) Math.round((targetPercent - currentPercent) * containerWidth / 100.0);

        Actions actions = new Actions(driver);
        actions.clickAndHold(handle)
                .moveByOffset(deltaX, 0)
                .release()
                .perform();

        try {
            Thread.sleep(150);
        } catch (InterruptedException ignored) {}
    }
}
