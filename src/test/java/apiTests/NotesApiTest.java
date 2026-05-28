package apiTests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import helperUtils.ConfigReader;
import helperUtils.ExcelReader;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class NotesApiTest {

    private String token;
    private String noteId;

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = ConfigReader.getProperty("apiUrl");

        // reuse same test data as UI tests
        Object[][] loginData = ExcelReader.getTestData(
                "src/test/resources/testdata.xlsx", "LoginData");

        // pick the first success row
        String email = null, password = null;
        for (Object[] row : loginData) {
            if (row[2].toString().equalsIgnoreCase("success")) {
                email = row[0].toString().trim();
                password = row[1].toString().trim();
                break;
            }
        }

        System.out.println("Using email: " + email);
        System.out.println("Using password: " + password);

        String requestBody = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        System.out.println("Request Body: " + requestBody);

        // login via API to get token
        Response loginResponse = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/users/login")
                .then()
                .extract().response();

        System.out.println("Login Status: " + loginResponse.getStatusCode());
        System.out.println("Login Response: " + loginResponse.getBody().asString());

        token = loginResponse.jsonPath().getString("data.token");
        System.out.println("Token: " + token);

        // create a note to use in delete test
        Response createNote = given()
                .contentType("application/json")
                .header("x-auth-token", token)
                .body("{\"title\":\"Test Note\", \"description\":\"This is a test note\", \"category\":\"Home\"}")
                .when()
                .post("/notes")
                .then()
                .extract().response();

        System.out.println("Create Note Response: " + createNote.getBody().asString());
        noteId = createNote.jsonPath().getString("data.id");
        System.out.println("Created Note ID: " + noteId);
    }

    // TC-API-01: GET /notes Returns Notes List
    @Test(priority = 1)
    public void getNotesList() {
        Response response = given()
                .header("x-auth-token", token)
                .when()
                .get("/notes")
                .then()
                .extract().response();

        Assert.assertNotNull(response.jsonPath().getList("data"),
                "TC-API-01 FAILED: Notes list is null");
        System.out.println("TC-API-01 PASSED: Notes list returned");
    }

    // TC-API-02: API Response Status Is 200
    @Test(priority = 2)
    public void getNotesStatusCode() {
        Response response = given()
                .header("x-auth-token", token)
                .when()
                .get("/notes")
                .then()
                .extract().response();

        Assert.assertEquals(response.getStatusCode(), 200,
                "TC-API-02 FAILED: Status code is not 200");
        System.out.println("TC-API-02 PASSED: Status code is 200");
    }

    // TC-API-03: API Response Time Is Below 2 Seconds
    @Test(priority = 3)
    public void getNotesResponseTime() {
        Response response = given()
                .header("x-auth-token", token)
                .when()
                .get("/notes")
                .then()
                .extract().response();

        long responseTime = response.getTime();
        Assert.assertTrue(responseTime < 2000,
                "TC-API-03 FAILED: Response time " + responseTime + "ms exceeds 2000ms");
        System.out.println("TC-API-03 PASSED: Response time is " + responseTime + "ms");
    }

    // TC-API-04: API Response Returns Valid JSON Structure
    @Test(priority = 4)
    public void getNotesValidJsonStructure() {
        Response response = given()
                .header("x-auth-token", token)
                .when()
                .get("/notes")
                .then()
                .extract().response();

        Assert.assertNotNull(response.jsonPath().get("success"),
                "TC-API-04 FAILED: 'success' field missing");
        Assert.assertNotNull(response.jsonPath().get("status"),
                "TC-API-04 FAILED: 'status' field missing");
        Assert.assertNotNull(response.jsonPath().get("data"),
                "TC-API-04 FAILED: 'data' field missing");
        System.out.println("TC-API-04 PASSED: Valid JSON structure");
    }

    // TC-API-05: DELETE /notes Removes the Note
    @Test(priority = 5, dependsOnMethods = "getNotesList")
    public void deleteNote() {
        Response response = given()
                .header("x-auth-token", token)
                .when()
                .delete("/notes/" + noteId)
                .then()
                .extract().response();

        Assert.assertEquals(response.getStatusCode(), 200,
                "TC-API-05 FAILED: Delete status code is not 200");
        System.out.println("TC-API-05 PASSED: Note deleted successfully");
    }

    // TC-API-06: Deleted Note Is Not Returned in GET Response
    @Test(priority = 6, dependsOnMethods = "deleteNote")
    public void deletedNoteNotInList() {
        Response response = given()
                .header("x-auth-token", token)
                .when()
                .get("/notes")
                .then()
                .extract().response();

        String responseBody = response.getBody().asString();
        Assert.assertFalse(responseBody.contains(noteId),
                "TC-API-06 FAILED: Deleted note still present in list");
        System.out.println("TC-API-06 PASSED: Deleted note not in list");
    }

    // TC-NEG-01: API Returns 401 for Unauthorized Request
    @Test(priority = 7)
    public void unauthorizedRequestReturns401() {
        Response response = given()
                .header("x-auth-token", "invalidtoken123")
                .when()
                .get("/notes")
                .then()
                .extract().response();

        Assert.assertEquals(response.getStatusCode(), 401,
                "TC-NEG-01 FAILED: Expected 401 but got " + response.getStatusCode());
        System.out.println("TC-NEG-01 PASSED: Unauthorized request returned 401");
    }

    // TC-NEG-02: API Returns Error When Deleting Non-Existent Note
    @Test(priority = 8)
    public void deleteNonExistentNoteReturnsError() {
        Response response = given()
                .header("x-auth-token", token)
                .when()
                .delete("/notes/nonexistentid123456")
                .then()
                .extract().response();

        Assert.assertNotEquals(response.getStatusCode(), 200,
                "TC-NEG-02 FAILED: Expected error but got 200");
        System.out.println("TC-NEG-02 PASSED: Delete non-existent note returned "
                + response.getStatusCode());
    }
}