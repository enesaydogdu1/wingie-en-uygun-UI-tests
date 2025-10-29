package listeners;

import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import utils.ConfigReader;
import utils.DriverFactory;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestListener implements ITestListener {
    private static final Logger log = LogManager.getLogger(TestListener.class);

    @Override public void onTestStart(ITestResult result) {}
    @Override public void onTestSuccess(ITestResult result) {}

    @Override
    public void onTestFailure(ITestResult result) {
        WebDriver driver = DriverFactory.getDriver();
        if (driver == null) return;
        try {
            byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("Failure Screenshot", new ByteArrayInputStream(bytes));

            String dir = ConfigReader.get("screenshotDir");
            String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String name = result.getMethod().getMethodName() + "_" + time + ".png";
            Path path = Path.of(dir);
            Files.createDirectories(path);
            Files.write(path.resolve(name), bytes);
            log.error("Screenshot saved: {}/{}", dir, name);
        } catch (Exception e) {
            log.error("Screenshot could not be saved", e);
        }
    }

    @Override public void onTestSkipped(ITestResult result) {}
    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
    @Override public void onStart(ITestContext context) {}
    @Override public void onFinish(ITestContext context) {}
}
