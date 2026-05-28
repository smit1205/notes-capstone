package pages;

import helperUtils.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import java.util.List;

public class NotesPage {

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    // ── Locators ──
    private By addNoteBtn     = By.xpath("//button[normalize-space()='+ Add Note']");
    private By noteTitleInput = By.xpath("//input[@name='title' or @placeholder='Title']");
    private By noteDescInput  = By.xpath("//textarea[@name='description' or @placeholder='Description']");
    private By categorySelect = By.xpath("//select[@name='category' or @id='category']");

    // FIX 1: "Save" added — edit modal uses "Save", create modal uses "Create"
    private By saveNoteBtn = By.xpath(
            "//button[normalize-space()='Create' or " +
                    "normalize-space()='Save'            or " +
                    "@data-testid='note-submit']"
    );

    private By modalOverlay = By.xpath(
            "//div[contains(@class,'modal') and contains(@class,'show')]"
    );
    private By anyDialog = By.xpath(
            "//div[contains(@class,'modal') and contains(@class,'show')] | //div[@role='dialog']"
    );
    private By confirmDeleteBtn = By.xpath(
            "//div[contains(@class,'modal') and contains(@class,'show')]//button[normalize-space()='Delete'] | " +
                    "//div[@role='dialog']//button[normalize-space()='Delete']"
    );

    public NotesPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(30));
        this.js     = (JavascriptExecutor) driver;
    }

    // FIX 2: Dismiss any leftover open modal before clicking Add Note
    // Prevents "Meeting" style TimeoutException when prior test left modal open
    private void dismissAnyOpenModal() {
        try {
            boolean modalPresent = !driver.findElements(modalOverlay).isEmpty();
            if (modalPresent) {
                driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            }
            wait.until(ExpectedConditions.invisibilityOfElementLocated(modalOverlay));
        } catch (Exception ignored) {}
    }

    public void clickAddNote() {
        dismissAnyOpenModal();
        wait.until(ExpectedConditions.presenceOfElementLocated(addNoteBtn));
        wait.until(ExpectedConditions.elementToBeClickable(addNoteBtn));
        js.executeScript("arguments[0].scrollIntoView(true);", driver.findElement(addNoteBtn));
        js.executeScript("arguments[0].click();", driver.findElement(addNoteBtn));
    }

    // FIX 3: JS clear + Ctrl+A + DELETE ensures pre-populated fields are wiped reliably
    private void clearAndType(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        js.executeScript("arguments[0].value = '';", el);
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        el.clear();
        el.sendKeys(text);
    }

    public void enterTitle(String title) {
        clearAndType(noteTitleInput, title);
    }

    public void enterDescription(String description) {
        clearAndType(noteDescInput, description);
    }

    public void selectCategory(String category) {
        String normalized = category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase();
        try {
            WebElement select = wait.until(ExpectedConditions.visibilityOfElementLocated(categorySelect));
            new Select(select).selectByVisibleText(normalized);
        } catch (Exception e) {
            By catBtn = By.xpath(
                    "//button[normalize-space()='" + normalized + "'] | " +
                            "//label[normalize-space()='"  + normalized + "']"
            );
            wait.until(ExpectedConditions.elementToBeClickable(catBtn));
            js.executeScript("arguments[0].click();", driver.findElement(catBtn));
        }
    }

    public void clickSave() {
        wait.until(ExpectedConditions.elementToBeClickable(saveNoteBtn));
        js.executeScript("arguments[0].click();", driver.findElement(saveNoteBtn));
    }

    public void addNote(String title, String description, String category) {
        clickAddNote();
        enterTitle(title);
        enterDescription(description);
        selectCategory(category);
        clickSave();

        // ✅ 30s timeout, handles stale refs and slow API
        new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(30))
                .until(driver -> {
                    try {
                        return driver
                                .findElements(By.cssSelector("[data-testid='note-card-title']"))
                                .stream()
                                .anyMatch(el -> el.getText().trim().equals(title));
                    } catch (StaleElementReferenceException e) {
                        return false;
                    }
                });
    }

    public boolean isNoteVisible(String title) {
        try {
            // ✅ Same pattern — safe against stale refs
            wait.until(driver -> {
                try {
                    return driver.findElements(By.cssSelector("[data-testid='note-card-title']"))
                            .stream()
                            .anyMatch(el -> el.getText().trim().equals(title));
                } catch (StaleElementReferenceException e) {
                    return false;
                }
            });
            return true;

        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isNoteAbsent(String title) {
        try {
            By noteTitle = By.xpath(
                    "//*[contains(@class,'card') or @data-testid='note-card']" +
                            "//*[self::h5 or self::h4 or self::h3 or contains(@class,'card-title') or contains(@class,'note-title')]" +
                            "[normalize-space()='" + title + "']"
            );
            wait.until(ExpectedConditions.invisibilityOfElementLocated(noteTitle));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void filterByCategory(String category) {
        String normalized = category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase();
        By catTab = By.xpath("//button[contains(normalize-space(), '" + normalized + "')]");
        wait.until(ExpectedConditions.elementToBeClickable(catTab));
        js.executeScript("arguments[0].click();", driver.findElement(catTab));
    }

    public void clickEditNote(String title) {
        By editInCard = By.xpath(
                "//*[contains(@class,'card') or @data-testid='note-card'][.//*[normalize-space()='" + title + "']]" +
                        "//button[normalize-space()='Edit'] | " +
                        "//*[normalize-space()='" + title + "']//following::button[@data-testid='edit-note'][1]"
        );
        wait.until(ExpectedConditions.elementToBeClickable(editInCard));
        js.executeScript("arguments[0].click();", driver.findElement(editInCard));
    }

    // FIX 4: Wait for modal open AND title field pre-populated before clearing
    // Prevents race condition where clear() fires on an empty field before React populates it
    public void editNote(String oldTitle, String newTitle, String newDescription) {
        clickEditNote(oldTitle);
        wait.until(ExpectedConditions.visibilityOfElementLocated(modalOverlay));
        wait.until(ExpectedConditions.visibilityOfElementLocated(noteTitleInput));
        wait.until(d -> {
            String val = d.findElement(noteTitleInput).getAttribute("value");
            return val != null && !val.trim().isEmpty();
        });
        enterTitle(newTitle);
        enterDescription(newDescription);
        clickSave();
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(modalOverlay));
        } catch (Exception ignored) {}
    }

    public void clickDeleteNote(String title) {
        By deleteInCard = By.xpath(
                "//*[contains(@class,'card') or @data-testid='note-card'][.//*[normalize-space()='" + title + "']]" +
                        "//button[contains(translate(normalize-space(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'delete')] | " +
                        "//*[normalize-space()='" + title + "']//following::button[@data-testid='delete-note'][1]"
        );
        wait.until(ExpectedConditions.elementToBeClickable(deleteInCard));
        js.executeScript("arguments[0].click();", driver.findElement(deleteInCard));
    }

    public void confirmDelete() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(anyDialog));
        wait.until(ExpectedConditions.elementToBeClickable(confirmDeleteBtn));
        js.executeScript("arguments[0].click();", driver.findElement(confirmDeleteBtn));
    }

    public void deleteNote(String title) {
        clickDeleteNote(title);
        confirmDelete();
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(modalOverlay));
        } catch (Exception ignored) {}
    }

    public boolean isValidationErrorVisible() {
        try {
            By validationError = By.xpath(
                    "//*[contains(@class,'invalid-feedback') or contains(@class,'error') or " +
                            "contains(@class,'alert-danger') or @role='alert']"
            );
            wait.until(ExpectedConditions.visibilityOfElementLocated(validationError));
            return true;
        } catch (Exception e) {
            try {
                WebElement titleEl = driver.findElement(noteTitleInput);
                Boolean invalid = (Boolean) js.executeScript(
                        "return arguments[0].value.trim() === '' || !arguments[0].validity.valid;", titleEl);
                return Boolean.TRUE.equals(invalid);
            } catch (Exception ex) {
                return false;
            }
        }
    }

    public String getNoteIdFromUI(String title) {
        try {
            By noteCard = By.xpath(
                    "//*[contains(@class,'card') or @data-testid='note-card']" +
                            "[.//*[normalize-space()='" + title + "']]"
            );
            wait.until(ExpectedConditions.visibilityOfElementLocated(noteCard));
            WebElement card = driver.findElement(noteCard);
            for (String attr : new String[]{"data-note-id", "data-id", "id"}) {
                String val = card.getAttribute(attr);
                if (val != null && !val.isEmpty()) return val;
            }
            By viewBtn = By.xpath(
                    "//*[contains(@class,'card') or @data-testid='note-card']" +
                            "[.//*[normalize-space()='" + title + "']]//a[normalize-space()='View'] | " +
                            "//*[contains(@class,'card') or @data-testid='note-card']" +
                            "[.//*[normalize-space()='" + title + "']]//button[@data-testid='view-note']"
            );
            WebElement view = driver.findElement(viewBtn);
            String href = view.getAttribute("href");
            if (href != null && href.contains("/")) {
                return href.substring(href.lastIndexOf("/") + 1);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}