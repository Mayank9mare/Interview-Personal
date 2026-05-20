package com.slice.library.model;

public class User {

    private final String username;
    private final String name;
    private final String password;

    public User(String username, String name, String password) {
        this.username = username;
        this.name     = name;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getName()     { return name; }
    public String getPassword() { return password; }

    @Override
    public String toString() {
        return String.format("User{username='%s', name='%s'}", username, name);
    }
}
