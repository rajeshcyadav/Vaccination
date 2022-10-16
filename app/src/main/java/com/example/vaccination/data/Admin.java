package com.example.vaccination.data;

public class Admin extends BaseModel{
    private boolean isAdmin;
    private long requestCounter=0;
    private long userCount=0;

    public Admin() {
    }

    public long getRequestCounter() {
        return requestCounter;
    }

    public void setRequestCounter(long requestCounter) {
        this.requestCounter = requestCounter;
    }

    public long getUserCount() {
        return userCount;
    }

    public void setUserCount(long userCount) {
        this.userCount = userCount;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "isAdmin=" + isAdmin +
                '}';
    }
}
