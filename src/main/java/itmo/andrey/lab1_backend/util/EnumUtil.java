package itmo.andrey.lab1_backend.util;

import java.util.Arrays;

public class EnumUtil {

    public static String[] getEnumValues(Class<?> enumType) {
        if (!enumType.isEnum()) {
            throw new IllegalArgumentException("Тип должен быть перечислением (enum)");
        }
        return Arrays.stream(enumType.getEnumConstants())
                .map(Object::toString)
                .toArray(String[]::new);
    }
}
