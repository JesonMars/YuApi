package com.pingo.yuapi.dto;

public class ConfigDTO {
    private String appName;
    private String appSlogan;
    private String welcomeImage;
    private String primaryColor;
    private String secondaryColor;
    private Object homeButtons;
    private Object tabBarConfig;
    private Object pageTexts;
    private Object notifications;

    public ConfigDTO() {}

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppSlogan() {
        return appSlogan;
    }

    public void setAppSlogan(String appSlogan) {
        this.appSlogan = appSlogan;
    }

    public String getWelcomeImage() {
        return welcomeImage;
    }

    public void setWelcomeImage(String welcomeImage) {
        this.welcomeImage = welcomeImage;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public Object getHomeButtons() {
        return homeButtons;
    }

    public void setHomeButtons(Object homeButtons) {
        this.homeButtons = homeButtons;
    }

    public Object getTabBarConfig() {
        return tabBarConfig;
    }

    public void setTabBarConfig(Object tabBarConfig) {
        this.tabBarConfig = tabBarConfig;
    }

    public Object getPageTexts() {
        return pageTexts;
    }

    public void setPageTexts(Object pageTexts) {
        this.pageTexts = pageTexts;
    }

    public Object getNotifications() {
        return notifications;
    }

    public void setNotifications(Object notifications) {
        this.notifications = notifications;
    }
}