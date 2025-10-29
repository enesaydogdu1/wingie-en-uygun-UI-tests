package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AssertionsHelper {

    private static final Logger log = LogManager.getLogger(AssertionsHelper.class);

    /** Verifies all departure times are within the expected [from,to] range. */
    public static void assertTimesWithinRange(List<String> actualTimes, String expectedFrom, String expectedTo) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime from = LocalTime.parse(expectedFrom, fmt);
        LocalTime to   = LocalTime.parse(expectedTo, fmt);

        List<String> outOfRangeTimes = new ArrayList<>();

        for (String timeStr : actualTimes) {
            if (timeStr == null || timeStr.isBlank()) continue;

            String clean = timeStr.trim();
            if (clean.length() > 5) clean = clean.substring(0, 5);

            LocalTime actual = LocalTime.parse(clean, fmt);
            boolean inRange = !actual.isBefore(from) && !actual.isAfter(to);

            if (!inRange) {
                outOfRangeTimes.add(clean);
            }
        }

        if (outOfRangeTimes.isEmpty()) {
            log.info("All departure times are within the expected range: {}-{} (Total flights: {})",
                    expectedFrom, expectedTo, actualTimes.size());
        } else {
            log.error("{} time value(s) are out of range: {}", outOfRangeTimes.size(), outOfRangeTimes);
            Assert.fail(String.format("Found times outside the filter range: %s", outOfRangeTimes));
        }
    }

    /** Verifies all displayed departure dates equal the expected ISO date (yyyy-MM-dd). */
    public static void assertDatesEqual(List<String> actualDates, String expectedDate) {
        DateTimeFormatter expectedFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
        LocalDate expected = LocalDate.parse(expectedDate, expectedFmt);
        int expectedYear = expected.getYear();

        Locale tr = new Locale("tr", "TR");

        // Formats with year
        DateTimeFormatter withYearShort = DateTimeFormatter.ofPattern("d MMM yyyy", tr);   // 13 Kas 2025
        DateTimeFormatter withYearLong  = DateTimeFormatter.ofPattern("d MMMM yyyy", tr);  // 13 Kasım 2025

        // Formats without year -> default to expectedYear
        DateTimeFormatter noYearShort = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("d MMM")
                .parseDefaulting(ChronoField.YEAR, expectedYear)
                .toFormatter(tr);

        DateTimeFormatter noYearLong = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("d MMMM")
                .parseDefaulting(ChronoField.YEAR, expectedYear)
                .toFormatter(tr);

        List<String> mismatched = new ArrayList<>();

        for (String raw : actualDates) {
            if (raw == null) continue;

            // Cleanup: remove hyphens and day names; collapse spaces
            String s = raw.replace(" -", " ")
                    .replace("-", " ")
                    .replaceAll("(?iu)\\b(pazartesi|salı|çarşamba|perşembe|cuma|cumartesi|pazar)\\b", "")
                    .trim()
                    .replaceAll("\\s+", " ");

            LocalDate parsed = null;

            for (DateTimeFormatter f : new DateTimeFormatter[]{withYearShort, withYearLong}) {
                try { parsed = LocalDate.parse(s, f); break; } catch (Exception ignore) {}
            }
            if (parsed == null) {
                for (DateTimeFormatter f : new DateTimeFormatter[]{noYearShort, noYearLong}) {
                    try { parsed = LocalDate.parse(s, f); break; } catch (Exception ignore) {}
                }
            }

            if (parsed == null) {
                mismatched.add(s + " (could not parse)");
            } else if (!parsed.equals(expected)) {
                mismatched.add(s);
            }
        }

        if (mismatched.isEmpty()) {
            log.info("All departure dates match the expected date: {}", expectedDate);
        } else {
            log.error("{} date value(s) do not match. Expected: {}, Mismatches: {}",
                    mismatched.size(), expectedDate, mismatched);
            Assert.fail("Date mismatches found: " + mismatched);
        }
    }

    /** Verifies all displayed departure cities equal the expected city (locale-aware, Turkish). */
    public static void assertCitiesEqual(List<String> actualCities, String expectedCity) {
        Locale tr = new Locale("tr", "TR");
        String expectedNorm = normalizeCity(expectedCity, tr);

        List<String> mismatched = new ArrayList<>();

        for (int i = 0; i < actualCities.size(); i++) {
            String raw = actualCities.get(i);
            if (raw == null || raw.isBlank()) {
                mismatched.add("#" + (i + 1) + ": (empty/null)");
                continue;
            }

            String actualNorm = normalizeCity(raw, tr);

            if (!actualNorm.equals(expectedNorm)) {
                mismatched.add("#" + (i + 1) + ": '" + raw + "'");
            }
        }

        if (mismatched.isEmpty()) {
            log.info("All departure cities match the expected city: {}", expectedCity);
        } else {
            log.error("{} card(s) have a different departure city. Expected: {}, Mismatches: {}",
                    mismatched.size(), expectedCity, mismatched);
            Assert.fail("City mismatches found: " + mismatched);
        }
    }

    private static String normalizeCity(String s, Locale tr) {
        String left = s.split(",")[0].trim();

        String nfd = Normalizer.normalize(left, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");

        String lowered = nfd.toLowerCase(tr).replaceAll("\\s+", " ").trim();

        lowered = lowered.replace("i̇", "i");

        return lowered;
    }

    /** Asserts that prices are in non-decreasing (ascending) order. */
    public static void assertPricesSortedAscending(List<Integer> prices) {
        if (prices == null || prices.isEmpty()) {
            log.error("Price list is empty.");
            Assert.fail("Price list is empty.");
        }

        List<Integer> mismatches = new ArrayList<>();
        for (int i = 1; i < prices.size(); i++) {
            Integer prev = prices.get(i - 1);
            Integer cur  = prices.get(i);
            if (cur < prev) {
                mismatches.add(i);
            }
        }

        if (mismatches.isEmpty()) {
            log.info("Prices are in ascending order. Count={}", prices.size());
        } else {
            List<Integer> sorted = new ArrayList<>(prices);
            sorted.sort(Comparator.naturalOrder());
            log.error("Price ordering is incorrect. Bad indices: {} \nActual: {}\nSorted: {}",
                    mismatches, prices, sorted);
            Assert.fail("Price ordering is incorrect. Bad indices: " + mismatches);
        }
    }

    /** Verifies that all airline names belong to Turkish Airlines (THY). */
    public static void assertAllAirlinesAreSelected(List<String> airlines) {
        if (airlines == null || airlines.isEmpty()) {
            log.error("Airline list is empty.");
            Assert.fail("Airline list is empty.");
        }

        List<String> offenders = new ArrayList<>();
        for (int i = 0; i < airlines.size(); i++) {
            String a = airlines.get(i);
            String norm = (a == null ? "" : a).toLowerCase(Locale.ROOT);
            boolean isThy = norm.contains("türk hava yolları") || norm.contains("turkish airlines") || norm.contains("thy");
            if (!isThy) offenders.add("#" + (i + 1) + ": '" + a + "'");
        }

        if (offenders.isEmpty()) {
            log.info("All displayed flights belong to Turkish Airlines (THY).");
        } else {
            log.error("Found non-THY airlines: {}", offenders);
            Assert.fail("Found non-THY airlines: " + offenders);
        }
    }

    public static void assertElementVisible(WebDriver driver, By locator, int timeoutSeconds, String desc) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            log.info("{} loaded and is visible.", desc);
        } catch (Exception e) {
            log.error("{} failed to load or is not visible. Error: {}", desc, e.toString());
            Assert.fail(desc + " failed to load or is not visible");
        }
    }

    public static void mustTrue(boolean condition, String message) {
        Assert.assertTrue(condition, message);
    }
}
