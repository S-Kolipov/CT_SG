package ui.tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Allure;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import ui.pages.PageManager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.codeborne.selenide.Selenide.*;

public class TestBase {

    protected static PageManager pages;
    protected static Properties properties;

    static {
        properties = new Properties();
        try (InputStream input = TestBase.class.getClassLoader().getResourceAsStream("local.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @BeforeMethod
    public void setUp() {
        configureWebDriver();
        openBaseUrl();
        WebDriverRunner.getWebDriver().manage().window().maximize();

        // Принятие куки, если баннер отображается
        WebElement cookieBanner = $(By.cssSelector(".r-cookie.js-r-cookie.active"));
        WebElement cookieBannerButton = $(By.cssSelector(".r-cookie__button-text.page__button-text"));
        if (cookieBanner != null && cookieBanner.isDisplayed()) {
            Allure.step("Принять куки", cookieBannerButton::click);
        }
    }

    @AfterMethod
    public void tearDown() {
        takeScreenshot();
        Selenide.closeWebDriver();
    }

    protected void configureWebDriver() {
        // Настройка WebDriverManager для автоматической загрузки драйверов
        if (Boolean.parseBoolean(properties.getProperty("useWebDriverManager", "true"))) {
            WebDriverManager.chromedriver().setup();
        }

        // Настройка Selenide
        Configuration.browser = "chrome";
        Configuration.headless = Boolean.parseBoolean(properties.getProperty("headless", "false"));
        Configuration.browserSize = "1920x1080"; // Установите размер окна браузера
        Configuration.timeout = 10000; // Установите таймаут для ожиданий
    }

    protected void openBaseUrl() {
        String baseUrl = properties.getProperty("web.baseUrl", "https://ct-sg.ru/");
        open(baseUrl); // Используем Selenide для открытия страницы
    }

    protected void takeScreenshot() {
        byte[] screenshot = Selenide.screenshot(OutputType.BYTES); // Получаем скриншот в виде массива байтов
        Allure.addAttachment("Screenshot on test end", "image/png", new ByteArrayInputStream(screenshot), "png");
    }
}