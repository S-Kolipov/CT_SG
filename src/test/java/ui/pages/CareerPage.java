package ui.pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

public class CareerPage {

    private static SelenideElement sendResumeButton = $x("//a[@class='intro-card__button page__button']");
    private static final SelenideElement feedbackForm = $x("//form[@class='r-feedback__form r-feedback-form']");
    private static final SelenideElement switcherPseudo = $x("//input[@class='switcher__checkbox license-vote']");
    private static final SelenideElement submitButton = $x("//button[contains(@class, 'r-feedback-form__submit')]");
    private static final SelenideElement nameField = $x("//input[@name='FIO']");
    private static final SelenideElement resumeField = $x("//input[@name='RESUME']");
    private static final SelenideElement mailField = $x("//input[@name='MAIL']");
    private static final SelenideElement phoneField = $x("//input[@name='PHONE']");
    private static final SelenideElement comentField = $x("//input[@name='COMMENT']");

    public static void sendResumeButtonClick() {
        sendResumeButton.click();
    }

    public CareerPage setSwitcherPseudo() {
        switcherPseudo.setSelected(true);
        return this;
    }

    public static void clicksubmitButton() {
        submitButton.click();
    }
    public CareerPage setName(String name) {
        nameField.setValue(name);
        return this;
    }
    public CareerPage setResumeData(String resumeData) {
        resumeField.setValue(resumeData);
        return this;
    }
    public CareerPage setEmailField(String email) {
        mailField.setValue(email);
        return this;
    }
    public CareerPage setPhoneField(String phone) {
        phoneField.setValue(phone);
        return this;
    }

}
