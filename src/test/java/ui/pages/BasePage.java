package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;


public class BasePage {
    private final SelenideElement HeaderLogo = $(".header__logo");
    private final SelenideElement searchButton = $(".r-header-search__open");
    private final SelenideElement searchField = $(".r-header-search__input");
    private final SelenideElement careerButton = $x("//a[@href='/career/']");

    public void searchItem(String item) {
        searchButton.click();
        searchField.click();
        searchField.setValue(item).pressEnter();
    }

    public void careerButtonClick() {
        careerButton.shouldBe(Condition.visible)
                .shouldBe(Condition.enabled)
                .click();
    }
}
