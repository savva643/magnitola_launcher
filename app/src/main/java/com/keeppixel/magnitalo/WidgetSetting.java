package com.keeppixel.magnitalo;

public class WidgetSetting {
    public String name;
    public boolean enabled;
    public int iconRes;

    public WidgetSetting(String name, boolean enabled, int iconRes) {
        this.name = name;
        this.enabled = enabled;
        this.iconRes = iconRes;
    }
}
