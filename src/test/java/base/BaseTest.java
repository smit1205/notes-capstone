package base;

import agentic.SmartWait;
import helperUtils.ConfigReader;
import helperUtils.DriverFactory;
import helperUtils.SelfHealingHelper;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

public class BaseTest {

    protected SelfHealingHelper healingHelper;
    protected SmartWait         smartWait;

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        String browser = ConfigReader.getProperty("browser");
        String url     = ConfigReader.getUrl();

        DriverFactory.initializeBrowser(browser);
        DriverFactory.getDriver().get(url);

        WebDriverWait wait = new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(10));

        healingHelper = new SelfHealingHelper(DriverFactory.getDriver(), wait);
        smartWait     = new SmartWait(DriverFactory.getDriver(), 15, 300);
    }

    @AfterMethod(alwaysRun = true)
    public void teardown(ITestResult result) {
        DriverFactory.exitBrowser();
    }
}