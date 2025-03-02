package me.julionxn.ttapp.endpoint;

import com.google.gson.JsonElement;

public record Response(int statusCode, JsonElement body) {
}
