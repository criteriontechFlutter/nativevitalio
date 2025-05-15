package com.critetiontech.ctvitalio.model;

/**
 * Created by Omron HealthCare Inc
 */

public class ReminderItem {
    private String hour;
    private String minute;

    public ReminderItem(String _hour, String _minute, String _days) {
        this.hour = _hour;
        this.minute = _minute;
        this.days = _days;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String _days) {
        this.days = _days;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String _minute) {
        this.minute = _minute;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String _hour) {
        this.hour = _hour;
    }

    private String days;
}
