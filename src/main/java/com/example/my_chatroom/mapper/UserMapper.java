
package com.example.my_chatroom.mapper;

import com.example.my_chatroom.bean.UserBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    UserBean getInfo(@Param("Name") String username, @Param("Password") String password);

    void addInfo(@Param("Name") String username, @Param("Password") String password, @Param("Nickname") String nickname, @Param("Money") int money);

    Boolean ifAvailable(@Param("Name") String username, @Param("Nickname") String nickname);

    UserBean getInfoFromId(@Param("id") int id);

    int getIdByNickname(@Param("Nickname") String Nickname);

    int getMaxId();

    void deleteById(@Param("id") int id);

    void editUsername(@Param("id") int id, @Param("Name") String edit_un);

    void editPassword(@Param("id") int id, @Param("Password") String edit_pw);

    void editNickname(@Param("id") int id, @Param("Nickname") String edit_nn);

    void editMoney(@Param("id") int id, @Param("Money") int edit_mn);

    List<Integer> searchByName(@Param("searchName") String searchName);

    List<Integer> searchByNickName(@Param("searchName") String searchName);

    void addMoney(@Param("id") int id, @Param("Money") int delta);
}
