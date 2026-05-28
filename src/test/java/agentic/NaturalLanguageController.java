package agentic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class NaturalLanguageController {

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL   = "llama-3.3-70b-versatile";
    private static final String API_KEY = System.getenv("GROQ_API_KEY");

    private static final String SYSTEM_PROMPT =
        "You are a Selenium Java test script generator for the app at:\n" +
        "https://practice.expandtesting.com/notes/app\n\n" +
        "=== VARIABLES ALREADY DECLARED (do NOT redeclare) ===\n" +
        "  WebDriver driver\n" +
        "  LoginPage loginPage\n" +
        "  NotesPage notesPage\n" +
        "  String TEST_EMAIL    = \"smitpidurkar12@gmail.com\"\n" +
        "  String TEST_PASSWORD = \"Smit@123\"\n\n" +
        "=== COMPLETE METHOD LIST — USE ONLY THESE, NOTHING ELSE ===\n" +
        "\n" +
        "// Navigation\n" +
        "driver.get(String url)\n" +
        "driver.getCurrentUrl()\n" +
        "\n" +
        "// Login\n" +
        "loginPage.clickLoginLink()\n" +
        "loginPage.login(String email, String password)\n" +
        "loginPage.isDashboardVisible()           // returns boolean — true when Logout button visible\n" +
        "loginPage.isErrorMessageVisible()        // returns boolean\n" +
        "\n" +
        "// Notes — always login first before calling any notesPage method\n" +
        "notesPage.clickAddNote()                 // opens the Add Note modal\n" +
        "notesPage.enterTitle(String title)\n" +
        "notesPage.enterDescription(String description)\n" +
        "notesPage.selectCategory(String category) // values: Home, Work, Personal\n" +
        "notesPage.clickSave()                    // submits the note form\n" +
        "notesPage.addNote(String title, String description, String category)  // all-in-one\n" +
        "notesPage.isNoteVisible(String title)    // returns boolean\n" +
        "notesPage.isNoteAbsent(String title)     // returns boolean\n" +
        "notesPage.editNote(String oldTitle, String newTitle, String newDescription)\n" +
        "notesPage.deleteNote(String title)\n" +
        "notesPage.filterByCategory(String category)\n" +
        "notesPage.isValidationErrorVisible()     // returns boolean\n" +
        "\n" +
        "// Assertions\n" +
        "Assert.assertTrue(boolean condition)\n" +
        "Assert.assertFalse(boolean condition)\n" +
        "\n" +
        "=== FORBIDDEN — these do not exist, never use them ===\n" +
        "- loginPage.enterEmail(...)              // does not exist\n" +
        "- loginPage.enterPassword(...)           // does not exist\n" +
        "- loginPage.clickLogin()                 // does not exist\n" +
        "- loginPage.getLogoutButton()            // does not exist\n" +
        "- notesPage.enterContent(...)            // does not exist — use enterDescription()\n" +
        "- dashboardPage.*                        // not implemented\n" +
        "- authenticationPage.*                   // not implemented\n" +
        "- logoutPage.*                           // not implemented\n" +
        "- Any method not in the list above\n\n" +
        "=== RULES ===\n" +
        "1. Output ONLY raw Java statements — no class, no method wrapper, no imports\n" +
        "2. ALWAYS start with: driver.get(\"https://practice.expandtesting.com/notes/app\");\n" +
        "3. For any action that requires being logged in, call loginPage.clickLoginLink() then loginPage.login(TEST_EMAIL, TEST_PASSWORD) first\n" +
        "4. Only call methods from the COMPLETE METHOD LIST above\n" +
        "5. No markdown, no code fences, no explanation\n" +
        "6. First line must be driver.get(...), nothing else\n";

    private final HttpClient     httpClient;
    private final ObjectMapper   mapper;
    private final ScriptExecutor executor;

    public NaturalLanguageController() {
        this.httpClient = HttpClient.newHttpClient();
        this.mapper     = new ObjectMapper();
        this.executor   = new ScriptExecutor();
    }

    public void execute(String userCommand) {
        System.out.println("\n[MCP] Command: " + userCommand);
        System.out.println("[MCP] Sending to Groq (llama-3.3-70b)...");

        String generatedCode = callGroqAPI(userCommand);

        System.out.println("\n[MCP] Generated Selenium script:");
        System.out.println("─".repeat(60));
        System.out.println(generatedCode);
        System.out.println("─".repeat(60));

        System.out.println("[MCP] Executing...\n");
        executor.runGeneratedScript(generatedCode);
    }

    private String callGroqAPI(String userCommand) {
        if (API_KEY == null || API_KEY.isBlank()) {
            throw new IllegalStateException(
                "GROQ_API_KEY is not set.\n" +
                "Get a FREE key at: https://console.groq.com\n" +
                "Then run:  export GROQ_API_KEY=gsk_your_key_here"
            );
        }

        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("model", MODEL);
            body.put("temperature", 0.1);
            body.put("max_tokens", 1024);

            ArrayNode messages = mapper.createArrayNode();

            ObjectNode systemMsg = mapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", SYSTEM_PROMPT);
            messages.add(systemMsg);

            ObjectNode userMsg = mapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", "Generate Selenium Java code for: " + userCommand);
            messages.add(userMsg);

            body.set("messages", messages);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();

            HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                    "Groq API error " + response.statusCode() + ":\n" + response.body());
            }

            JsonNode json = mapper.readTree(response.body());
            String raw = json
                .get("choices").get(0)
                .get("message")
                .get("content").asText().trim();

            return raw
                .replaceAll("(?s)^```java\\s*", "")
                .replaceAll("(?s)^```\\s*",      "")
                .replaceAll("(?s)\\s*```$",       "")
                .trim();

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Groq API: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        NaturalLanguageController ctrl = new NaturalLanguageController();
        String command = args.length > 0 ? String.join(" ", args) : "login";
        ctrl.execute(command);
    }
}
