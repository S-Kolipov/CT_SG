package ui.tests;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.model.Status;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class HomePageSmokeTest {

    private static final Logger logger = LoggerFactory.getLogger(HomePageSmokeTest.class);
    protected static Properties properties;
    protected static WebDriver driver;

    static {
        properties = new Properties();
        try (InputStream input = HomePageSmokeTest.class.getClassLoader().getResourceAsStream("local.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BeforeMethod
    public void setUp() {
        configureWebDriver();
        openBaseUrl();
        closeCookieBanner();
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            takeScreenshot("Тест завершен");
            driver.quit();
        }
    }

    protected void configureWebDriver() {
        if (Boolean.parseBoolean(properties.getProperty("useWebDriverManager", "true"))) {
            io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        if (Boolean.parseBoolean(properties.getProperty("headless", "false"))) {
            options.addArguments("--headless");
        }

        driver = new ChromeDriver(options);
    }

    protected void openBaseUrl() {
        String baseUrl = properties.getProperty("web.baseUrl", "https://ct-sg.ru/");
        driver.get(baseUrl);
        logger.info("Открыта базовая страница: {}", baseUrl);
    }

    protected void closeCookieBanner() {
        try {
            WebElement cookieBanner = driver.findElement(By.cssSelector(".r-cookie.js-r-cookie.active"));
            WebElement cookieBannerButton = driver.findElement(By.cssSelector(".r-cookie__button-text.page__button-text"));
            if (cookieBanner != null && cookieBanner.isDisplayed()) {
                Allure.step("Принять куки", cookieBannerButton::click);
                logger.info("Куки-баннер закрыт.");
            }
        } catch (NoSuchElementException e) {
            logger.info("Куки-баннер не найден или уже закрыт.");
        }
    }

    protected void takeScreenshot(String stepName) {
        if (driver instanceof TakesScreenshot) {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("Скриншот: " + stepName, "image/png", new ByteArrayInputStream(screenshot), "png");
        }
    }

    @Test(description = "Проверка кликабельных элементов на главной странице")
    @Description("Тест выполняет клики доступных элементов на главной странице")
    @Severity(SeverityLevel.CRITICAL)
    public void clickableElementsOnHomePageTest() {
        SoftAssert softAssert = new SoftAssert();
        boolean[] isAnyStepFailed = new boolean[]{false};

        logger.info("Главная страница открыта.");
        Allure.step("Открыть главную страницу.");

        // Находим все кликабельные элементы
        List<WebElement> clickableElements = driver.findElements(By.cssSelector("a, button, input[type='submit'], input[type='button']"));
        logger.info("Найдено {} кликабельных элементов.", clickableElements.size());

        // Основной цикл проверки элементов
        for (int i = 0; i < clickableElements.size(); i++) {
            try {

                final List<WebElement>[] currentClickableElements = new List[]{driver.findElements(By.cssSelector("a, button, input[type='submit'], input[type='button']"))};
                WebElement element = currentClickableElements[0].get(i);

                String elementText = element.getText().trim();
                String elementTag = element.getTagName();
                String elementHref = element.getAttribute("href");

                if (elementText.isEmpty() && !elementTag.equals("input")) {
                    logger.info("Пропускаем элемент без текста: {}", elementTag);
                    continue;
                }
                if (elementHref != null && !elementHref.contains("ct-sg.ru")) {
                    logger.info("Пропускаем внешнюю ссылку: {}", elementHref);
                    continue;
                }

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                wait.until(ExpectedConditions.elementToBeClickable(element)); // Ожидаем кликабельности

                String mainWindowHandle = driver.getWindowHandle();

                Allure.step("Кликнуть на элемент: " + elementText, () -> {
                    try {
                        element.click();
                        logger.info("Клик на элемент: {}", elementText);

                        wait.until(ExpectedConditions.not(ExpectedConditions.titleIs("")));

                        Set<String> windowHandles = driver.getWindowHandles();

                        if (windowHandles.size() > 1) {

                            for (String windowHandle : windowHandles) {
                                if (!windowHandle.equals(mainWindowHandle)) {
                                    driver.switchTo().window(windowHandle);
                                    // Ожидаем загрузки страницы в новой вкладке
                                    wait.until(ExpectedConditions.not(ExpectedConditions.titleIs("")));
                                    String pageTitle = driver.getTitle();
                                    logger.info("Переключиться на новую вкладку: {}", pageTitle);

                                    try {
                                        driver.close();
                                        logger.info("Новая вкладка закрыта.");
                                    } catch (Exception e) {
                                        logger.error("Ошибка при закрытии новой вкладки: {}", e.getMessage());
                                        logger.info("Не удалось закрыть новую вкладку");
                                    }

                                    driver.switchTo().window(mainWindowHandle);
                                    logger.info("Вернуться на главную вкладку.");
                                    break;
                                }
                            }
                        } else {
                            // Если новая вкладка не открывалась, а был переход, то проверяем заголовок страницы
                            String pageTitle = driver.getTitle();
                            softAssert.assertTrue(pageTitle != null && !pageTitle.isEmpty(),
                                    "Страница не открылась после клика на элемент: " + elementText);
                            logger.info("Страница успешно открыта. Заголовок: {}", pageTitle);
                            Allure.step("Страница успешно открыта. Заголовок: " + pageTitle);

                            driver.navigate().back();
                            logger.info("Вернуться на главную страницу.");
                        }

                        // Обновить список элементов после возврата на главную страницу
                        currentClickableElements[0] = driver.findElements(By.cssSelector("a, button, input[type='submit'], input[type='button']"));
                        logger.info("Список элементов обновлен.");

                    } catch (Exception e) {
                        logger.error("Ошибка при клике на элемент: {}", elementText, e);
                        Allure.step("Ошибка при клике на элемент: " + elementText, Status.FAILED);
                        isAnyStepFailed[0] = true;
                    }
                });




            } catch (Exception e) {
                logger.error("Ошибка при обработке элемента: {}", e.getMessage());
                Allure.step("Ошибка при обработке элемента", Status.FAILED);
                isAnyStepFailed[0] = true;
            }
        }

        if (isAnyStepFailed[0]) {
            softAssert.fail("Один или несколько шагов теста провалены.");
        }

        softAssert.assertAll();
    }
}