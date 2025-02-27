package utils;

import com.codeborne.selenide.Selenide;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.qameta.allure.Allure.getLifecycle;
import static java.lang.String.format;
import static org.testng.AssertJUnit.assertTrue;

public class AllureCustom {

    /**
     * Кастомный Аллюр-шаг с префиксом 'Шаг:'
     *
     * @param name     имя шага
     * @param runnable логика
     */
    public static void allureStep(final String name, final Allure.ThrowableRunnableVoid runnable) {
        try {
            Allure.step("Шаг: " + name, runnable);
        } catch (RuntimeException | AssertionError e) {
            // Attach the screenshot to the Allure report
            getLifecycle().addAttachment("stepScreenshot", "image/png", ".png",
                    Selenide.screenshot(OutputType.BYTES));

            // Rethrow the assertion error to mark the test as failed in TestNG
            throw e;
        }
    }

    /**
     * Кастомный Аллюр-шаг с префиксом 'Шаг: и возможностью игнорировать ошибку'
     *
     * @param name     имя шага
     * @param runnable логика
     */
    public static void allureStep(boolean ignoreException, final String name, final Allure.ThrowableRunnableVoid runnable) {
        try {
            Allure.step("Шаг: " + name, runnable);
        } catch (RuntimeException | AssertionError e) {
            // Attach the screenshot to the Allure report
            getLifecycle().addAttachment("stepScreenshot", "image/png", ".png",
                    Selenide.screenshot(OutputType.BYTES));
        }
    }

    /**
     * Кастомный Аллюр-шаг с префиксом 'Проверка'
     *
     * @param name     имя проверки
     * @param runnable логика проверки
     */
    public static void verify(final String name, final Allure.ThrowableRunnableVoid runnable) {
        try {
            Allure.step("Проверка: " + name, runnable);
        } catch (RuntimeException | AssertionError e) {
            // Attach the screenshot to the Allure report
            getLifecycle().addAttachment("verifyScreenshot", "image/png", ".png",
                    Selenide.screenshot(OutputType.BYTES));

            // Rethrow the assertion error to mark the test as failed in TestNG
            throw e;
        }
    }

    /**
     * Кастомный Аллюр-шаг с префиксом 'Проверка:' и игнорированием ошибки (в случае возникновения)
     * шаг в отчёте покрасит, тест продолжит
     *
     * @param name     имя проверки
     * @param runnable логика проверки
     */
    public static void verify(boolean ignoreException, String name, Allure.ThrowableRunnableVoid runnable) {
        try {
            Allure.step("Проверка: " + name, runnable);
        } catch (RuntimeException | AssertionError e) {
            String str = e.getLocalizedMessage();
            int index = e.getLocalizedMessage().indexOf("at");
            if (index != -1) {
                str = str.substring(0, index);
            }
            Allure.step("Тест будет продолжен с ошибкой: " + str, Status.BROKEN);
            // Attach the screenshot to the Allure report
            getLifecycle().addAttachment("verifyScreenshot", "image/png", ".png",
                    Selenide.screenshot(OutputType.BYTES));
        }
    }

    /**
     * Кастомный Аллюр-шаг с префиксом 'Шаг:' и возможностью добавления скриншота
     *
     * @param name     имя шага
     * @param runnable логика
     */
    public static void allureStep(final String name, boolean isScreenshot, final Allure.ThrowableRunnableVoid runnable) {
        try {
            Allure.step("Шаг: " + name, runnable);
            if (isScreenshot) {
                getLifecycle().addAttachment("stepScreenshot", "image/png", ".png",
                        Selenide.screenshot(OutputType.BYTES));
            }
        } catch (RuntimeException | AssertionError e) {
            // Attach the screenshot to the Allure report
            getLifecycle().addAttachment("stepScreenshot", "image/png", ".png",
                    Selenide.screenshot(OutputType.BYTES));

            // Rethrow the assertion error to mark the test as failed in TestNG
            throw e;
        } finally {
            if (isScreenshot) {
                Allure.step("Скриншот шага:", () -> {
                    getLifecycle().addAttachment("stepScreenshot", "image/png", ".png",
                            Selenide.screenshot(OutputType.BYTES));
                });
            }
        }
    }

    /**
     * Кастомный Аллюр-шаг с префиксом 'Проверка:' и возможностью добавления скриншота
     *
     * @param name     имя проверки
     * @param runnable логика проверки
     */
    public static void verify(final String name, boolean isScreenshot, final Allure.ThrowableRunnableVoid runnable) {
        try {
            if (isScreenshot) {
                Allure.step("Скриншот проверки", () -> {
                    getLifecycle().addAttachment("stepScreenshot", "image/png", ".png",
                            Selenide.screenshot(OutputType.BYTES));
                });
            }

            Allure.step("Проверка: " + name, runnable);
        } catch (RuntimeException | AssertionError e) {
            // Attach the screenshot to the Allure report
            getLifecycle().addAttachment("verifyScreenshot", "image/png", ".png",
                    Selenide.screenshot(OutputType.BYTES));
            // Rethrow the assertion error to mark the test as failed in TestNG
            throw e;
        }
    }

    /**
     * Перегруженный метод Аллюр-проверки для его отключения путем добавления переменной isDisabled
     *
     * @param isDisabled переменная для отключения проверки
     * @param reason     причина отключения
     * @param name       имя проверки
     * @param runnable   логика проверки
     */
    public static void verify(boolean isDisabled, String reason, final String name, final Allure.ThrowableRunnableVoid runnable) {
        Allure.step(format("Проверка '%s' отключена на время отладки \n по причине: '%s'", name, reason), Status.SKIPPED);
    }

    /**
     * Перегруженный метод Аллюр-шага для его отключения путем добавления переменной isDisabled
     *
     * @param isDisabled переменная для отключения шага
     * @param reason     причина отключения
     * @param name       имя шага
     * @param runnable   логика шага
     */
    public static void allureStep(boolean isDisabled, String reason, final String name, final Allure.ThrowableRunnableVoid runnable) {
        Allure.step(format("Шаг '%s' отключен на время отладки \n по причине: '%s'", name, reason), Status.SKIPPED);
    }

    /**
     * Добавить в отчёт скриншот всей видимой области экрана монитора
     *
     * @param state флаг подтверждения
     */
    private static void addDesktopScreenshot(boolean state) throws AWTException, IOException {
        if (state) {
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage capture = new Robot().createScreenCapture(screenRect);

            File imageFile = new File("screen.png");
            ImageIO.write(capture, "png", imageFile);
            assertTrue(imageFile.exists());

            getLifecycle().addAttachment("Screenshot", "image/png", ".png", FileUtils.openInputStream(imageFile));
        }
    }
}