package com.example.my_chatroom.service;

import com.example.my_chatroom.bean.UserBean;

import java.util.List;

public interface UserService {

    UserBean loginIn(String name, String password);

    boolean signUp(String name, String password, String nickname);

    int getTotalNum();

    UserBean searchId(int id);

    void delById(int id);

    void editById(int id, String edit_un, String edit_pw, String edit_nn);

    List<UserBean> searchName(String searchName);
}
