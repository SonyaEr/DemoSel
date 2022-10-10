package com.epam;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.*;

import static org.openqa.selenium.By.xpath;
import static org.testng.Assert.*;

public class TestAvic {
    private WebDriver driver;

    @BeforeClass
    public void profileSetUp() {
        System.setProperty("webdriver.chrome.driver", "src\\main\\resources\\chromedriver.exe");
    }

    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://avic.ua/");
    }

    @Test(priority = 1)
    public void checkAddToOrder() {
        driver.findElement(By.id("input_search")).sendKeys("samsung");
        driver.findElement(By.cssSelector("button.button-reset.search-btn")).click();
        driver.findElements(By.className("prod-cart__buy")).get(0).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("js_cart")));
        driver.findElements(By.className("btn-add")).get(1).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(xpath("(//div[@class='item'])[2]")));
        String tittle = driver.findElement(By.xpath("((//a[@class='btn-add'])[1]/following::a)[2]")).getText();
        WebElement el = driver.findElement(xpath("//div[@class='item']//span[contains(text(),'" + tittle + "')]"));
        assertTrue(el.isDisplayed());

    }

    @Test(priority = 2)
    public void checkAmountAfterAddQuantity() {
        driver.findElement(By.id("input_search")).sendKeys("samsung");
        driver.findElement(By.cssSelector("button.button-reset.search-btn")).click();
        driver.findElements(By.className("prod-cart__buy")).get(1).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("js_cart")));
        String totalPriceOld = driver.findElement(xpath("//div[contains(@class,'item-total')]/span[contains(@class,'prise')]")).getText();
        driver.findElements(By.className("btn-add")).get(1).click();
        wait.until(ExpectedConditions.elementToBeClickable(xpath("(//span[contains(@class,'plus')])[1]")));
        driver.findElement(xpath("(//span[contains(@class,'plus')])[1]")).click();
        (new WebDriverWait(driver, Duration.ofSeconds(8))).until((ExpectedCondition<WebElement>) webDriver -> {
            if (!Objects.equals(totalPriceOld, driver.findElement(xpath("//div[contains(@class,'item-total')]/span[contains(@class,'prise')]")).getText())) {
                return driver.findElement(xpath("//div[contains(@class,'item-total')]/span[contains(@class,'prise')]"));
            }
            return null;
        });
        List<WebElement> prices = driver.findElements(xpath("//div[contains(@class,'total-h')]/span[contains(@class,'prise')]"));
        int numberNew = 0;
        for (int i = 0; i < prices.size(); i++) {
            String[] part = prices.get(i).getText().split(" ");
            if (part.length != 2) {
                throw new IllegalArgumentException();
            }
            int totalH = Integer.parseInt(part[0]);
            int count = Integer.parseInt(driver.findElement(xpath("(//input[@class='js-changeAmount count'])[" + i + "+1]")).getAttribute("value"));
            numberNew = numberNew+totalH * count;
        }
       String totalPrice = driver.findElement(xpath("//div[contains(@class,'item-total')]/span[contains(@class,'prise')]")).getText();
        String[] part2 = totalPrice.split(" ");
        if (part2.length != 2) {
            throw new IllegalArgumentException();
        }
        int number = Integer.parseInt(part2[0]);
        assertEquals(numberNew, number);
    }

    @Test(priority = 3)
    public void checkCartSignIn() {
        driver.findElement(xpath("//div[contains(@class,'header-bottom__cart')]/parent::a")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("js_cart")));
        driver.findElement(xpath("//div[@id='js_cart']/descendant::a[contains(@href,'sign-in')]")).click();
        assertEquals(driver.findElement(By.className("page-title")).getText(), "Вхід та реєстрація");
    }

    @Test(priority = 4)
    public void checkSliderMenuHrefValid() {
        Actions actions = new Actions(driver);
        ArrayList<String> ref = new ArrayList<>();
        List<WebElement> menu = driver.findElements(By.xpath("//div[@class='menu-lvl first-level']/ul/li[@class='parent js_sidebar-item']"));
        for (int i = 0; i < menu.size(); i++) {
            actions.moveToElement(menu.get(i)).perform();
            ref.add(driver.findElement(By.xpath("(//div[@class='menu-lvl first-level']/ul/li[@class='parent js_sidebar-item']/a)[" + i + "+1]")).getAttribute("href"));
            List<WebElement> subMenu = driver.findElements(xpath("(//div[@class='menu-lvl second-level next-level js_next-level'])[" + i + "+1]/ul/li[@class='parent js_sidebar-item']"));
            for (int j = 0; j < subMenu.size(); j++) {
                ref.add(driver.findElement(By.xpath("((//div[@class='menu-lvl second-level next-level js_next-level'])[" + i + "+1]/ul/li[@class='parent js_sidebar-item']/a)[" + j + "+1]")).getAttribute("href"));
                List<WebElement> sub2Menu = driver.findElements(xpath("((//ul[@class='sidebar-list'])[" + i + "+1]/li[@class='parent js_sidebar-item'])[" + j + "+1]//li[@class='single-hover-block']/a"));
                for (int k = 0; k < sub2Menu.size(); k++) {
                    ref.add(sub2Menu.get(k).getAttribute("href"));
                }
                actions.moveToElement(subMenu.get(j)).perform();
            }
        }
        Set<String> set = new LinkedHashSet<>(ref);
        String url;
        HttpURLConnection huc;
        int respCode;

        Iterator<String> it = set.iterator();
        ArrayList<String> invalid = new ArrayList<>();
        while (it.hasNext()) {
            url = it.next();
            if (url == null || url.isEmpty()) {
                continue;
            }
            try {
                huc = (HttpURLConnection) (new URL(url).openConnection());
                huc.setRequestMethod("HEAD");
                huc.connect();
                respCode = huc.getResponseCode();
                if (respCode >= 400) {
                    invalid.add(url);
                }
            } catch (IOException e) {
                invalid.add(url);
            }
        }
        assertEquals(invalid.size(), 0, invalid.toString());
    }

    @AfterMethod
    public void close() {
        driver.close();
    }

}
