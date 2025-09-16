package org.udesa.tp1_ing_sof.model;

public class User {
    private String userName;
    private String password;
    public int userId;

    public User(String userName, String password, int userId) {
        this.userName = userName;
        this.password = password;
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }
    public String getPassword() {
        return password;
    }
    public int getUserId() {
        return userId;
    }

}
