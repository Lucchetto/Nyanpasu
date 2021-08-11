package com.zhenxiang.nyaa.api;

import java.util.HashMap;
import java.util.Map;

public enum ApiDataSource {
    NYAA_SI(0);

    private int value;
    private static Map map = new HashMap<>();

    static {
        for (ApiDataSource dataSource : ApiDataSource.values()) {
            map.put(dataSource.value, dataSource);
        }
    }

    public static ApiDataSource valueOf(int dataSource) {
        return (ApiDataSource) map.get(dataSource);
    }

    public static ReleaseCategory[] getCategories(ApiDataSource dataSource) {
        switch (dataSource) {
            case NYAA_SI:
                return NyaaReleaseCategory.values();
            default:
                return null;
        }
    }

    public static String getUrl(ApiDataSource dataSource) {
        switch (dataSource) {
            case NYAA_SI:
                return "nyaa.si";
            default:
                return null;
        }
    }

    private ApiDataSource(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}