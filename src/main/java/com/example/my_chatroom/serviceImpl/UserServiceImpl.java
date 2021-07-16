package com.example.my_chatroom.serviceImpl;

import com.example.my_chatroom.bean.UserBean;
import com.example.my_chatroom.mapper.UserMapper;
import com.example.my_chatroom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    //将DAO注入Service层
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserBean loginIn(String username, String password) {
        return userMapper.getInfo(username,password);
    }

    @Override
    public boolean signUp(String name, String password, String nickname) {
        if (name.isEmpty() || nickname.isEmpty()){
            return false;
        }
        else if(userMapper.getInfo(name,password)!=null){
            return false;
        }
        else if(!userMapper.ifAvailable(name)){
            return false;
        }
        else {
            userMapper.addInfo(name, password, nickname);
            return true;
        }
    }

    @Override
    public int getTotalNum() {
        return userMapper.getMaxId();
    }

    @Override
    public UserBean searchId(int id) {
        return userMapper.getInfoFromId(id);
    }

    @Override
    public void delById(int id){
        userMapper.deleteById(id);
    }

    @Override
    public void editById(int id, String edit_un, String edit_pw, String edit_nn) {
        userMapper.editUsername(id, edit_un);
        userMapper.editPassword(id, edit_pw);
        userMapper.editNickname(id, edit_nn);
    }

    @Override
    public List<UserBean> searchName(String searchName) {
        List<Integer> idList1 = userMapper.searchByName(searchName);
        List<Integer> idList2 = userMapper.searchByNickName(searchName);
        List<Integer> idList;
        idList1.addAll(idList2);
        idList = idList1.stream().distinct().collect(Collectors.toList());
        List<UserBean> userBeanList = new ArrayList<>();
        for (int i: idList){
            userBeanList.add(userMapper.getInfoFromId(i));
        }
        return userBeanList;
    }
}

