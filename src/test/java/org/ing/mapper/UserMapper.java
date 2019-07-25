package org.ing.mapper;

import org.ing.model.User;

import java.util.List;

/**
 * Created by ing on 2019-07-13.
 */
public interface UserMapper {

    public List<User> selectUser();

    public User selectUserByCode(String code,String name);
}
