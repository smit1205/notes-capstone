package tests;

import agentic.NaturalLanguageController;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * MCPTest — TestNG wrapper so you can run MCP commands as part of your test suite.
 *
 * Run via Maven:
 *   mvn test -Dtest=MCPTest
 *
 * Or specify a custom command at runtime:
 *   mvn test -Dtest=MCPTest -Dmcp.command="create note titled Sprint Planning"
 */
public class MCPTest {

    private NaturalLanguageController controller;

    @BeforeClass
    public void setup() {
        controller = new NaturalLanguageController();
    }

    @Test(description = "MCP: Login with valid credentials")
    public void testMCPLogin() {
        controller.execute("login with valid credentials");
    }

    @Test(description = "MCP: Create a new note", dependsOnMethods = "testMCPLogin")
    public void testMCPCreateNote() {
        controller.execute("create a note titled 'Sprint Planning' with content 'Team sync at 10am'");
    }

    @Test(description = "MCP: Logout of the application", dependsOnMethods = "testMCPCreateNote")
    public void testMCPLogout() {
        controller.execute("logout");
    }

    // ── Dynamic command from system property ──────────────────────
    @Test(description = "MCP: Run dynamic command from -Dmcp.command=...")
    public void testMCPDynamicCommand() {
        String cmd = System.getProperty("mcp.command", "view all notes");
        controller.execute(cmd);
    }
}
