package it.branduzzo.nextbedwars.blockdisplays.web.serverapi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.branduzzo.nextbedwars.blockdisplays.web.exception.BDEngineModelNotFoundException;
import it.branduzzo.nextbedwars.blockdisplays.web.exception.InvalidResponseException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class ServerApiFetcher {
    private static final String API = "https://block-display.com/server-api?id=";
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).followRedirects(HttpClient.Redirect.ALWAYS).build();

    private ServerApiFetcher() {}

    public static ServerApiModel fetch(String modelId) throws InvalidResponseException, BDEngineModelNotFoundException {
        return fetch(modelId, "bde");
    }

    public static ServerApiModel fetch(String modelId, String tag) throws InvalidResponseException, BDEngineModelNotFoundException {
        if (modelId == null || modelId.isBlank()) throw new IllegalArgumentException("modelId must not be empty");
        String url = API + modelId.trim();
        if (tag != null && !tag.isBlank() && !tag.equals("bde")) url += "&tag=" + tag;
        try {
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).header("User-Agent", "NextBedWars blocks/1.0").timeout(Duration.ofSeconds(15)).GET().build();
            HttpResponse<String> resp = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            String body = resp.body();
            int code = resp.statusCode();
            if (code == 200) {
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                if (!json.has("content")) throw new InvalidResponseException("Missing content field");
                JsonObject content = json.getAsJsonObject("content");
                return new ServerApiModel(modelId, content);
            } else if (code == 400 || code == 404) {
                String err = "Model not found or expired (id=" + modelId + ")";
                try {
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    if (json.has("error")) err = json.get("error").getAsString();
                } catch (Exception ignored) {}
                throw new BDEngineModelNotFoundException(err);
            } else if (code == 301 || code == 302 || code == 303 || code == 307 || code == 308) {
                String loc = resp.headers().firstValue("Location").orElse(null);
                if (loc != null) {
                    HttpRequest req2 = HttpRequest.newBuilder().uri(URI.create(loc)).header("User-Agent", "NextBedWars blocks/1.0").timeout(Duration.ofSeconds(15)).GET().build();
                    HttpResponse<String> resp2 = CLIENT.send(req2, HttpResponse.BodyHandlers.ofString());
                    if (resp2.statusCode() == 200) {
                        JsonObject json = JsonParser.parseString(resp2.body()).getAsJsonObject();
                        if (!json.has("content")) throw new InvalidResponseException("Missing content field");
                        return new ServerApiModel(modelId, json.getAsJsonObject("content"));
                    }
                }
                throw new InvalidResponseException("Redirect failed status " + code + ": " + body);
            } else {
                throw new InvalidResponseException("Unexpected status " + code + ": " + body);
            }
        } catch (IOException | InterruptedException e) {
            throw new InvalidResponseException(e);
        }
    }
}
