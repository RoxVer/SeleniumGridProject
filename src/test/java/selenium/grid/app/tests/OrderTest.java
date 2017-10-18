package selenium.grid.app.tests;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import selenium.grid.app.BaseTest;
import selenium.grid.app.utils.CustomReporter;
import selenium.grid.app.utils.Properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OrderTest extends BaseTest {
    int randomNumber;
    int productsQ;
    String nameP;
    SoftAssert asser = new SoftAssert();
    Random rand = new Random();
    String randName;
    String domain = "gmail.com";
    List<String> products = new ArrayList<String>();
    List<WebElement> elements;
    int id;

    @BeforeTest
    public void checkVersion(String browser) {
        CustomReporter.logAction("Check version test starts");
        driver.navigate().to(Properties.getBaseUrl());
        if (browser.equals("android") && driver.findElement(By.id("menu-icon")).isDisplayed()) {
            CustomReporter.log("PASSED: Mobile version is opened correctly");
        } else if (browser.equals("chrome") && !(driver.findElement(By.id("menu-icon")).isDisplayed())) {
            CustomReporter.log("PASSED: Desktop version opened correctly");
        } else if (browser.equals("android") && !(driver.findElement(By.id("menu-icon")).isDisplayed())) {
            CustomReporter.log("FAILED: Mobile version opened incorrectly");
        } else if (browser.equals("chrome") && driver.findElement(By.id("menu-icon")).isDisplayed()) {
            CustomReporter.log("FAILED: Desktop version opened incorrectly");
        }
    }

    @Test
    public void checkOrder() {
        CustomReporter.logAction("checkOrder test starts");
        CustomReporter.logAction("Open URL");
        driver.navigate().to(Properties.getBaseUrl());
        bigScroll();
        CustomReporter.logAction("Click All products button");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#content > section > a")));
        driver.findElement(By.cssSelector("#content > section > a")).click();
        //Check products quantity on a page
        WebElement element = driver.findElement(By.cssSelector("div.col-sm-12.hidden-md-up.text-xs-center.showing"));
        String prod = element.getText();
        prod = prod.substring(prod.lastIndexOf(" ") + 1);
        prod = prod.replaceAll("\\D+","");
        try {
            productsQ = Integer.parseInt(prod);
        } catch (NumberFormatException e) {
            CustomReporter.log("Number Format Exception <br />");
        }
        for (int i = 0; i < productsQ; i++) {
            elements = driver.findElements(By.xpath("//h1/a"));
            products.add(elements.get(i).getText());
        }
        CustomReporter.logAction("Click random product");
        id = randomNumber();
        scrollUntilVisible(elements.get(id));
        elements.get(id).click();
        smallScroll();
        CustomReporter.logAction("Click info about product");
        driver.findElement(By.cssSelector("li:nth-child(2) > a.nav-link")).click();
        //Info about product on product page
        //Quantity
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"product-details\"]/div[3]/span")));
        String previousQString = driver.findElement(By.xpath("//*[@id=\"product-details\"]/div[3]/span")).getText();
        previousQString = previousQString.replaceAll("\\D+","");
        int previousQuantity = 0;
        try {
            previousQuantity = Integer.parseInt(previousQString);
        } catch (NumberFormatException e) {
            CustomReporter.log("Previous Quantity Format Exception");
        }
        //Price
        String priceEl = driver.findElement(By.cssSelector("div.product-price.h5 > div > span")).getText();
        priceEl = priceEl.replaceAll("\\D+","");
        int priceP = 0;
        try {
            priceP = Integer.parseInt(priceEl);
        } catch (NumberFormatException e) {
            CustomReporter.log("Price Format Exception");
        }
        //Name
        nameP = driver.findElement(By.cssSelector("h1.h1")).getText();

        CustomReporter.logAction("Add product to basket");
        driver.findElement(By.cssSelector("div.add > button")).click();
        By modalButton = By.cssSelector("div.modal-body > div > div:nth-child(2) > div > a");
        wait.until(ExpectedConditions.visibilityOfElementLocated(modalButton));
        CustomReporter.logAction("Click Make Order button");
        driver.findElement(modalButton).click();

        CustomReporter.logAction("Compare added product with the basket product");
        //Basket product quantity
        String qty = driver.findElement(By.xpath("//*[@id=\"cart-subtotal-products\"]/span[1]")).getText();
        qty = qty.replaceAll("\\D+","");
        int quant = 0;
        try {
            quant = Integer.parseInt(qty);
        } catch (NumberFormatException e) {
            CustomReporter.log("Number Format Exception <br />");
        }
        asser.assertEquals(quant, 1, "Incorrect quantity of product in the basket, should be 1: ");
        //Basket product name
        String basketName = driver.findElement(By.cssSelector("span > img")).getText();
        asser.assertEquals(basketName, nameP, "Incorrect name of product in the basket: ");
        //Basket product price
        String actualPrice = driver.findElement(By.cssSelector("strong")).getText();
        actualPrice = actualPrice.replaceAll("\\D+","");
        int basketPrice = 0;
        try {
            basketPrice = Integer.parseInt(actualPrice);
        } catch (NumberFormatException e) {
            CustomReporter.log("Price Format Exception");
        }
        asser.assertEquals(basketPrice, priceP, "Incorrect price of product in the basket: ");

        CustomReporter.logAction("Fill personal data");
        driver.findElement(By.cssSelector("div.text-xs-center > a")).click();
        driver.findElement(By.name("firstname")).sendKeys(getName(randomNumber()));
        driver.findElement(By.name("lastname")).sendKeys(getName(randomNumber()));
        driver.findElement(By.name("email")).sendKeys(getEmail(randomNumber(), domain));
        smallScroll();
        driver.findElement(By.name("continue")).click();
        driver.findElement(By.name("address1")).sendKeys(getName(randomNumber()));
        driver.findElement(By.name("postcode")).sendKeys(index());
        driver.findElement(By.name("city")).sendKeys(getName(randomNumber()));
        smallScroll();
        driver.findElement(By.name("confirm-addresses")).click();
        driver.findElement(By.name("confirmDeliveryOption")).click();
        driver.findElement(By.id("payment-option-2")).click();
        driver.findElement(By.name("conditions_to_approve[terms-and-conditions]")).click();
        driver.findElement(By.cssSelector("#payment-confirmation > div.ps-shown-by-js > button")).click();
        String expectedMessage = "ВАШ ЗАКАЗ ПОДТВЕРЖДЁН";
        String message = driver.findElement(By.cssSelector("h3.h1.card-title")).getText();
        asser.assertEquals(message, expectedMessage, "Incorrect order confirmation message: ");

        String lastQ = driver.findElement(By.cssSelector("div > div.col-xs-2")).getText();
        asser.assertEquals(lastQ, 1, "Incorrect product quantity in the order details: ");

        String lastP = driver.findElement(By.cssSelector("div.col-xs-5.text-xs-right.bold")).getText();
        lastP = lastP.replaceAll("\\D+","");
        int lastPrice = 0;
        try {
            lastPrice = Integer.parseInt(lastP);
        } catch (NumberFormatException e) {
            CustomReporter.log("Price Format Exception");
        }
        asser.assertEquals(lastPrice, priceP, "Incorrect product price in the order details: ");

        String lastName = driver.findElement(By.cssSelector("div.col-sm-4.col-xs-9.details > span")).getText();
        if(lastName.contains(nameP)) {
            CustomReporter.log("Product name in the order details is correct");
        } else {
            CustomReporter.log("Product name in the order details is NOT correct");
        }
        scrollPageDown();
        CustomReporter.logAction("Click Save button");
        driver.findElement(By.cssSelector("#content-hook-order-confirmation-footer > section > a")).click();
        nameP = WordUtils.capitalizeFully(nameP);
        scrollUntilVisible(driver.findElement(By.xpath("//h1/a[.='" + nameP + "']")));
        driver.findElement(By.xpath("//h1/a[.='" + nameP + "']")).click();
        scrollUntilVisible(driver.findElement(By.cssSelector("div.tabs > ul > li:nth-child(2) > a")));
        driver.findElement(By.cssSelector("div.tabs > ul > li:nth-child(2) > a")).click();

        String finalQString = driver.findElement(By.cssSelector("div.product-quantities > span")).getText();
        finalQString = finalQString.replaceAll("\\D+","");
        int finalQuantity = 0;
        try {
            finalQuantity = Integer.parseInt(finalQString);
        } catch (NumberFormatException e) {
            CustomReporter.log("Final Quantity Format Exception");
        }
        asser.assertEquals(finalQuantity - 1, previousQuantity, "Incorrect product quantity on the product's page: ");
        asser.assertAll();
    }

    public int randomNumber() {
        randomNumber = new RandomDataGenerator().nextInt(1, productsQ - 1);
        return randomNumber;
    }

    public String index() {
        int ind = 10000 + rand.nextInt(90000);
        String index = Integer.toString(ind);
        return index;
    }

    public String getEmail(int count, String domain) {
        String chars = "abcdefghijklmnopqrstuvwxyz" + count;
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int index = (int) (rand.nextDouble() * chars.length());
            builder.append(chars.charAt(index));
        }
        randName = builder.toString();
        randName = randName + "@" + domain;
        return randName;
    }

    public String getName(int count) {
        String chars = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int index = (int) (rand.nextDouble() * chars.length());
            builder.append(chars.charAt(index));
        }
        String name = builder.toString();
        return name;
    }

    public void bigScroll(){
        WebDriverWait wait = new WebDriverWait(driver, 10);
        WebElement icon = driver.findElement(By.cssSelector("#menu-icon > i"));
        wait.until(ExpectedConditions.elementToBeClickable(icon));
        JavascriptExecutor executor = (JavascriptExecutor)driver;
        executor.executeScript("window.scrollBy(0,2500)");
    }

    public void scrollPageDown(){
        WebDriverWait wait = new WebDriverWait(driver, 10);
        WebElement icon = driver.findElement(By.cssSelector("#menu-icon > i"));
        wait.until(ExpectedConditions.elementToBeClickable(icon));
        JavascriptExecutor executor = (JavascriptExecutor)driver;
        executor.executeScript("var scrollBefore = $(window).scrollTop();" +
                        "window.scrollTo(scrollBefore, document.body.scrollHeight);" +
                        "return $(window).scrollTop() > scrollBefore;");
    }

    public void smallScroll(){
        WebDriverWait wait = new WebDriverWait(driver, 10);
        WebElement icon = driver.findElement(By.cssSelector("#menu-icon > i"));
        wait.until(ExpectedConditions.elementToBeClickable(icon));
        JavascriptExecutor executor = (JavascriptExecutor)driver;
        executor.executeScript("window.scrollBy(0,500)");
    }

    public void scrollUntilVisible(WebElement element){
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }
}
