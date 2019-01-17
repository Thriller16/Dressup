package com.jwbinc.app.dressupapk;

public class GalleryImage {
    String clothType, dateAdded, url;

    public GalleryImage(String clothType, String dateAdded, String url) {
        this.clothType = clothType;
        this.dateAdded = dateAdded;
        this.url = url;
    }

    public String getClothType() {
        return clothType;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public String getUrl() {
        return url;
    }
}
