package com.example.my_chatroom.bean;

public class UserBean {
    private int id;
    private String username;
    private String password;
    private String nickname;
    private int money;

    public UserBean(int id, String username, String password, String nickname, int money) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.money = money;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }
}

