package ui.pages;

public class PageManager {
    private final HomePage homePage;
    private final SearchPage searchPage;
    private final CareerPage careerPage;

    public HomePage getHomePage() {
        return homePage;
    }

    public SearchPage getSearchPage() {
        return searchPage;
    }

    public CareerPage getCareerPage() {
        return careerPage;
    }

    public PageManager() {
        this.homePage = new HomePage();
        this.searchPage = new SearchPage();
        this.careerPage = new CareerPage();
    }
}