package utils;

import ui.tests.TestBase;
import io.qameta.allure.Allure;
import io.qameta.allure.listener.TestLifecycleListener;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class AllureTestngListener extends TestBase implements ITestListener, TestLifecycleListener, IRetryAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(AllureTestngListener.class); // Инициализация логгера
    private static Properties properties;
    private int nowretryCount = 0;
    private final int maxRetryCount = 1;

    static {
        properties = new Properties();
        try (InputStream input = AllureTestngListener.class.getClassLoader().getResourceAsStream("local.properties")) {
            properties.load(input);
        } catch (IOException e) {
            logger.error("Ошибка при загрузке файла local.properties", e);
        }
    }

    // ************** Testng ITestListener **************//
    @Override
    public void onTestStart(ITestResult result) {

    }

    // ************** Allure TestLifecycleListener **************//

    @Override
    public void beforeTestStop(TestResult result) {

        // Присваиваем статус результату теста в отчете Allure
        setStepsStatus(result.getSteps());

        // Верхний уровень шагов - присваиваем статус результату теста
        if (result.getSteps().stream()
                .anyMatch(stepResult -> stepResult.getStatus().value().equals("failed"))) {
            result.setStatus(Status.FAILED);
        } else if (result.getSteps().stream()
                .anyMatch(stepResult -> stepResult.getStatus().value().equals("broken"))) {
            result.setStatus(Status.BROKEN);
        }

        // Добавляем стилизованное описание теста в отчет
        StringBuilder sb = new StringBuilder();
        if (result.getSteps().stream()
                .anyMatch(stepResult -> stepResult.getStatus().value().equals("skipped"))) {
            sb.append("<span style=\"color:#000000; background-color:#fffacd\">")
                    .append("Один или несколько шагов пропущены.")
                    .append("</span><br/>");
        }

        if (!Boolean.parseBoolean(getProperty("doEmail", "true"))) {
            sb.append("<span style=\"color:#000000; background-color:#fffacd\">")
                    .append("Проверка E-mail отключена.")
                    .append("</span><br/>");
        }

        String currentDescription = getCurrentDescription(result);
        result.setDescriptionHtml(currentDescription + sb);
    }

    /**
     * Добавляет стили в отчет для @Description
     *
     * @param result результат теста
     * @return styled description
     */
    private static String getCurrentDescription(TestResult result) {
        String currentDescription = result.getDescription();

        if (currentDescription != null) {
            String html = String.format(
                    "<style>\n" +
                            ".info-icon {\n" +
                            "    display: flex;\n" +
                            "    justify-content: center;\n" +
                            "    align-items: center;\n" +
                            "    width: 20px;\n" +
                            "    height: 20px;\n" +
                            "    border-radius: 50%%;\n" +
                            "    background-color: #29892c;\n" +
                            "    color: #fff;\n" +
                            "    margin-right: 16px;\n" +
                            "    aspect-ratio: 1 / 1;\n" +
                            "}\n" +
                            ".text {\n" +
                            "    display: flex;\n" +
                            "    align-items: center;\n" +
                            "}\n" +
                            ".box {\n" +
                            "    background-color: #f3f9f4;\n" +
                            "    color: #000000;\n" +
                            "    padding: 20px;\n" +
                            "    border: 1px solid #ccc;\n" +
                            "    border-radius: 5px;\n" +
                            "    border-color: #91c89c;\n" +
                            "    max-width: 90%%;\n" +
                            "    margin: 20px auto;\n" +
                            "    text-align: left;\n" +
                            "    margin-left: 10px;\n" +
                            "}\n" +
                            "</style>\n" +
                            "<div class=\"box\">\n" +
                            "   <div class=\"text\">" +
                            "       <div class=\"info-icon\">i</div>  %s" +
                            "   </div>\n" +
                            "</div>\n", result.getDescription());

            currentDescription = html + "<br/>";
        } else {
            currentDescription = "";
        }

        return currentDescription;
    }

    /**
     * Присваивает каждому верхнеуровневому родительскому шагу статус дочернего, если не Passed
     *
     * @param stepResults результаты шагов
     */
    private void setStepsStatus(List<StepResult> stepResults) {
        for (StepResult result : stepResults) {
            setStatusRecursively(result);
        }
    }

    /**
     * Присваивает рекурсивно статус вложенных дочерних шагов их родительскому шагу, если не Passed
     *
     * @param stepResult результат шага
     */
    private void setStatusRecursively(StepResult stepResult) {
        if (!stepResult.getSteps().isEmpty()) {
            for (StepResult subStep : stepResult.getSteps()) {
                setStatusRecursively(subStep);
            }

            if (stepResult.getSteps().stream().anyMatch(subStep -> subStep.getStatus().value()
                    .equals("failed"))) {
                stepResult.setStatus(Status.FAILED);
            } else if (stepResult.getSteps().stream().anyMatch(subStep -> subStep.getStatus().value()
                    .equals("broken"))) {
                stepResult.setStatus(Status.BROKEN);
            } else if (stepResult.getSteps().stream().anyMatch(subStep -> subStep.getStatus().value()
                    .equals("skipped"))) {
                stepResult.setStatus(Status.SKIPPED);
            }
        }
    }

    /**
     * При добавлении @Test(retryAnalyzer = AllureTestngListener.class) перезапускает единожды тест,
     * если результат в рантайме был FAILED
     *
     * @param result результат запуска тестового метода
     * @return boolean
     */
    @Override
    public boolean retry(ITestResult result) {
        if (nowretryCount < maxRetryCount) {
            nowretryCount++;
            return true; // Пока истина, перезапускаем
        }
        return false;
    }

    /**
     * Получает значение свойства из local.properties
     *
     * @param key ключ свойства
     * @return значение свойства
     */
    private String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Получает значение свойства из local.properties с дефолтным значением
     *
     * @param key          ключ свойства
     * @param defaultValue дефолтное значение
     * @return значение свойства
     */
    private String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
