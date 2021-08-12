package com.zhenxiang.nyaa.api;

import java.util.HashMap;
import java.util.Map;

public enum ApiDataSource {
    NYAA_SI(0, "nyaa.si");

    private int value;
    private String url;
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

    private ApiDataSource(int value, String url) {
        this.value = value;
        this.url = url;
    }

    public int getValue() {
        return value;
    }
    public String getUrl() {
        return url;
    }
}