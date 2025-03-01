package ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.util.ArrayList;

import static com.codeborne.selenide.Selenide.*;

public class SearchPage extends BasePage {
    private final SelenideElement searchCleanButton = $(".search__close");
    private final SelenideElement searchButton = $(".search__button");
    private final SelenideElement searchTitle = $(".search__title");
    private final SelenideElement searchResultsCount = $x("//h2[@class='search__title']//span");
    private final SelenideElement searchField = $x("//label[@class='search__label']//input[@class='search__input']");
    SelenideElement searchItem = $x(".search__item");

    public int getSearchResultsCount() {
        return Integer.parseInt(searchResultsCount.getText());
    }

    public String getSearchTitle() {
        String fullText = searchTitle.getText();
        String spanText = searchResultsCount.getText();
        return fullText.replace(spanText, "").trim();
    }

    public String getSearchFieldValue() {
        return searchField.getAttribute("value");
    }

    public void setSearchRequest(String requestValue) {
        searchField
                .doubleClick()
                .setValue(requestValue)
                .pressEnter();
    }

    public ArrayList getAllSearchResults() {
        ArrayList<SelenideElement> searchResultsList = new ArrayList<>();
        ElementsCollection searchItems = $$(".search__item");
        searchResultsList.addAll(searchItems);

        return searchResultsList;
    }

    public SelenideElement openSearchItem(int index) {
        ElementsCollection searchItems = $$(".search__item");
        if (searchItems.isEmpty()) {
            throw new IndexOutOfBoundsException("Коллекция элементов пуста");
        }
        return searchItems.get(index);
    }
}