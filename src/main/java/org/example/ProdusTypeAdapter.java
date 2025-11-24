package org.example;

import com.google.gson.*;
import java.lang.reflect.Type;

// Clasa necesară pentru a permite serializarea/deserializarea corectă a claselor moștenite
// Adaugă un câmp "type" la obiectul JSON care specifică subclasa reală (Mancare, Bautura, etc.)
public class ProdusTypeAdapter implements JsonSerializer<Produs>, JsonDeserializer<Produs> {
    private static final String CLASSNAME = "CLASSTYPE";
    private static final String INSTANCE  = "INSTANCE";

    @Override
    public Produs deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonPrimitive prim = (JsonPrimitive) jsonObject.get(CLASSNAME);
        String className = prim.getAsString();
        try {
            Class<?> clazz = Class.forName(className);
            return context.deserialize(jsonObject.get(INSTANCE), clazz);
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e.getMessage());
        }
    }

    @Override
    public JsonElement serialize(Produs src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(CLASSNAME, src.getClass().getName());
        JsonElement element = context.serialize(src, src.getClass());
        jsonObject.add(INSTANCE, element);
        return jsonObject;
    }
}