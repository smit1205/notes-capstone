package listeners;

import helperUtils.DriverFactory;
import helperUtils.RetryAnalyzer;
import helperUtils.ScreenshotHelper;
import io.qameta.allure.Allure;
import org.testng.IAnnotationTransformer;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class TestListeners implements ITestListener, IAnnotationTransformer {

    // ── NEW — auto injects RetryAnalyzer into every @Test ─────────────────
    @Override
    public void transform(ITestAnnotation annotation,
                          Class testClass,
                          Constructor testConstructor,
                          Method testMethod) {

        // Skip retry for API tests — they are deterministic
        if (testClass != null && testClass.getPackageName().equals("apiTests")) {
            return;
        }

        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }

    // ── EXISTING — untouched ──────────────────────────────────────────────
    @Override
    public void onTestFailure(ITestResult result) {

        String testName = result.getName();

        String screenshotPath =
                ScreenshotHelper.captureScreenshot(
                        DriverFactory.getDriver(),
                        testName
                );

        try {
            Allure.addAttachment(
                    "Failure Screenshot",
                    new FileInputStream(screenshotPath)
            );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}