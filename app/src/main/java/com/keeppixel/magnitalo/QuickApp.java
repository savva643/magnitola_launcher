package com.keeppixel.magnitalo;

public class QuickApp {
    public int iconRes;
    public int colorRes;
    public String name;
    public String packageName; // Optional for launching apps

    public QuickApp(int iconRes, int colorRes, String name, String packageName) {
        this.iconRes = iconRes;
        this.colorRes = colorRes;
        this.name = name;
        this.packageName = packageName;
    }
}
