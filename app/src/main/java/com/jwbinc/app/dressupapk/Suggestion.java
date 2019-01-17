package com.jwbinc.app.dressupapk;

public class Suggestion {
    String url, clothType;

    public Suggestion(String url, String clothType) {
        this.url = url;
        this.clothType = clothType;
    }

    public String getUrl() {
        return url;
    }

    public String getClothType() {
        return clothType;
    }
}
