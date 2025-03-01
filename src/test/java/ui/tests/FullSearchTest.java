package ui.tests;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.title;
import static utils.AllureCustom.allureStep;
import static utils.AllureCustom.verify;

public class FullSearchTest extends TestBase {

    @Test(description = "Полнотекстовый поиск, проверка изменения запроса")
    @Description("Проверка изменения поискового запроса полнотекстового поиска на странице результатов")
    @Severity(SeverityLevel.NORMAL)
    public void fullSearchResultTest() {
        SoftAssert softAssert = new SoftAssert();

        String expectedTitle = "Компания Crosstech Solutions Group - ИБ решения для мониторинга, контроля и комплексной защиты от внутренних угроз";
        String actualTitle = title();
        String expectedSearchWindowTitle = "Результаты поиска";
        String searchRequest = "Тестирование";
        String newSearchRequest = "Проект";

        Allure.step("Проверить, что заголовок страницы: " + expectedTitle, () -> {
            Assert.assertEquals(actualTitle, expectedTitle);
        });

        allureStep("Ввести поисковый запрос и выполнить поиск", () -> {
            Allure.step("Ввести поисковый запрос и выполнить поиск", () -> {
                pages.getHomePage().searchItem(searchRequest);
            });
            String actualSearchWindowTitle = pages.getSearchPage().getSearchTitle();
            int searchResultsCounter = pages.getSearchPage().getSearchResultsCount();
            int searchItemsCount = pages.getSearchPage().getAllSearchResults().size();

            Allure.step("Проверить заголовок окна", () -> {
                softAssert.assertEquals(actualSearchWindowTitle, expectedSearchWindowTitle);
            });

            Allure.step("Проверить, что счетчик отображает верное количество результатов поисковой выдачи", () -> {
                softAssert.assertEquals(searchResultsCounter, searchItemsCount);
            });

            Allure.step("Проверить, что строка запроса содержит искомое значение " + searchRequest, () -> {
                softAssert.assertEquals(pages.getSearchPage().getSearchFieldValue(), searchRequest);
            });

        });

        allureStep("Изменить поисковый запрос и выполнить поиск", () -> {
            Allure.step("Ввести поисковый запрос и выполнить поиск", () -> {
                pages.getSearchPage().setSearchRequest(newSearchRequest);
            });

            int searchResultsCounter = pages.getSearchPage().getSearchResultsCount();
            int searchItemsCount = pages.getSearchPage().getAllSearchResults().size();

            Allure.step("Проверить, что счетчик отображает верное количество результатов поисковой выдачи", () -> {
                softAssert.assertEquals(searchResultsCounter, searchItemsCount);
            });

            Allure.step("Проверить, что строка запроса содержит искомое значение " + newSearchRequest, () -> {
                softAssert.assertEquals(pages.getSearchPage().getSearchFieldValue(), newSearchRequest);
            });

        });


        softAssert.assertAll();
    }

    @Test(description = "Проверка открытия результата полнотекстового поиска")
    @Description("Поиск информации на сайте и открытие результата поисковой выдачи'")
    @Severity(SeverityLevel.NORMAL)
    public void openSearchResultItem() {
        SoftAssert softAssert = new SoftAssert();
        String searchRequest = "Тестирование";

        allureStep("Выполнить полнотекстовый поиск переход по результату поиска", () -> {
            allureStep("Ввести поисковый запрос и выполнить поиск", () -> {
                Allure.step("Ввести поисковый запрос и выполнить поиск", () -> {
                    pages.getHomePage().searchItem(searchRequest);
                });

                Allure.step("Открыть первый результат поисковой выдачи", () -> {
                    pages.getSearchPage().openSearchItem(2).click();

                });

                verify("Проверить, что на странице содержится строка запроса: " + searchRequest, () -> {
                    boolean isStringFound = false;

                    ElementsCollection h1Elements = $$("h1");
                    ElementsCollection h2Elements = $$("h2");
                    ElementsCollection pElements = $$("p");

                    List<SelenideElement> allElements = new ArrayList<>();
                    allElements.addAll(h1Elements);
                    allElements.addAll(h2Elements);
                    allElements.addAll(pElements);

                    for (SelenideElement element : allElements) {
                        String elementText = element.getText().trim().toLowerCase();
                        if (elementText.contains(searchRequest.toLowerCase())) {
                            isStringFound = true;
                        }
                    }

                    if (!isStringFound) {
                        softAssert.fail("Строка запроса '" + searchRequest + "' не найдена на странице");
                    }
                });

            });

        });

    }

}

