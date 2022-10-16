package com.example.vaccination.myutils;

import com.example.vaccination.data.User;

public class MyCurrentUser {
    private static User user;

    public static void setUser(User user) {
        MyCurrentUser.user = user;
    }

    public static User getUser() {
        return user;
    }
}