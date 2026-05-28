package tests;

import helperUtils.DriverFactory;
import helperUtils.ExcelReader;
import helperUtils.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.LoginPage;

public class LoginTest {

    @DataProvider(name = "loginData")
    public Object[][] getLoginData() {
        String filePath = "src/test/resources/testdata.xlsx";
        String sheetName = "LoginData";
        return ExcelReader.getTestData(filePath, sheetName);
    }

    @Test(dataProvider = "loginData")
    public void loginTest(String username, String password, String expectedResult) {

            // ADD THESE to debug
            System.out.println("=== ROW: username='" + username + "' password='" + password + "' expected='" + expectedResult + "'");

        // Fix 1: Skip header row if ExcelReader reads it as data
        if (username.equalsIgnoreCase("username") || username.equalsIgnoreCase("email")) {
            return;
        }

        WebDriver driver = DriverFactory.initializeBrowser(ConfigReader.getProperty("browser"));
        driver.get("https://practice.expandtesting.com/notes/app");
        LoginPage loginPage = new LoginPage(driver);
        loginPage.clickLoginLink();

        // Fix 2: Null-safe trim for empty field rows (TC-UI-04)
        String user = (username == null) ? "" : username.trim();
        String pass = (password == null) ? "" : password.trim();

        loginPage.login(user, pass);

        if (expectedResult.equalsIgnoreCase("success")) {
            Assert.assertTrue(loginPage.isDashboardVisible(),
                    "Expected successful login but dashboard not shown for user: " + user);
        } else {
            Assert.assertTrue(loginPage.isErrorMessageVisible(),
                    "Expected error message but none was shown for user: " + user + ", pass: " + pass);
        }

        driver.quit();
    }
}