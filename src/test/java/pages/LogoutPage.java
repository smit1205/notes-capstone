package pages;

import helperUtils.WaitsHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LogoutPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // data-testid is the most stable locator — set by devs explicitly for testing
    private final By logoutBtn = By.cssSelector("button[data-testid='logout']");

    public LogoutPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void clickLogout() {
        WebElement btn = wait.until(
                ExpectedConditions.elementToBeClickable(logoutBtn)
        );

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", btn
        );

        try {
            btn.click();
        } catch (Exception e) {
            System.out.println("Normal click failed, using JS fallback: " + e.getMessage());
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }
}