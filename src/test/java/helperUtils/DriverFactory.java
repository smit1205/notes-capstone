package helperUtils;

import java.time.Duration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class DriverFactory {

    // ✅ ThreadLocal — each thread gets its OWN driver, never shared
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static WebDriver initializeBrowser(String browserName) {

        WebDriver webDriver;

        if (browserName.equalsIgnoreCase("chrome")) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--incognito");
            options.addArguments("--disable-notifications");
            options.addArguments("--disable-popup-blocking");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-infobars");
            options.addArguments("--disable-blink-features=AutomationControlled");
            webDriver = new ChromeDriver(options);

        } else if (browserName.equalsIgnoreCase("firefox")) {
            webDriver = new FirefoxDriver(new FirefoxOptions());

        } else if (browserName.equalsIgnoreCase("edge")) {
            webDriver = new EdgeDriver(new EdgeOptions());

        } else {
            throw new RuntimeException("Invalid browser: " + browserName);
        }

        webDriver.manage().window().maximize();
        webDriver.manage().timeouts().implicitlyWait(
                Duration.ofSeconds(ConfigReader.getImplicitWaitfromConfig())
        );

        driver.set(webDriver); // ✅ binds to THIS thread only
        return webDriver;
    }

    public static WebDriver getDriver() {
        return driver.get(); // ✅ returns THIS thread's driver
    }

    public static void exitBrowser() {
        WebDriver webDriver = driver.get();
        if (webDriver != null) {
            webDriver.quit();
            driver.remove(); // ✅ CRITICAL — cleans up thread's slot
        }
    }
}