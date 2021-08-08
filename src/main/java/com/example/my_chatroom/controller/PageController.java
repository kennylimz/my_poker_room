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
        return "index";
    }

    @RequestMapping("/login")
    public String show(){
        return "login";
    }

    @PostMapping(value = "/loginIn")
    public String login(String username, String password, Model model){
        UserBean userBean = userServiceImpl.loginIn(username, password);
        if(userBean!=null){
            curUserBean = new UserBean(
                    userBean.getId(),
                    userBean.getUsername(),
                    userBean.getPassword(),
                    userBean.getNickname(),
                    userBean.getMoney()
            );
            model.addAttribute("nickname", curUserBean.getNickname());
            return "chatroom";
        }else {
            return "loginError";
        }
    }

    @PostMapping(value = "/signUp")
    public String signUp(String username, String password, String nickname){
        if(userServiceImpl.signUp(username,password,nickname)){
            return "signupSuccess";
        }else {
            return "invalidReg";
        }
    }

    @RequestMapping(value = "/toSignUp")
    public String toSignUp() {return "signup";}

    @RequestMapping(value = "/manager")
    public String manager(Model model){
        int totalNum = userServiceImpl.getTotalNum();
        List<UserBean> userBeanList= new ArrayList<>();
        int cnt = 0;
        for (int i=0; i<totalNum; i++){
            if (userServiceImpl.searchId(i+1)!=null){
                userBeanList.add(userServiceImpl.searchId(i+1));
                cnt++;
            }
        }
        String[][] userArray = new String[cnt][5];
        for (int i=0; i<cnt; i++){
            userArray[i][0] = String.valueOf(userBeanList.get(i).getId());
            userArray[i][1] = userBeanList.get(i).getUsername();
            userArray[i][2] = userBeanList.get(i).getPassword();
            userArray[i][3] = userBeanList.get(i).getNickname();
            userArray[i][4] = String.valueOf(userBeanList.get(i).getMoney());
        }
        model.addAttribute("userList", userArray);
        return "manager";
    }

    @PostMapping(value = "/managerSignUp")
    public String managerSignup(String username, String password, String nickname, Model model) {
        userServiceImpl.signUp(username, password, nickname);
        return manager(model);
    }

    @PostMapping(value = "/delUser")
    public String delUser(String idToDel, Model model){
        int id = Integer.valueOf(idToDel);
        userServiceImpl.delById(id);
        return manager(model);
    }

    @PostMapping(value = "/managerLogIn")
    public String managerLogIn(String loginusername, String loginpassword, Model model){
        UserBean userBean = userServiceImpl.loginIn(loginusername, loginpassword);
        if(userBean!=null){
            curUserBean = new UserBean(
                    userBean.getId(),
                    userBean.getUsername(),
                    userBean.getPassword(),
                    userBean.getNickname(),
                    userBean.getMoney()
            );
            model.addAttribute("nickname", curUserBean.getNickname());
            return "chatroom";
        }else {
            return "loginError";
        }
    }

    @PostMapping(value = "/managerEdit")
    public String managerEdit(String edit_id, String edit_un, String edit_pw, String edit_nn, String edit_mn, Model model){
        int id = Integer.valueOf(edit_id);
        userServiceImpl.editById(id,edit_un,edit_pw,edit_nn,Integer.valueOf(edit_mn));
        System.out.println(edit_id+edit_un);
        return manager(model);
    }

    @PostMapping(value = "/managerSearch")
    public String managerSearch(String searchName, Model model){
        List<UserBean> userBeanList= userServiceImpl.searchName(searchName);
        int cnt = userBeanList.size();
        String[][] userArray = new String[cnt][5];
        for (int i=0; i<cnt; i++){
            userArray[i][0] = String.valueOf(userBeanList.get(i).getId());
            userArray[i][1] = userBeanList.get(i).getUsername();
            userArray[i][2] = userBeanList.get(i).getPassword();
            userArray[i][3] = userBeanList.get(i).getNickname();
            userArray[i][4] = String.valueOf(userBeanList.get(i).getMoney());
        }
        model.addAttribute("userList", userArray);
        return "searchPage";
    }
}
