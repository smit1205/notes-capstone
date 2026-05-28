package helperUtils;

import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class RetryAnalyzer implements IRetryAnalyzer, IAnnotationTransformer {

    private int count = 0;
    private static final int MAX_RETRY = 2;

    // ── Retry logic ───────────────────────────────────────────────────────
    @Override
    public boolean retry(ITestResult result) {
        if (count < MAX_RETRY) {
            count++;
            System.out.println("[RETRY] " + result.getName()
                    + " failed — retrying attempt " + count + " of " + MAX_RETRY);
            return true;
        }
        System.out.println("[RETRY] " + result.getName()
                + " exhausted all retries — marking as FAILED");
        return false;
    }

    // ── Auto-injects retryAnalyzer into every @Test in the suite ─────────
    @Override
    public void transform(ITestAnnotation annotation,
                          Class testClass,
                          Constructor testConstructor,
                          Method testMethod) {
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
}