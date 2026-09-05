package com.student.common;

import java.util.Map;

public final class MapUtils {
    private MapUtils() { }
    public static String string(Map<String, Object> map, String key) { return map.get(key) == null ? null : String.valueOf(map.get(key)); }
    public static Long id(Map<String, Object> map) { return map.get("id") == null ? null : Long.valueOf(String.valueOf(map.get("id"))); }
}
