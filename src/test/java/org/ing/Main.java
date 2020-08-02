package org.ing;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.google.gson.Gson;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.*;
import org.ing.mapper.UserMapper;
import org.ing.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ing on 2019-07-13.
 */
public class Main {

    public static void main(String[] args) throws IOException {


        String config="config.xml";
        InputStream inputStream= Resources.getResourceAsStream(config);

        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(inputStream);

        SqlSession session = factory.openSession();

       // Connection connection = session.getConnection();

        List<User> list=session.selectList("org.ing.mapper.UserMapper.selectUser");

        List<User> users=session.getMapper(UserMapper.class).selectUser();

        System.out.println(new Gson().toJson(users));

        System.out.println(new Gson().toJson(list));

        Map<String,String> map=new HashMap();
        map.put("code","7894561230");
        map.put("name","Ten");
        session.select("org.ing.mapper.UserMapper.selectUserByCode",map, new ResultHandler() {
            @Override
            public void handleResult(ResultContext resultContext) {
               Object obj= resultContext.getResultObject();

                System.out.println(obj);
            }
        });





    }
}
