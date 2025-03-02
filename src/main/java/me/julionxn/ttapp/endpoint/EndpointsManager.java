package me.julionxn.ttapp.endpoint;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class EndpointsManager {

    private static final EndpointsManager INSTANCE;

    static {
        INSTANCE = new EndpointsManager();
    }

    public static EndpointsManager getInstance() {
        return INSTANCE;
    }

    private final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private static final String endpoint = "http://localhost:8080/ttserver/api/v1/";

    private EndpointsManager(){}

    public final UsersManager USERS = new UsersManager();

    public CompletableFuture<Response> getFromAPI(String url) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint).resolve(url))
                .GET()
                .build();
        return sendRequest(client, request);
    }

    private CompletableFuture<Response> sendRequest(HttpClient client, HttpRequest request) {
        int timeout = 30;
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    int statusCode = response.statusCode();
                    String body = response.body();
                    JsonElement bodyJsonElement = JsonParser.parseString(body);
                    return new Response(statusCode, bodyJsonElement);
                })
                .orTimeout(timeout, TimeUnit.SECONDS);
    }

    public CompletableFuture<Response> postToAPI(String url, Object body) {
        HttpClient client = HttpClient.newHttpClient();
        String bodyJson = gson.toJson(body);
        System.out.println(bodyJson);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint).resolve(url))
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .header("Content-Type", "application/json")
                .build();
        return sendRequest(client, request);
    }


}
