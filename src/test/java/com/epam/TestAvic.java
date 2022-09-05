package com.epam;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.*;

import static org.openqa.selenium.By.xpath;
import static org.testng.Assert.*;

public class TestAvic {
    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://avic.ua/");
    }

    @Test(priority = 1)
    public void checkAddToOrder() {
        driver.findElement(xpath("//input[@id='input_search']")).sendKeys("samsung");
        driver.findElement(xpath("//button[@class='button-reset search-btn']")).click();
        driver.findElement(xpath("(//a[@class='prod-cart__buy'])[1]")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(6));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("js_cart")));
        driver.findElements(xpath("//*[@class='item']")).size();
        driver.findElement(xpath("(//a[@class='btn-add'])[2]")).click();
        driver.findElement(xpath("(//a[@class='name'])[2]")).getText();
        assertNotNull(driver.findElement(xpath("//div[@class='cart-parent-limit']//span[contains(text(),name)]")));
    }

    @Test(priority = 2)
    public void checkAmountAfterAddQuantity() throws InterruptedException {
        driver.findElement(xpath("//input[@id='input_search']")).sendKeys("samsung");
        driver.findElement(xpath("//button[@class='button-reset search-btn']")).click();
        driver.findElement(xpath("(//a[@class='prod-cart__buy'])[1]")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(300));
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
        Thread.sleep(1000);
        String total_price = driver.findElement(xpath("//div[contains(@class,'item-total')]/span[contains(@class,'prise')]")).getText();
        String[] part2 = total_price.split(" ");
        if (part2.length != 2) {
            throw new IllegalArgumentException();
        }
        int number = Integer.parseInt(part2[0]);
        assertTrue(number == number_new);
    }

    @Test(priority = 3)
    public void checkCartSignIn() {
        driver.findElement(xpath("//div[contains(@class,'header-bottom__cart')]/parent::a")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(6));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("js_cart")));
        driver.findElement(xpath("//div[@id='js_cart']/descendant::a[contains(@href,'sign-in')]")).click();
        assertEquals(driver.findElement(xpath("//div[@class='page-title']")).getText(), "Вхід та реєстрація");
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
        Set<String> set = new LinkedHashSet<String>(ref);
        String url = "";
        HttpURLConnection huc = null;
        int respCode = 200;

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
        assertTrue(invalid.size()==0, invalid.toString());
    }

    @AfterMethod
    public void close() {
        driver.close();
    }

}
