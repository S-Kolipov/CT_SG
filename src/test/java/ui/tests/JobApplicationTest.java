package ui.tests;

import com.codeborne.selenide.Selenide;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;


import static com.codeborne.selenide.Condition.cssValue;
import static org.testng.Assert.assertEquals;
import static utils.AllureCustom.allureStep;
import static utils.AllureCustom.verify;

public class JobApplicationTest extends TestBase {
    @Test(description = "Проверка обязательности полей")
    @Description("Тест проверяет что поля, обязательные для заполнения подсвечиваются красной рамкой, если не заполнены")
    @Severity(SeverityLevel.NORMAL)
    public void jobPageEmailValidationTest() {
        allureStep("Через пункт меню в Подвале открыть страницу Вакансий",
                () -> {
                    pages
                            .getHomePage()
                            .careerFooterButtonClick();

                    String expectedTitle = "Карьера в компании Crosstech Solutions Group";
                    String actualTitle = Selenide.title();
                    assertEquals(actualTitle, expectedTitle, "Заголовок страницы не соответствует ожидаемому");
                });

        allureStep("Прокрутить страницу до формы обратной связи и выполнить проверку", () -> {
            pages
                    .getCareerPage()
                    .scrollTofeedbackForm()
                    .commit();
            verify("Проверить, что поля, обязательные для заполнения, подсвечены красным", () -> {
                pages.getCareerPage().getnameField().shouldHave(cssValue("border-color", "rgb(255, 0, 0)"));
                pages.getCareerPage().getresumeField().shouldHave(cssValue("border-color", "rgb(255, 0, 0)"));
                pages.getCareerPage().getmailField().shouldHave(cssValue("border-color", "rgb(255, 0, 0)"));
                pages.getCareerPage().getphoneField().shouldHave(cssValue("border-color", "rgb(255, 0, 0)"));
            });
        });


    }
}
