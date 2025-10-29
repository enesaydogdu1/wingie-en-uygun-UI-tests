package pages;

import locator.PassengerFormPageLocator;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class PassengerFormPage {

    private final WebDriver driver;

    public PassengerFormPage(WebDriver driver) {
        this.driver = driver;
    }

    // ---- Actions ----

    public void fillContactInfo(String email, String phone) {
        driver.findElement(PassengerFormPageLocator.emailInput).sendKeys(email);
        driver.findElement(PassengerFormPageLocator.phoneInput).sendKeys(phone);
    }

    public void fillPassengerInfo(String firstName,
                                  String lastName,
                                  String day,
                                  int monthIndex,   // ayı index olarak aldım (1=Ocak, 2=Şubat, 3=Mart vs.)
                                  String year,
                                  String gender,
                                  String nationalId) throws InterruptedException {

        driver.findElement(PassengerFormPageLocator.firstNameInput).sendKeys(firstName);
        driver.findElement(PassengerFormPageLocator.lastNameInput).sendKeys(lastName);

        WebElement dayField = driver.findElement(PassengerFormPageLocator.dobDayInput);
        dayField.sendKeys(day);
        dayField.sendKeys(Keys.TAB);

        WebElement monthElement = driver.findElement(PassengerFormPageLocator.dobMonthInput);
        Select selectMonth = new Select(monthElement);
        selectMonth.selectByIndex(monthIndex);

        WebElement yearField = driver.findElement(PassengerFormPageLocator.dobYearInput);
        yearField.sendKeys(year);
        yearField.sendKeys(Keys.ENTER);

        if (gender.equalsIgnoreCase("erkek")) {
            driver.findElement(PassengerFormPageLocator.maleGenderLabel).click();
        } else if (gender.equalsIgnoreCase("kadın")) {
            driver.findElement(PassengerFormPageLocator.femaleGenderLabel).click();
        }
        driver.findElement(PassengerFormPageLocator.nationalIdInput).sendKeys(nationalId);
    }

    public void clickProceedToPayment() {
        driver.findElement(PassengerFormPageLocator.submitBtn).click();
    }
}
