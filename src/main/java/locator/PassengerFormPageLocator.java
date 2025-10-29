package locator;

import org.openqa.selenium.By;

public class PassengerFormPageLocator {

    // ---- Passenger basic fields ----

    public static final By emailInput = By.cssSelector(
            "#passenger-form input[name='email'], #passenger-form input[type='email']");
    public static final By phoneInput = By.cssSelector(
            "#passenger-form input[name='phone'], #passenger-form input[type='tel']");
    public static final By firstNameInput = By.xpath("//*[@id='firstName_0']");
    public static final By lastNameInput = By.xpath("//*[@id='lastName_0']");
    public static final By dobDayInput = By.xpath("//*[@id='birthDateDay_0']");
    public static final By dobMonthInput = By.xpath("//*[@id='birthDateMonth_0']");
    public static final By dobYearInput = By.xpath("//*[@id='birthDateYear_0']");
    public static final By maleGenderLabel   = By.cssSelector("label[for='gender_M_0']");
    public static final By femaleGenderLabel = By.cssSelector("label[for='gender_F_0']");
    public static final By nationalIdInput = By.cssSelector("input[data-testid='reservation-publicid-TR-input']");
    public static final By passengerForm = By.xpath("//*[@id=\"passenger-form\"]");
    public static final By paymentForm = By.xpath("//*[@id=\"payment-form\"]");

    public static final By submitBtn = By.xpath("//*[@id=\"continue-button\"]");


}
