package com.zhenxiang.nyaa.api;

import java.util.HashMap;
import java.util.Map;

public enum ApiDataSource {
    NYAA_SI(0, "nyaa.si", NyaaReleaseCategory.values()),
    SUKEBEI_NYAA_SI(1, "sukebei.nyaa.si", SukebeiReleaseCategory.values());

    private int value;
    private String url;
    private ReleaseCategory[] categories;
    private static Map map = new HashMap<>();

    static {
        for (ApiDataSource dataSource : ApiDataSource.values()) {
            map.put(dataSource.value, dataSource);
        }
    }

    public static ApiDataSource valueOf(int dataSource) {
        return (ApiDataSource) map.get(dataSource);
    }

    private ApiDataSource(int value, String url, ReleaseCategory[] categories) {
        this.value = value;
        this.url = url;
        this.categories = categories;
    }

    public int getValue() {
        return value;
    }
    public String getUrl() {
        return url;
    }
    public ReleaseCategory[] getCategories() {return categories; };
}