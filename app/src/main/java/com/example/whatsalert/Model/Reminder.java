package com.example.whatsalert.Model;

public class Reminder {

    private int id;
    private String time;
    private String date;
    private String contactNumber;
    private String message;
    private boolean isNotified;
    private String contactName;

    // Default constructor
    public Reminder() {}

    // Constructor to initialize the reminder fields including id
    public Reminder(int id, String time, String date, String contactNumber, String message, String contactName) {
        this.id = id;
        this.time = time;
        this.date = date;
        this.contactNumber = contactNumber;
        this.message = message;
        this.contactName = contactName;
    }

    // Getter for id
    public int getId() {
        return id;
    }

    // Getter for time
    public String getTime() {
        return time;
    }

    // Getter for date
    public String getDate() {
        return date;
    }

    // Getter for contact number
    public String getContactNumber() {
        return contactNumber;
    }

    // Getter for message
    public String getMessage() {
        return message;
    }

    // Setter for message
    public void setMessage(String message) {
        this.message = message;
    }

    // Getter for notified status
    public boolean isNotified() {
        return isNotified;
    }

    // Setter for notified status
    public void setNotified(boolean notified) {
        isNotified = notified;
    }

    public String getContactName(){ return  contactName; }

}
