package enums;

import java.util.LinkedHashMap;
import java.util.Map;

public enum ProductType {
    CRAB,
    SALMON,
    LOBSTER,
    TUNA,
    GENERIC_FISH,
    MISC;

    public static Map<String, String> options() {
        LinkedHashMap<String, String> values = new LinkedHashMap<String, String>();
        for (ProductType t : ProductType.values()) {
            String niceName = t.name().substring(0, 1).toUpperCase() + t.name().substring(1).toLowerCase().replace("_", " ");
            values.put(t.name(), niceName);
        }
        return values;
    }
}