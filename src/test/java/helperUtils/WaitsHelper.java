package helperUtils;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WaitsHelper {

    private WebDriverWait wait;

    public WaitsHelper(WebDriver driver) {
        this.wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(
                        Long.parseLong(
                                ConfigReader.getProperty("explicit.wait")
                        )
                )
        );
    }

    public void waitForVisibility(WebElement element) {
        wait.until(
                ExpectedConditions.visibilityOf(element)
        );
    }

    public void waitForClickable(WebElement element) {
        wait.until(
                ExpectedConditions.elementToBeClickable(element)
        );
    }

    public WebElement waitForVisibility(By locator) {

        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(locator)
        );
    }
    public WebElement waitForClickable(By locator) {
        return wait.until(
                ExpectedConditions.elementToBeClickable(locator)
        );
    }
}