package base;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.*;
import utils.ConfigReader;
import utils.DriverFactory;

import java.nio.charset.StandardCharsets;

public abstract class BaseTest {
    protected WebDriver driver;
    protected long explicitWaitSec;
    protected final Logger log = LogManager.getLogger(this.getClass());

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        String browser = ConfigReader.get("browser"); // sadece config.prop dan okuyorum artık. paralel testi deaktif ettim.
        boolean headless = Boolean.parseBoolean(ConfigReader.get("headless"));
        explicitWaitSec = Long.parseLong(ConfigReader.get("explicitWait"));

        DriverFactory.initDriver(browser, headless);
        driver = DriverFactory.getDriver();
        log.info("Driver started. Browser={}", browser);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverFactory.quitDriver();
        ThreadContext.remove("browser");
        log.info("Driver closed.");
    }

    // Allure attachments
    @Attachment(value = "Screenshot", type = "image/png")
    private byte[] attachScreenshot() {
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception ignored) {
            return new byte[0];
        }
    }

    @Attachment(value = "Page Source", type = "text/html", fileExtension = ".html")
    private byte[] attachPageSource() {
        try {
            return driver.getPageSource().getBytes(StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return "<empty/>".getBytes(StandardCharsets.UTF_8);
        }
    }

    // 🔹 Auto-attach on failure (Allure)
    @AfterMethod(alwaysRun = true)
    public void afterEach(ITestResult result) {
        if (!result.isSuccess()) {
            Allure.step("Test failed — attaching screenshot & page source", () -> {
                attachScreenshot();
                attachPageSource();
            });
        }
    }
}
