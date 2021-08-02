package com.example.my_chatroom.controller;

import com.example.my_chatroom.bean.UserBean;
import com.example.my_chatroom.serviceImpl.UserServiceImpl;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class PageController {

    private UserBean curUserBean;

    //将Service注入Web层
    @Autowired
    UserServiceImpl userServiceImpl;

    @RequestMapping("/index")
    public String index(){
        return "chatroom_nodb";
    }
}
