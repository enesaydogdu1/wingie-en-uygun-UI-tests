package locator;

import org.openqa.selenium.By;

public class ResultsPageLocator {

    // ===== Kart içi indeksli (dinamik) locator'lar =====
    public static By detayBtnInCardByIndex(int i) {
        return By.xpath("(//div[contains(@class,'flight-item')])[" + (i + 1) + "]//button[contains(., 'Detay') or contains(.,'Detail')]");
    }

    public static By originInCardByIndex(int i) {
        return By.xpath("(//div[contains(@class,'flight-item')])[" + (i + 1) + "]//div[contains(@class,'segment-airport-origin')]");
    }

    public static By timeInCardByIndex(int i) {
        return By.xpath("(//div[contains(@class,'flight-item')])[" + (i + 1) + "]//div[contains(@class,'segment-airport-origin')]//span[@data-testid='undefinedTime']");
    }

    public static By dateInCardByIndex(int i) {
        return By.xpath("(//div[contains(@class,'flight-item')])[" + (i + 1) + "]//div[contains(@class,'segment-airport-origin')]//span[@data-testid='undefinedDate']");
    }

    public static By airportInfoInCardByIndex(int i) {
        return By.xpath("(//div[contains(@class,'flight-item')])[" + (i + 1) + "]//div[contains(@class,'segment-airport-origin')]//span[@data-testid='undefinedFlightAirportInfo']");
    }

    public static final By resultsContainer       = By.cssSelector("div.search-result.search-result-departure-only");
    public static final By anyResultItem          = By.cssSelector("div.flight-item");
    public static final By departureTimeFilterOpen= By.cssSelector("div.ctx-filter-departure-return-time.card-header");
    public static final By leftHandle             = By.cssSelector(".rc-slider-handle.rc-slider-handle-1"); // kalkış
    public static final By rightHandle            = By.cssSelector(".rc-slider-handle.rc-slider-handle-2"); // varış
    public static final By sliderContainer        = By.cssSelector(".rc-slider"); // parent; rail genişliği buradan
    public static final By airlinesDropdown       = By.cssSelector("div.ctx-filter-airline.card-header");
    public static final By thyFilterCheckbox      = By.xpath("//label[contains(@for,'TK') and contains(.,'Türk Hava Yolları')]");
    public static final By sortByPriceAsc         = By.cssSelector("div.sort-buttons.search__filter_sort-PRICE_ASC");

    public static final By airlineLabels          = By.cssSelector("div.summary-marketing-airlines[data-testid]");
    public static final By priceMoneyInt          = By.cssSelector("div.summary-average-price span.money-int");
    public static final By summaryAveragePrice    = By.cssSelector(".summary-average-price[data-testid='flightInfoPrice']");

    //for critical journey
    public static final By clickFirstFlightCard = By.xpath("//*[@id=\"flight-0\"]/div[1]");
    public static final By selectFirstFlightCardBtn = By.cssSelector("button[data-testid='providerSelectBtn']");
    public static final By clickReturnFlightCard = By.cssSelector("div.flight-list.flight-list-return.domesticList div.flight-item__wrapper:first-of-type");
    public static final By selectSuperEkoPackageBtn = By.xpath("//*[@id=\"flight-0\"]/div[1]/div[6]/div/div[1]");

}
