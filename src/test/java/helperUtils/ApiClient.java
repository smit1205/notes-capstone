package helperUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {

    private static final String BASE_URL = "https://practice.expandtesting.com/notes/api";
    private final HttpClient client = HttpClient.newHttpClient();
    private final String token;

    public ApiClient(String token) {
        this.token = token;
    }

    // ── Login and return token ──
    public static String login(String email, String password) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String body = "email=" + email + "&password=" + password;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        return json.getJSONObject("data").getString("token");
    }

    // ── GET /notes ──
    public JSONArray getAllNotes() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/notes"))
                .header("x-auth-token", token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject json = new JSONObject(response.body());
        return json.getJSONArray("data");
    }

    // ── GET /notes — find by title ──
    public JSONObject getNoteByTitle(String title) throws Exception {
        JSONArray notes = getAllNotes();
        for (int i = 0; i < notes.length(); i++) {
            JSONObject note = notes.getJSONObject(i);
            if (note.getString("title").equals(title)) {
                return note;
            }
        }
        return null;
    }

    // ── DELETE /notes/{id} ──
    public int deleteNoteById(String id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/notes/" + id))
                .header("x-auth-token", token)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode();
    }
}