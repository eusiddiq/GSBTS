package com.team17.gsbts;

public class ModelFeed {
    String timeStamp, notificationText;

    public ModelFeed(String timeStamp, String notificationText) {
        this.timeStamp = timeStamp;
        this.notificationText = notificationText;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getNotificationText() {
        return notificationText;
    }

    public void setNotificationText(String notificationText) {
        this.notificationText = notificationText;
    }
}
