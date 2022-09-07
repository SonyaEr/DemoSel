package com.epam;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.function.Function;

import static org.openqa.selenium.By.xpath;
import static org.testng.Assert.*;

public class TestAvic {
    private WebDriver driver;

    @BeforeTest
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
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("js_cart")));
        driver.findElements(By.className("btn-add")).get(1).click();
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));
        String tittle = driver.findElement(By.xpath("((//a[@class='btn-add'])[1]/following::a)[2]")).getText();
        WebElement el = driver.findElement(xpath("//div[@class='item']//span[contains(text(),'"+ tittle +"')]"));
        assertTrue(el.isDisplayed());

    }

    @Test(priority = 2)
    public void checkAmountAfterAddQuantity()  {
        driver.findElement(By.id("input_search")).sendKeys("samsung");
        driver.findElement(By.cssSelector("button.button-reset.search-btn")).click();
        driver.findElements(By.className("prod-cart__buy")).get(1).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(500));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("js_cart")));
        WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofMillis(800));
        wait2.until(ExpectedConditions.elementToBeClickable(By.xpath("(//span[contains(@class,'plus')])[1]")));
        driver.findElement(xpath("(//span[contains(@class,'plus')])[1]")).click();
        List<WebElement> prices = driver.findElements(xpath("//div[contains(@class,'total-h')]/span[contains(@class,'prise')]"));
        int number_new = 0;
        for (int i = 0; i < prices.size(); i++) {
            String[] part = prices.get(i).getText().split(" ");
            if (part.length != 2) {
                throw new IllegalArgumentException();
            }
            int total_h = Integer.parseInt(part[0]);
            int count = Integer.parseInt(driver.findElement(xpath("(//input[@class='js-changeAmount count'])[" + i + "+1]")).getAttribute("value"));
            number_new = total_h * count;
        }
        String total_price_old = driver.findElement(xpath("//div[contains(@class,'item-total')]/span[contains(@class,'prise')]")).getText();

        Wait<WebDriver> wait3 = new FluentWait<>(driver).withTimeout(Duration.ofSeconds(3)).pollingEvery(Duration.ofSeconds(1)).ignoring(NoSuchElementException.class);
        Function<WebDriver, Boolean> function = i -> {
           if(!Objects.equals(total_price_old, driver.findElement(xpath("//div[contains(@class,'item-total')]/span[contains(@class,'prise')]")).getText()))
           {
               return true;
           }
            return false;
        };
        wait3.until(function);
        String total_price = driver.findElement(xpath("//div[contains(@class,'item-total')]/span[contains(@class,'prise')]")).getText();
        String[] part2 = total_price.split(" ");
        if (part2.length != 2) {
            throw new IllegalArgumentException();
        }
        int number = Integer.parseInt(part2[0]);
        assertEquals(number_new, number);
    }

    @Test(priority = 3)
    public void checkCartSignIn() {
        driver.findElement(xpath("//div[contains(@class,'header-bottom__cart')]/parent::a")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
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
            ref.add(driver.findElement(By.xpath("(//div[@class='menu-lvl first-level']/ul/li[@class='parent js_sidebar-item']/a)[" +i+ "+1]")).getAttribute("href"));
              List<WebElement> sub_menu = driver.findElements(xpath("(//div[@class='menu-lvl second-level next-level js_next-level'])[" + i + "+1]/ul/li[@class='parent js_sidebar-item']"));
            for (int j = 0; j < sub_menu.size(); j++) {
                ref.add(driver.findElement(By.xpath("((//div[@class='menu-lvl second-level next-level js_next-level'])[" + i + "+1]/ul/li[@class='parent js_sidebar-item']/a)[" +j+ "+1]")).getAttribute("href"));
                 List<WebElement> sub2_menu = driver.findElements(xpath("((//ul[@class='sidebar-list'])[" + i + "+1]/li[@class='parent js_sidebar-item'])[" + j + "+1]//li[@class='single-hover-block']/a"));
                for (int k = 0; k < sub2_menu.size(); k++) {
                      ref.add(sub2_menu.get(k).getAttribute("href"));
                  }
                actions.moveToElement(sub_menu.get(j)).perform();
            }
        }
        Set<String> set = new LinkedHashSet<>(ref);
        String url;
        HttpURLConnection huc;
        int respCode;

        Iterator<String> it = set.iterator();
        ArrayList<String> invalid = new ArrayList<>();
        while(it.hasNext()){
            url = it.next();
            if(url == null || url.isEmpty()){
                continue;
            }
            try {
                huc = (HttpURLConnection)(new URL(url).openConnection());
                huc.setRequestMethod("HEAD");
                huc.connect();
                respCode = huc.getResponseCode();
                if(respCode >= 400){
                    invalid.add(url);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
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
