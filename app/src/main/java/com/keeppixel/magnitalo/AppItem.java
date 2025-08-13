package com.keeppixel.magnitalo;

public class AppItem {
    public String name;
    public String packageName;
    public int iconRes;
    public android.graphics.drawable.Drawable iconDrawable;
    public int colorRes;

    // Constructor for resource-based icons
    public AppItem(String name, String packageName, int iconRes, int colorRes) {
        this.name = name;
        this.packageName = packageName;
        this.iconRes = iconRes;
        this.colorRes = colorRes;
        this.iconDrawable = null;
    }

    // Constructor for drawable icons
    public AppItem(String name, String packageName, android.graphics.drawable.Drawable iconDrawable, int colorRes) {
        this.name = name;
        this.packageName = packageName;
        this.iconDrawable = iconDrawable;
        this.colorRes = colorRes;
        this.iconRes = 0;
    }
}