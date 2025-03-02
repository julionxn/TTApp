package me.julionxn.ttapp.endpoint;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import me.julionxn.ttapp.endpoint.model.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class UsersManager {

    protected final Gson gson = new Gson();

    public CompletableFuture<List<User>> getAllItems(Consumer<Error> err) {
        return get("user", err, this::deserializeJsonList);
    }

    public <K> CompletableFuture<K> get(String path, Consumer<Error> err, Function<JsonElement, K> fn){
        System.out.println("path: " + path);
        return EndpointsManager.getInstance()
                .getFromAPI(path)
                .thenApply(response -> response(response, err, fn));
    }

    protected List<User> deserializeJsonList(JsonElement jsonResponse) {
        return gson.fromJson(jsonResponse.getAsJsonArray(), new TypeToken<List<User>>() {}.getType());
    }

    public CompletableFuture<User> postItem(User obj, Consumer<Error> err){
        return EndpointsManager.getInstance().postToAPI("user", obj)
                .thenApply(response ->
                        response(response, err, body -> gson.fromJson(body, User.class))
                );
    }

    protected <K> K response(Response response, Consumer<Error> err, Function<JsonElement, K> fn){
        int statusCode = response.statusCode();
        System.out.println("statusCode: " + statusCode);
        if (statusCode != 200 && statusCode != 204) {
            System.out.println("bad status");
            err.accept(badStatus(statusCode));
            return null;
        }
        JsonElement body = response.body();
        if (body == null) {
            System.out.println("bad body");
            err.accept(emptyBody());
            return null;
        }
        System.out.println("================================");
        return fn.apply(body);
    }

    protected Error badStatus(int code){
        return new Error(ErrorType.STATUS_CODE, String.valueOf(code));
    }

    protected Error emptyBody(){
        return new Error(ErrorType.EMPTY_BODY, "Not expected empty body response.");
    }

    public record Error(ErrorType errorType, String message){}

    public enum ErrorType {
        STATUS_CODE,
        EMPTY_BODY
    }

}
