package base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import locator.HomePageLocator;

public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final Logger log = LogManager.getLogger(this.getClass());

    public BasePage(WebDriver driver, long explicitWaitSec) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWaitSec));
    }

    protected WebElement waitVisible(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            throw new RuntimeException("Element did not become visible: " + locator, e);
        }
    }

    protected void acceptCookiesIfPresent() {
        try {
            click(HomePageLocator.acceptCookiesBtn);
            log.info("Cookies accepted.");
        } catch (Exception e) {
            log.warn("Cookie pop-up not found or could not be clicked: {}", e.getMessage());
        }
    }

    protected void click(By locator) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
        } catch (Exception e) {
            throw new RuntimeException("Click failed: " + locator, e);
        }
    }

    protected void type(By locator, String text) {
        WebElement el = waitVisible(locator);
        el.clear();
        el.sendKeys(text);
    }

    protected void pressEnter(By locator) {
        waitVisible(locator);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        driver.findElement(locator).sendKeys(Keys.ENTER);
    }

    protected void shortWait(double seconds) {
        try {
            Thread.sleep((long) (seconds * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("shortWait interrupted: {}", e.getMessage());
        }
    }
}
