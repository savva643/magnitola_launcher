package com.keeppixel.magnitalo;

// HomeGridItem class
public class HomeGridItem {
    public enum Type {
        SPEED_WIDGET, NAVIGATION_WIDGET, MUSIC_PLAYER, QUICK_APP, WEATHER_WIDGET
    }

    public Type type;
    public String title;
    public int iconRes;
    public int spanSize;
    public int rowSpan;

    public HomeGridItem(Type type, String title, int iconRes, int spanSize, int rowSpan) {
        this.type = type;
        this.title = title;
        this.iconRes = iconRes;
        this.spanSize = spanSize;
        this.rowSpan = rowSpan;
    }
}
