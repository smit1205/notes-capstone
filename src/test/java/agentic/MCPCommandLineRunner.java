package agentic;

import java.util.Scanner;

/**
 * MCPCommandLineRunner — interactive CLI / CI entrypoint for the MCP Selenium Controller.
 *
 * Usage (single command):
 *   mvn exec:java -Dexec.mainClass="agentic.MCPCommandLineRunner" -Dcmd="login"
 *   mvn exec:java -Dexec.mainClass="agentic.MCPCommandLineRunner" -Dcmd="create note titled My Meeting"
 *
 * Usage (interactive REPL mode — no -Dcmd):
 *   mvn exec:java -Dexec.mainClass="agentic.MCPCommandLineRunner"
 *
 * Environment:
 *   export ANTHROPIC_API_KEY=sk-ant-...
 *   export NOTES_EMAIL=your@email.com      (optional, defaults to test@notes.com)
 *   export NOTES_PASSWORD=YourPassword123  (optional)
 */
public class MCPCommandLineRunner {

    public static void main(String[] args) {
        NaturalLanguageController controller = new NaturalLanguageController();

        String singleCmd = System.getProperty("cmd");

        if (singleCmd != null && !singleCmd.isBlank()) {
            // One-shot mode: run a single command then exit
            System.out.println("═".repeat(60));
            System.out.println("  MCP Selenium Controller — Single Command Mode");
            System.out.println("═".repeat(60));
            controller.execute(singleCmd);
        } else {
            // Interactive REPL mode
            System.out.println("═".repeat(60));
            System.out.println("  MCP Selenium Controller — Interactive Mode");
            System.out.println("  Target: https://practice.expandtesting.com/notes/app");
            System.out.println("  Type 'exit' or 'quit' to stop.");
            System.out.println("═".repeat(60));

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("\n> Command: ");
                String input = scanner.nextLine().trim();

                if (input.isBlank()) continue;
                if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                    System.out.println("[MCP] Goodbye.");
                    break;
                }

                controller.execute(input);
            }
            scanner.close();
        }
    }
}
