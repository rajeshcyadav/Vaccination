package com.example.vaccination.data;

import java.util.List;

public class User extends BaseModel{
    private String gender;
    private List<Request> requestList;

    public User() {
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public List<Request> getRequestList() {
        return requestList;
    }

    public void setRequestList(List<Request> requestList) {
        this.requestList = requestList;
    }

    @Override
    public String toString() {
        return "User{" +
                "gender='" + gender + '\'' +
                ", requestList=" + requestList +
                '}';
    }
}
