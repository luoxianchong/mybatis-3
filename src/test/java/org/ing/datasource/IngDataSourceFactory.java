package org.ing.datasource;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.apache.ibatis.datasource.DataSourceFactory;

import javax.sql.DataSource;
import java.util.Properties;

public class IngDataSourceFactory implements DataSourceFactory{
    private DataSource dataSource;

    @Override
    public void setProperties(Properties props){
        try{
             this.dataSource = DruidDataSourceFactory.createDataSource(props);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public DataSource getDataSource(){
        return this.dataSource;
    }
}
