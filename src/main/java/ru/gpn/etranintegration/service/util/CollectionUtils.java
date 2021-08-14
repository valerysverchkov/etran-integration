package ru.gpn.etranintegration.service.util;

import java.util.List;

public class CollectionUtils {

    private CollectionUtils() {
    }

    public static boolean isNotEmpty(List list) {
        return list != null && !list.isEmpty();
    }

    public static boolean isEmpty(List list) {
        return !isNotEmpty(list);
    }

}
