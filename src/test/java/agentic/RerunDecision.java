package agentic;                          // ← must match folder location

import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;

import java.util.logging.Logger;

public class RerunDecision {

    private static final Logger log = Logger.getLogger(RerunDecision.class.getName());
    private final int maxReruns;

    public RerunDecision(int maxReruns) { this.maxReruns = maxReruns; }
    public RerunDecision()              { this(2); }

    public boolean shouldRerun(Throwable failure, int currentRun) {
        if (currentRun >= maxReruns) {
            log.warning("Max reruns (" + maxReruns + ") reached — marking FAILED");
            return false;
        }
        boolean decision = isFlaky(failure);
        log.info("Failure: " + failure.getClass().getSimpleName()
                + " → rerun: " + decision);
        return decision;
    }

    private boolean isFlaky(Throwable t) {
        if (t == null) return false;
        if (t instanceof StaleElementReferenceException
                || t instanceof ElementClickInterceptedException
                || t instanceof TimeoutException
                || t instanceof NoSuchSessionException
                || t instanceof WebDriverException) return true;

        if (t.getCause() != null) return isFlaky(t.getCause());

        String msg = t.getMessage() != null ? t.getMessage().toLowerCase() : "";
        return msg.contains("timeout") || msg.contains("connection refused");
    }

    public int getMaxReruns() { return maxReruns; }
}