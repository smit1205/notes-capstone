package tests;

import helperUtils.ConfigReader;
import helperUtils.DriverFactory;
import helperUtils.ExcelReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import pages.LoginPage;
import pages.NotesPage;

import java.time.Duration;
import helperUtils.SelfHealingHelper;

public class NotesTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private NotesPage notesPage;

    private static final String EMAIL    = "smitpidurkar12@gmail.com";
    private static final String PASSWORD = "Smit@123";

    @BeforeMethod
    public void setUp() {
        driver    = DriverFactory.initializeBrowser(ConfigReader.getProperty("browser"));
        wait      = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get("https://practice.expandtesting.com/notes/app");

        // Login before every test
        LoginPage login = new LoginPage(driver);
        login.clickLoginLink();
        login.login(EMAIL, PASSWORD);

        notesPage = new NotesPage(driver);
    }

    @AfterMethod
    public void tearDown() {
        driver.quit();
    }

    @DataProvider(name = "notesData")
    public Object[][] getNotesData() {
        return ExcelReader.getTestData("src/test/resources/testdata.xlsx", "NoteData");
    }


    // TC-UI-06: Note Creation With Valid Details


    @Test(dataProvider = "notesData",
            description = "TC-UI-06: Create a note with valid title, description and category")
    public void TC_UI_06_NoteCreationWithValidDetails(String title, String description, String category) {

        System.out.println("TC-UI-06 | title: " + title + " | category: " + category);

        notesPage.addNote(title, description, category); // wait already inside addNote

        // ✅ isNoteVisible has its own wait — no extra wait needed here
        Assert.assertTrue(
                notesPage.isNoteVisible(title),
                "TC-UI-06 FAILED: Note '" + title + "' not visible after creation."
        );
    }


    // TC-UI-07: Note Creation in Different Categories


    @Test(dataProvider = "notesData",
            description = "TC-UI-07: Note appears under correct category filter after creation")
    public void TC_UI_07_NoteCreationInDifferentCategories(String title, String description, String category) {

        System.out.println("TC-UI-07 | title: " + title + " | category: " + category);

        notesPage.addNote(title, description, category);

        // Filter by the category and verify note is visible
        notesPage.filterByCategory(category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase());

        Assert.assertTrue(notesPage.isNoteVisible(title),
                "TC-UI-07 FAILED: Note '" + title + "' not visible under category: " + category);
    }


    // TC-UI-08: Validation for Empty Note Title


    @Test(description = "TC-UI-08: Creating a note with empty title should show validation error")
    public void TC_UI_08_ValidationForEmptyNoteTitle() {

        System.out.println("TC-UI-08 | empty title validation");

        notesPage.clickAddNote();
        notesPage.enterTitle("");           // empty title
        notesPage.enterDescription("Some description");
        notesPage.clickSave();

        Assert.assertTrue(notesPage.isValidationErrorVisible(),
                "TC-UI-08 FAILED: Validation error not shown for empty title.");
    }


    // TC-UI-09: Newly Created Note Appears Immediately


    @Test(description = "TC-UI-09: Note should appear in the list immediately after creation")
    public void TC_UI_09_NewlyCreatedNoteAppearsImmediately() {

        String title       = "Immediate Note";
        String description = "This should appear right away";
        String category    = "Personal";   // ← was "Home", changed to valid category

        System.out.println("TC-UI-09 | creating note and checking immediate visibility");

        notesPage.addNote(title, description, category);

        Assert.assertTrue(notesPage.isNoteVisible(title),
                "TC-UI-09 FAILED: Note did not appear immediately after creation.");
    }


    // TC-UI-10: Note Deletion


    @Test(description = "TC-UI-11: Delete a note and verify it is removed")
    public void TC_UI_11_NoteDeletion() {

        String title = "Note To Delete";

        System.out.println("TC-UI-11 | creating then deleting a note");

        notesPage.addNote(title, "Will be deleted", "Personal");
        notesPage.deleteNote(title);

        Assert.assertTrue(notesPage.isNoteAbsent(title),
                "TC-UI-11 FAILED: Note '" + title + "' still visible after deletion.");
    }


    // TC-UI-11: Deleted Note Is Removed From List

    @Test(description = "TC-UI-12: Deleted note should not appear in any category filter")
    public void TC_UI_12_DeletedNoteIsRemovedFromList() {

        String title    = "Note To Remove";
        String category = "Work";

        System.out.println("TC-UI-12 | verifying deleted note absent from all views");

        notesPage.addNote(title, "Should disappear", category);
        notesPage.deleteNote(title);

        // Check under All tab
        Assert.assertTrue(notesPage.isNoteAbsent(title),
                "TC-UI-12 FAILED: Deleted note still visible in All view.");

        // Check under category tab
        notesPage.filterByCategory(category);
        Assert.assertTrue(notesPage.isNoteAbsent(title),
                "TC-UI-12 FAILED: Deleted note still visible under category: " + category);
    }



    //Test to verify the working of Agentic self-healing
    @Test(description = "Create a new note with broken locators - Agentic Self-Healing Test")
    public void testCreateNoteWithFaultyLocators() {

        SelfHealingHelper healer = new SelfHealingHelper(driver, wait);

        // BAD: By.id("add-note-btn-WRONG")  →  HEAL: real button
        WebElement addNoteBtn = healer.findElement(
                By.id("add-note-btn-WRONG"),
                By.xpath("//button[contains(@class,'btn-primary') and (contains(text(),'+') or contains(text(),'Note'))]"),
                By.xpath("//a[contains(@href,'notes') and contains(@class,'btn')]")
        );
        addNoteBtn.click();

        // BAD: By.cssSelector(".note-title-input-BROKEN")  →  HEAL: #title
        WebElement titleField = healer.findElement(
                By.cssSelector(".note-title-input-BROKEN"),
                By.id("title"),
                By.name("title"),
                By.xpath("//input[@placeholder='Note Title' or @id='title']")
        );
        titleField.clear();
        titleField.sendKeys("Agentic Test Note");

        // BAD: By.id("category-DOESNOTEXIST")  →  HEAL: #category
        WebElement categoryDropdown = healer.findElement(
                By.id("category-DOESNOTEXIST"),
                By.id("category"),
                By.name("category"),
                By.xpath("//select[contains(@class,'category') or @id='category']")
        );
        new Select(categoryDropdown).selectByVisibleText("Home");

        // BAD: By.name("note-desc-WRONG")  →  HEAL: #description
        WebElement descriptionField = healer.findElement(
                By.name("note-desc-WRONG"),
                By.id("description"),
                By.name("description"),
                By.xpath("//textarea[@id='description' or @name='description']")
        );
        descriptionField.clear();
        descriptionField.sendKeys("This note was created by a self-healing test");

        WebElement submitBtn = healer.findElement(
                By.xpath("//button[text()='Save Note WRONG']"),
                By.xpath("//div[@role='dialog']//button[text()='Create Note']"),
                By.xpath("//div[@role='dialog']//button[contains(@class,'btn-primary')]")
        );
        healer.safeClick(submitBtn);

        // BAD: .note-card-BROKEN  →  HEAL: actual card class
        WebElement noteCard = healer.findElement(
                By.cssSelector(".note-card-BROKEN"),
                By.xpath("//div[contains(@class,'card') and .//text()='Agentic Test Note']"),
                By.xpath("//div[contains(@class,'note-item') or contains(@class,'card')]")
        );
        Assert.assertTrue(noteCard.isDisplayed(),
                "Note card should be visible after self-healed creation");
    }


}