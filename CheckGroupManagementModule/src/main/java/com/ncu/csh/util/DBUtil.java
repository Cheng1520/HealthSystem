package com.ncu.csh.util;

import java.sql.*;

public class DBUtil {
    Connection con = null;
    ResultSet rs = null;

    //1、加载驱动
    static{
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    //2、建立连接
    private Connection getConnection() {
        try {
            con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/healthsystem?characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false","root","123123");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return con;
    }

    //3、执行查询sql语句--获得查询结果
    public ResultSet querySql(String sql, Object[] param){
        try {
            //获取连接对象
            Connection con = getConnection();
            //利用连接对象，打开接口，预加载sql
            PreparedStatement pstm = con.prepareStatement(sql);
            if(param != null){
                //将实际参数值 赋值 到sql语句里
                for(int i = 0;i<param.length;i++){
                    pstm.setObject(i+1,param[i]);
                }
            }
            //执行sql
            rs = pstm.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rs;
    }

    //3、执行新增、修改、删除sql语句 -- 获得结果
    public int iudSql(String sql,Object[] param){
        int num = 0;
        try {
            //获取连接对象
            Connection con = getConnection();
            //利用连接对象，打开接口，预加载sql
            PreparedStatement pstm = con.prepareStatement(sql);
            if(param != null){
                //将实际参数值 赋值 到sql语句里
                for(int i = 0;i<param.length;i++){
                    pstm.setObject(i+1,param[i]);
                }
            }
            //执行sql
            num = pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return num;
    }




    //4、关闭流
    public void close(){
        try {
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}