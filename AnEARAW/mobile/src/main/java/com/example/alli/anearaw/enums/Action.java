package com.example.alli.anearaw.enums;

public enum Action
{
    enabled ("com.example.alli.anearaw.enabled"),
    disabled ("com.example.alli.anearaw.disabled"),
    p_enabled ("com.example.alli.anearaw.p_enabled"),
    p_disabled ("com.example.alli.anearaw.p_disabled");

    private final String mAction;

    Action(String action) {
        mAction = action;
    }

    public static Action getAction(String action) {
        switch (action) {
            case "com.example.alli.anearaw.enabled":
                return enabled;
            case "com.example.alli.anearaw.disabled":
                return disabled;
            case "com.example.alli.anearaw.p_enabled":
                return p_enabled;
            case "com.example.alli.anearaw.p_disabled":
                return p_disabled;
            default:
                return null;
        }
    }

    public String toString() {
        return this.mAction;
    }
}
