package agentic;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

public class ScriptExecutor {

    private static final Path   TEMP_DIR   = Path.of(System.getProperty("java.io.tmpdir"), "mcp_generated");
    private static final String CLASS_NAME = "MCPGeneratedTest";
    private static final String M2 = System.getProperty("user.home") + "/.m2/repository";

    public void runGeneratedScript(String generatedCode) {
        try {
            Files.createDirectories(TEMP_DIR);
            String fullClass  = buildFullClass(generatedCode);
            Path   sourceFile = TEMP_DIR.resolve(CLASS_NAME + ".java");
            Files.writeString(sourceFile, fullClass);

            boolean compiled = compile(sourceFile);
            if (!compiled) {
                System.err.println("[MCP] Compilation failed. Check the generated code above.");
                return;
            }
            runCompiledClass();
        } catch (Exception e) {
            System.err.println("[MCP] Execution error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildFullClass(String code) {
        return "import base.BaseTest;\n" +
               "import org.openqa.selenium.*;\n" +
               "import org.openqa.selenium.support.ui.*;\n" +
               "import org.openqa.selenium.support.ui.ExpectedConditions;\n" +
               "import org.testng.Assert;\n" +
               "import java.util.*;\n" +
               "import java.time.Duration;\n" +
               "import pages.LoginPage;\n" +
               "import pages.NotesPage;\n" +
               "import helperUtils.DriverFactory;\n" +
               "\n" +
               "public class " + CLASS_NAME + " extends BaseTest {\n" +
               "\n" +
               "    public void runMCPCommand() throws Exception {\n" +
               "        setup();\n" +
               "        org.openqa.selenium.WebDriver driver = helperUtils.DriverFactory.getDriver();\n" +
               "        pages.LoginPage loginPage = new pages.LoginPage(driver);\n" +
               "        pages.NotesPage notesPage = new pages.NotesPage(driver);\n" +
               "        String TEST_EMAIL    = \"smitpidurkar12@gmail.com\";\n" +
               "        String TEST_PASSWORD = \"Smit@123\";\n" +
               "        try {\n" +
               indent(code, 12) + "\n" +
               "            System.out.println(\"[MCP] Script completed successfully.\");\n" +
               "        } catch (AssertionError ae) {\n" +
               "            System.out.println(\"[MCP] Assertion failed: \" + ae.getMessage());\n" +
               "            throw ae;\n" +
               "        } catch (Exception e) {\n" +
               "            System.out.println(\"[MCP] Script error: \" + e.getMessage());\n" +
               "            throw e;\n" +
               "        } finally {\n" +
               "            teardown(null);\n" +
               "        }\n" +
               "    }\n" +
               "\n" +
               "    public static void main(String[] args) throws Exception {\n" +
               "        new " + CLASS_NAME + "().runMCPCommand();\n" +
               "    }\n" +
               "}\n";
    }

    private String buildClasspath() {
        String projectRoot = System.getProperty("user.dir");
        String testClasses = projectRoot + File.separator + "target" + File.separator + "test-classes";
        String mainClasses = projectRoot + File.separator + "target" + File.separator + "classes";
        String runtimeCp   = System.getProperty("java.class.path");

        StringBuilder cp = new StringBuilder();
        cp.append(runtimeCp);
        cp.append(File.pathSeparator).append(testClasses);
        cp.append(File.pathSeparator).append(mainClasses);
        addJarsFromDir(cp, M2);
        return cp.toString();
    }

    private void addJarsFromDir(StringBuilder cp, String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) return;
        try {
            Files.walk(dir.toPath())
                .filter(p -> p.toString().endsWith(".jar"))
                .filter(p -> !p.toString().contains("-sources.jar"))
                .filter(p -> !p.toString().contains("-javadoc.jar"))
                .forEach(p -> cp.append(File.pathSeparator).append(p));
        } catch (IOException ignored) {}
    }

    private boolean compile(Path sourceFile) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.err.println("[MCP] JavaCompiler not found — run with JDK, not JRE.");
            return false;
        }

        String fullCp = buildClasspath();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fm = compiler.getStandardFileManager(diagnostics, null, null);
        Iterable<? extends JavaFileObject> units =
            fm.getJavaFileObjectsFromFiles(List.of(sourceFile.toFile()));

        List<String> options = Arrays.asList(
            "-classpath", fullCp,
            "-d", TEMP_DIR.toString(),
            "--release", "17"
        );

        boolean success = compiler.getTask(null, fm, diagnostics, options, null, units).call();
        if (!success) {
            System.err.println("[MCP] Compilation errors:");
            for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics()) {
                if (d.getKind() == Diagnostic.Kind.ERROR) {
                    System.err.println("  Line " + d.getLineNumber() + ": " + d.getMessage(null));
                }
            }
        }
        fm.close();
        return success;
    }

    private void runCompiledClass() throws Exception {
        String fullCp    = buildClasspath();
        String[] entries = fullCp.split(File.pathSeparator);

        URL[] urls = new URL[entries.length + 1];
        urls[0] = TEMP_DIR.toUri().toURL();
        for (int i = 0; i < entries.length; i++) {
            File f = new File(entries[i]);
            if (f.exists()) urls[i + 1] = f.toURI().toURL();
        }

        URLClassLoader loader = URLClassLoader.newInstance(
            urls,
            Thread.currentThread().getContextClassLoader()
        );

        Class<?> cls    = Class.forName(CLASS_NAME, true, loader);
        Object instance = cls.getDeclaredConstructor().newInstance();
        Method run      = cls.getMethod("runMCPCommand");
        run.invoke(instance);
        loader.close();
    }

    private String indent(String code, int spaces) {
        String pad = " ".repeat(spaces);
        return code.lines()
            .map(line -> line.isBlank() ? "" : pad + line)
            .reduce("", (a, b) -> a.isEmpty() ? b : a + "\n" + b);
    }
}
