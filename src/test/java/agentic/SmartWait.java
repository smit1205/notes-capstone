package agentic;                          // ← must match folder location

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;

public class SmartWait {

    private final WebDriver driver;
    private final int timeoutSec;
    private final int pollMs;

    public SmartWait(WebDriver driver, int timeoutSec, int pollMs) {
        this.driver     = driver;
        this.timeoutSec = timeoutSec;
        this.pollMs     = pollMs;
    }

    public SmartWait(WebDriver driver) {
        this(driver, 15, 300);
    }

    public WebElement untilVisible(By locator) {
        return fluent().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement untilClickable(By locator) {
        return fluent().until(ExpectedConditions.elementToBeClickable(locator));
    }

    public boolean untilTextPresent(By locator, String text) {
        return fluent().until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    public boolean untilInvisible(By locator) {
        return fluent().until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public WebElement untilPresent(By locator) {
        return fluent().until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    private FluentWait<WebDriver> fluent() {
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSec))
                .pollingEvery(Duration.ofMillis(pollMs))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);
    }
}