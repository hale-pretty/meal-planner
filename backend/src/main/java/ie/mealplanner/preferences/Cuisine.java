package ie.mealplanner.preferences;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum Cuisine {
    ANY("any", "Any cuisine"),
    AMERICAN("american", "American"),
    IRISH("irish", "Irish"),
    VIETNAMESE("vietnamese", "Vietnamese");

    private final String code;
    private final String label;

    Cuisine(String code, String label) {
        this.code = code;
        this.label = label;
    }

    @JsonValue
    public String code() { return code; }
    public String label() { return label; }

    @JsonCreator
    public static Cuisine fromCode(String code) {
        return Arrays.stream(values()).filter(value -> value.code.equals(code)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported choice"));
    }
}
