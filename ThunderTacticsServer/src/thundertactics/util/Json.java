package thundertactics.util;

import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * JSON utility methods.
 * @author Paul Nechifor
 */
public class Json {
    private static final Gson GSON;

    static {
        GSON = new GsonBuilder().create();
    }
    
    private Json() {
    }
    
    public static <E> E fromString(String message, Class<E> type)
            throws IOException {
        return GSON.fromJson(message, type);
    }
    
    public static String toString(Object object)
            throws IOException {
        return GSON.toJson(object);
    }
}
