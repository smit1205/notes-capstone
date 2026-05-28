package tests;

import helperUtils.ConfigReader;
import helperUtils.DriverFactory;
import helperUtils.ExcelReader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import pages.LoginPage;
import pages.LogoutPage;

import java.time.Duration;

public class LogoutTest {

    private WebDriver driver;
    private WebDriverWait wait;

    // Single source of truth for all locators used in this test
    private static final By LOGOUT_BTN   = By.cssSelector("button[data-testid='logout']");
    private static final By LOGIN_LINK   = By.xpath("//a[contains(@href,'login')] | //button[contains(normalize-space(),'Login')]");
    private static final By ERROR_TOAST  = By.xpath("//*[contains(@class,'alert') or contains(@class,'toast') or @role='alert']");
    private static final By OVERLAY_CLOSE = By.xpath(
            "//*[contains(@class,'close') or contains(@class,'dismiss')" +
                    " or contains(@aria-label,'close') or contains(@aria-label,'Close')]"
    );

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.initializeBrowser(ConfigReader.getProperty("browser"));
        wait   = new WebDriverWait(driver, Duration.ofSeconds(25));
        driver.get("https://practice.expandtesting.com/notes/app");
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @DataProvider(name = "loginData")
    public Object[][] getData() {
        return ExcelReader.getTestData("src/test/resources/testdata.xlsx", "LoginData");
    }

    @Test(dataProvider = "loginData")
    public void logoutTest(String email, String password, String expectedResult) {

        // Skip header row
        if (email.equalsIgnoreCase("email") || email.equalsIgnoreCase("username")) {
            return;
        }

        String user = (email    == null) ? "" : email.trim();
        String pass = (password == null) ? "" : password.trim();

        // ── Login ─────────────────────────────────────────────────────────
        LoginPage login = new LoginPage(driver);
        login.clickLoginLink();
        login.login(user, pass);

        System.out.println("[INFO] URL after login attempt: " + driver.getCurrentUrl());

        // ── Branch on expected result ─────────────────────────────────────
        if (expectedResult.equalsIgnoreCase("success")) {
            handleSuccessFlow();

        } else if (user.isEmpty() || pass.isEmpty()) {
            handleEmptyFieldsFlow();

        } else {
            handleInvalidCredentialsFlow(user);
        }
    }

    // ── Success flow ──────────────────────────────────────────────────────────
    private void handleSuccessFlow() {

        // 1. Wait for URL to leave login page
        wait.until(ExpectedConditions.not(
                ExpectedConditions.urlContains("login")
        ));

        // 2. Confirm dashboard URL
        wait.until(ExpectedConditions.urlContains("notes/app"));

        // 3. Wait for page JS to fully settle
        waitForPageLoad();

        // 4. Dismiss any ad/overlay that could block the logout button
        dismissOverlayIfPresent();

        // 5. Confirm logout button is visible on dashboard
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGOUT_BTN));
        System.out.println("[INFO] Dashboard loaded — Logout button visible");

        // 6. Perform logout
        LogoutPage logout = new LogoutPage(driver);
        logout.clickLogout();

        System.out.println("[INFO] URL after logout: " + driver.getCurrentUrl());

        // 7. Confirm redirected back to landing/login area
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("login"),
                ExpectedConditions.visibilityOfElementLocated(LOGIN_LINK)
        ));

        Assert.assertTrue(
                driver.getCurrentUrl().contains("notes/app"),
                "Logout failed — unexpected URL: " + driver.getCurrentUrl()
        );

        System.out.println("[PASS] Logout successful");
    }

    // ── Empty fields flow ─────────────────────────────────────────────────────
    private void handleEmptyFieldsFlow() {
        Assert.assertTrue(
                driver.getCurrentUrl().contains("login"),
                "Expected to remain on login page for empty credentials"
        );
        System.out.println("[PASS] Stayed on login page for empty fields");
    }

    // ── Invalid credentials flow ──────────────────────────────────────────────
    private void handleInvalidCredentialsFlow(String user) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_TOAST));
            Assert.assertTrue(
                    driver.findElement(ERROR_TOAST).isDisplayed(),
                    "Expected error message not shown for user: " + user
            );
            System.out.println("[PASS] Error toast displayed for invalid user: " + user);
        } catch (Exception e) {
            Assert.fail("Expected error toast not shown for: " + user + " | " + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void waitForPageLoad() {
        new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                d -> ((JavascriptExecutor) d)
                        .executeScript("return document.readyState").equals("complete")
        );
    }

    private void dismissOverlayIfPresent() {
        try {
            WebElement closeBtn = new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.elementToBeClickable(OVERLAY_CLOSE));
            closeBtn.click();
            System.out.println("[INFO] Overlay dismissed");
        } catch (Exception ignored) {
            // No overlay — perfectly fine
        }
    }
}