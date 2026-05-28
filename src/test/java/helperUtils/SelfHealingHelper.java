package helperUtils;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * SelfHealingHelper — Agentic locator recovery utility (Section 3.3)
 *
 * Two modes:
 *   1. findWithAutoHeal(primary)        — derives fallbacks automatically from the locator hint
 *   2. findElement(primary, fallbacks)  — explicit fallback chain you supply
 *
 * Also provides safeClick() with 3-tier click recovery.
 */
public class SelfHealingHelper {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public SelfHealingHelper(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait   = wait;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MODE 1 — Auto-heal: derives fallback locators from the broken locator hint
    // Usage:  healer.findWithAutoHeal(By.id("broken-id"))
    // ─────────────────────────────────────────────────────────────────────────

    public WebElement findWithAutoHeal(By primary) {
        String hint = extractHint(primary);

        List<By> autoFallbacks = Arrays.asList(
                By.id(hint),
                By.name(hint),
                By.cssSelector("[data-testid='" + hint + "']"),
                By.cssSelector("[placeholder='"  + hint + "']"),
                By.cssSelector("[aria-label='"   + hint + "']"),
                By.xpath("//*[normalize-space(text())='" + hint + "']"),
                By.xpath("//*[contains(@class,'" + hint + "')]"),
                By.xpath("//*[contains(@id,'"    + hint + "')]")
        );

        System.out.println("[SELF-HEAL] 🔍 Auto-heal mode — extracted hint: \"" + hint + "\"");
        return findElement(primary, autoFallbacks.toArray(new By[0]));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MODE 2 — Manual chain: you supply explicit fallbacks
    // Usage:  healer.findElement(By.id("broken"), By.cssSelector(".btn"), By.xpath("//button"))
    // ─────────────────────────────────────────────────────────────────────────

    public WebElement findElement(By primary, By... fallbacks) {

        // 1. Try primary
        try {
            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(primary));
            System.out.println("[SELF-HEAL] ✅ Primary locator succeeded: " + primary);
            return el;
        } catch (Exception e) {
            System.out.println("[SELF-HEAL] ❌ Primary FAILED: " + primary);
        }

        // 2. Try each fallback
        for (int i = 0; i < fallbacks.length; i++) {
            By fallback = fallbacks[i];
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                WebElement el = shortWait.until(ExpectedConditions.visibilityOfElementLocated(fallback));
                System.out.println("[SELF-HEAL] 🔧 Healed with fallback #" + (i + 1) + ": " + fallback);
                System.out.println("[SELF-HEAL] 💡 UPDATE your Page Object → replace broken locator with: " + fallback);
                return el;
            } catch (Exception ex) {
                System.out.println("[SELF-HEAL] ❌ Fallback #" + (i + 1) + " FAILED: " + fallback);
            }
        }

        // 3. All exhausted
        throw new NoSuchElementException(
                "[SELF-HEAL] 🚨 All locators exhausted. Primary was: " + primary
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SAFE CLICK — 3-tier click recovery
    // Usage:  healer.safeClick(element)
    // ─────────────────────────────────────────────────────────────────────────

    public void safeClick(WebElement element) {
        try {
            element.click();
            System.out.println("[SELF-HEAL] ✅ Normal click succeeded");
        } catch (ElementClickInterceptedException e) {
            System.out.println("[SELF-HEAL] ⚠️ Click intercepted — retrying with JS click");
            try {
                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", element);
                System.out.println("[SELF-HEAL] ✅ JS click succeeded");
            } catch (Exception ex) {
                System.out.println("[SELF-HEAL] ⚠️ JS click failed — retrying with Actions");
                new Actions(driver).moveToElement(element).click().perform();
                System.out.println("[SELF-HEAL] ✅ Actions click succeeded");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE — extract the value part from a By locator string
    // e.g. By.id("note-title") → "note-title"
    //      By.cssSelector(".btn-add") → ".btn-add"
    // ─────────────────────────────────────────────────────────────────────────

    private String extractHint(By locator) {
        String s = locator.toString();
        return s.contains(": ") ? s.substring(s.indexOf(": ") + 2).trim() : s;
    }
}
