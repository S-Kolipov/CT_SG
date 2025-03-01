package ui.tests;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.model.Status;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class HomePageSmokeTestRef {

    private static final Logger logger = LoggerFactory.getLogger(HomePageSmokeTestRef.class);
    protected static Properties properties;
    protected static WebDriver driver;

    static {
        properties = new Properties();
        try (var input = HomePageSmokeTestRef.class.getClassLoader().getResourceAsStream("local.properties")) {
            properties.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load properties", e);
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
            WebElement cookieBannerButton = driver.findElement(By.cssSelector(".r-cookie__button-text.page__button-text"));
            if (cookieBannerButton.isDisplayed()) {
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
            Allure.addAttachment(stepName, new ByteArrayInputStream(screenshot));
        }
    }

    @Test(description = "Проверка кликабельных элементов на главной странице")
    @Description("Тест выполняет клики доступных элементов на главной странице")
    @Severity(SeverityLevel.CRITICAL)
    public void clickableElementsOnHomePageTest() {
        SoftAssert softAssert = new SoftAssert();
        boolean isAnyStepFailed = false;

        logger.info("Главная страница открыта.");
        Allure.step("Открыть главную страницу.");

        String mainWindowHandle = driver.getWindowHandle();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        String baseUrl = properties.getProperty("web.baseUrl", "https://ct-sg.ru/");

        // Получаем начальный список элементов
        List<WebElement> allElements = getVisibleClickableElements();

        // Логируем общее количество кликабельных элементов
        int totalElements = allElements.size();
        logger.info("Найдено {} кликабельных элементов.", totalElements);

        // Фильтруем и логируем пропущенные элементы
        int skippedCount = filterAndLogSkippedElements(allElements);
        if (skippedCount > 0) {
            logger.info("Пропущено {} элементов без текста или недоступных.", skippedCount);
        }

        // Создаем копию индексов для обработки
        List<Integer> validIndices = getValidElementIndices(allElements);

        for (int index : validIndices) {
            try {
                // Обновляем список элементов перед каждым кликом
                List<WebElement> currentElements = getVisibleClickableElements();

                if (index >= currentElements.size()) {
                    logger.info("Все доступные элементы успешно обработаны.");
                    break;
                }

                WebElement element = currentElements.get(index);

                String elementText = getElementText(element);

                Allure.step("Кликнуть на элемент: " + elementText, () -> {
                    element.click();
                    logger.info("Клик на элемент: {}", elementText);

                    handleWindowTransition(mainWindowHandle, wait, elementText, softAssert);

                    // Возвращаемся на домашнюю страницу
                    returnToBaseUrl(baseUrl, wait);
                });
            } catch (Exception e) {
                logger.error("Ошибка при обработке элемента: {}", e.getMessage());
                Allure.step("Ошибка при обработке элемента", Status.FAILED);
                isAnyStepFailed = true;
            }
        }

        if (isAnyStepFailed) {
            softAssert.fail("Один или несколько шагов теста провалены.");
        }

        softAssert.assertAll();
    }
    private void returnToBaseUrl(String baseUrl, WebDriverWait wait) {
        driver.navigate().back();

        // Проверяем текущий URL и восстанавливаем домашнюю страницу при необходимости
        WebDriverWait urlWait = new WebDriverWait(driver, Duration.ofSeconds(5));
        urlWait.until((ExpectedCondition<Boolean>) d -> {
            String currentUrl = d.getCurrentUrl();
            if ("data:".equals(currentUrl) || !currentUrl.startsWith(baseUrl)) {
                logger.warn("Текущий URL некорректен ({}). Перезагрузка домашней страницы.", currentUrl);
                driver.get(baseUrl);
                return false; // Продолжаем ожидание
            }
            return true;
        });

        logger.info("Вернуться на главную страницу.");
    }

    private void handleWindowTransition(String mainWindowHandle, WebDriverWait wait, String elementText, SoftAssert softAssert) {
        Set<String> windowHandles = driver.getWindowHandles();

        if (windowHandles.size() > 1) {
            handleNewTab(windowHandles, mainWindowHandle, wait, softAssert);
        } else {
            handleSameTab(wait, elementText, softAssert);
        }
    }

    private void handleNewTab(Set<String> windowHandles, String mainWindowHandle, WebDriverWait wait, SoftAssert softAssert) {
        for (String windowHandle : windowHandles) {
            if (!windowHandle.equals(mainWindowHandle)) {
                driver.switchTo().window(windowHandle);

                // Проверяем URL новой вкладки
                String currentUrl = driver.getCurrentUrl();
                if (currentUrl.endsWith(".pdf")) {
                    logger.info("Открыта новая вкладка с PDF. Закрываем её.");
                } else {
                    try {
                        wait.until(ExpectedConditions.not(ExpectedConditions.titleIs("")));
                        String pageTitle = driver.getTitle();
                        logger.info("Переключиться на новую вкладку: {}", pageTitle);
                    } catch (Exception e) {
                        logger.warn("Не удалось получить заголовок новой вкладки. Возможно, это пустая вкладка.", e);
                    }
                }

                // Закрываем новую вкладку
                try {
                    driver.close();
                    logger.info("Новая вкладка закрыта.");
                } catch (Exception e) {
                    logger.error("Ошибка при закрытии новой вкладки: {}", e.getMessage());
                }

                driver.switchTo().window(mainWindowHandle);
                logger.info("Вернуться на главную вкладку.");
                break;
            }
        }
    }
    private void handleSameTab(WebDriverWait wait, String elementText, SoftAssert softAssert) {
        wait.until(ExpectedConditions.not(ExpectedConditions.titleIs("")));
        String pageTitle = driver.getTitle();
        softAssert.assertTrue(pageTitle != null && !pageTitle.isEmpty(),
                "Страница не открылась после клика на элемент: " + elementText);
        logger.info("Страница успешно открыта. Заголовок: {}", pageTitle);
        Allure.step("Страница успешно открыта. Заголовок: " + pageTitle);
    }

    private List<WebElement> getVisibleClickableElements() {
        return driver.findElements(By.cssSelector("a:not([href^='http']), button, input[type='submit'], input[type='button']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .filter(WebElement::isEnabled)
                .collect(Collectors.toList());
    }

    private int filterAndLogSkippedElements(List<WebElement> elements) {
        long skippedCount = elements.stream()
                .filter(this::isElementInvalid)
                .count();
        return (int) skippedCount;
    }

    private boolean isElementInvalid(WebElement element) {
        return element.getText().trim().isEmpty() && !"input".equals(element.getTagName());
    }

    private List<Integer> getValidElementIndices(List<WebElement> elements) {
        return elements.stream()
                .filter(this::isElementValid)
                .mapToInt((element) -> elements.indexOf(element))
                .boxed()
                .collect(Collectors.toList());
    }

    private boolean isElementValid(WebElement element) {
        return !isElementInvalid(element);
    }

    private String getElementText(WebElement element) {
        return element.getText().trim().isEmpty() ?
                element.getAttribute("value") != null ? element.getAttribute("value") :
                        element.getAttribute("placeholder") != null ? element.getAttribute("placeholder") :
                                element.getTagName() :
                element.getText().trim();
    }
}