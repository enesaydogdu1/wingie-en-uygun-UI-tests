package utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class DriverFactory {

    private static final ThreadLocal<WebDriver> tlDriver = new ThreadLocal<>();

    public static void initDriver(String browser, boolean headless) {
        if (tlDriver.get() != null) return;

        switch (browser.toLowerCase()) {
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions fopts = new FirefoxOptions();
                if (headless) fopts.addArguments("-headless");
                fopts.setCapability("moz:webdriverClick", false);
                tlDriver.set(new FirefoxDriver(fopts));
                tlDriver.get().manage().window().maximize();
                break;

            case "chrome":
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions copts = new ChromeOptions();
                if (headless) copts.addArguments("--headless=new");
                copts.addArguments("--start-maximized", "--disable-gpu", "--disable-notifications");
                tlDriver.set(new ChromeDriver(copts));
        }
    }

    public static WebDriver getDriver() {
        return tlDriver.get();
    }

    public static void quitDriver() {
        if (tlDriver.get() != null) {
            tlDriver.get().quit();
            tlDriver.remove();
        }
    }
}
