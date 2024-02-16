package com.love._util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class JsonUtil {
    private static final Gson gson = new Gson();

    public interface Json{}
    private static class Impl implements Json{
        JsonElement root;
        JsonElement current;
        Impl(JsonElement element){this.root =current= element;}
    }
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }
    public static <T>T toBean(String json,Class<T> clazz){
        return gson.fromJson(json, clazz);
    }
    public static <T>T toBean(JsonElement json,Class<T> clazz){
        return gson.fromJson(json, clazz);
    }
    public static Json to(String json){
        return new Impl(toBean(json,JsonElement.class));
    }
    private static JsonElement ele(Json ctx,String ...keys){
        if(ctx instanceof Impl c){
            JsonObject cur = c.current.getAsJsonObject();
            for(int i=0;i<keys.length-1;i++){
                cur = cur.get(keys[i]).getAsJsonObject();
            }
            return cur.get(keys[keys.length-1]);
        }
        return JsonNull.INSTANCE;
    }
    public static String readString(Json ctx,String ...keys){
        return ele(ctx, keys).toString();
    }
}
