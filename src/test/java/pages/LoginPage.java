package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class LoginPage {

    private WebDriver driver;
    private WebDriverWait wait;        // 15s – for main actions
    private WebDriverWait shortWait;   // 3s  – for fallback checks only
    private JavascriptExecutor js;

    @FindBy(xpath = "//a[contains(@href,'login')] | //button[normalize-space()='Login']")
    private WebElement loginLink;

    @FindBy(xpath = "//input[@type='email' or @name='email' or @placeholder='Email address']")
    private WebElement emailField;

    @FindBy(xpath = "//input[@type='password' or @name='password']")
    private WebElement passwordField;

    @FindBy(css = "button[data-testid='login-submit']")
    private WebElement loginButton;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait      = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
        this.js = (JavascriptExecutor) driver;
        PageFactory.initElements(driver, this);
    }

    public void clickLoginLink() {
        wait.until(ExpectedConditions.elementToBeClickable(loginLink));
        js.executeScript("arguments[0].click();", loginLink);
    }

    public void login(String username, String password) {
        wait.until(ExpectedConditions.visibilityOf(emailField));
        emailField.clear();
        emailField.sendKeys(username);
        passwordField.clear();
        passwordField.sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        js.executeScript("arguments[0].click();", loginButton);
    }

    public boolean isDashboardVisible() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//button[normalize-space()='Logout']")
            ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isErrorMessageVisible() {
        // Check 1: Wait up to 10s for DOM alert/toast (invalid credentials - needs time for API response)
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//*[@data-testid='alert-message'] | //*[@role='alert'] | " +
                                    "//*[contains(@class,'alert') and not(contains(@class,'alert-dismissible'))] | " +
                                    "//*[contains(@class,'toast')]")
                    ));
            return true;
        } catch (Exception ignored) {}

        // Check 2: Empty fields – both values empty + still on login page (no wait needed, instant check)
        try {
            String emailVal = emailField.getAttribute("value");
            String passVal  = passwordField.getAttribute("value");
            boolean bothEmpty = (emailVal == null || emailVal.trim().isEmpty())
                    && (passVal  == null || passVal.trim().isEmpty());
            if (bothEmpty && driver.getCurrentUrl().contains("login")) {
                return true;
            }
        } catch (Exception ignored) {}

        // Check 3: One field empty – short wait only (3s max, not 10s)
        try {
            Boolean emailInvalid = (Boolean) js.executeScript(
                    "return arguments[0].value.trim() === '' || !arguments[0].validity.valid;", emailField);
            Boolean passInvalid  = (Boolean) js.executeScript(
                    "return arguments[0].value.trim() === '' || !arguments[0].validity.valid;", passwordField);
            if (Boolean.TRUE.equals(emailInvalid) || Boolean.TRUE.equals(passInvalid)) {
                return true;
            }
        } catch (Exception ignored) {}

        return false;
    }
}