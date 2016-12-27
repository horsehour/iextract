package com.horsehour.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * <p>Integrated Mysql Connector and related actions.
 * <li>DBConnection(): 加载数据库驱动程序、连接数据库,实例化Statement对象
 * <li>DBConnection(String sql)-->加载驱动、连接数据库,通过sql语句实例化PrepareStatement对象
 * <li>Connection getConnection():生成连接对象
 * <li>void close():关闭连接,同时执行PrepareStatement、Statement对象资源的释放
 * <li>void prepareStatement(String sql):根据sql生成PrepareStatement对象
 * <li>PreparedStatement getPreparedStatement():返回PrepareStatement对象
 * <li>void setString(int index,String value):设置预处理SQL语句参数值（String）
 * <li>void setInt(int index,int value):同理（int）...long,float,boolean,InputStream
 * <li>void executeUpdate():执行INSERT、DELETE、UPDATE操作
 * <li>ResultSet executeQuery():执行查询操作,返回单个ResultSet对象
 * <li>Statement getStatement():返回Statement对象
 * <li>void executeUpdate(String sql):执行INSERT、DELETE、UPDATE操作
 * <li>ResultSet executeQuery(String sql):执行查询操作,返回单个ResultSet对象
 * @author Chunheng Jiang
 * @version 1.0
 */
public class DBConnection {
	private Connection conn = null;
	private Statement stmt = null;
	private PreparedStatement prepstmt = null;
	private final String URL = "jdbc:mysql://localhost:3306/echo"; // URL指向要访问的数据库名echo
	private final String USER = "root"; // MySQL配置时的用户名
	private final String PASSWORD = "chiang"; // MySQL配置时的密码

	public DBConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");// 加载数据库驱动程序
			conn = DriverManager.getConnection(URL, USER, PASSWORD);// 连接数据库
			stmt = conn.createStatement();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public DBConnection(String sql) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(URL, USER, PASSWORD);
			this.prepareStatement(sql);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public Connection getConnection(){
		return conn;
	}

	public void prepareStatement(String sql){
		try {
			prepstmt = conn.prepareStatement(sql);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void setString(int index, String value){
		try {
			prepstmt.setString(index, value);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void setInt(int index, int value){
		try {
			prepstmt.setInt(index, value);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void setBoolean(int index, boolean value){
		try {
			prepstmt.setBoolean(index, value);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void setDate(int index, Date value) throws SQLException{
		try {
			prepstmt.setDate(index, value);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void setLong(int index, long value) throws SQLException{
		try {
			prepstmt.setLong(index, value);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void setFloat(int index, float value) throws SQLException{
		try {
			prepstmt.setFloat(index, value);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void setBinaryStream(int index, InputStream in, int length) throws SQLException{
		try {
			prepstmt.setBinaryStream(index, in, length);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void clearParameters() throws SQLException{
		try {
			prepstmt.clearParameters();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public PreparedStatement getPreparedStatement(){
		return prepstmt;
	}

	public Statement getStatement(){
		return stmt;
	}

	public ResultSet executeQuery(String sql){
		try {
			if (stmt != null)
				return stmt.executeQuery(sql);
			else
				return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ResultSet executeQuery(){
		try {
			if (prepstmt != null)
				return prepstmt.executeQuery();
			else
				return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void executeUpdate(String sql){
		try {
			if (stmt != null)
				stmt.executeUpdate(sql);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void executeUpdate(){
		try {
			if (prepstmt != null)
				prepstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void close(){
		try {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
			if (prepstmt != null) {
				prepstmt.close();
				prepstmt = null;
			}
			conn.close();
			conn = null;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
}