package com.horsehour.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20130428
 */
public class JDBCUtil {
	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String DBURL = "jdbc:mysql://localhost:3306/TutorCenter";
	private static Properties properties = new Properties();

	/**
	 * 启动连接
	 * 
	 * @return connection to data base
	 */
	public static Connection getConnection(){
		properties.setProperty("user", "root");
		properties.setProperty("password", "root");

		// 设置编码,防止乱码
		properties.setProperty("characterEncoding", "gb2312");

		Connection conn = null;
		try {
			Class.forName(DRIVER);// 初始化驱动
			conn = DriverManager.getConnection(DBURL, properties);// 创建连接
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return conn;
	}

	/**
	 * 利用配置文件获取连接
	 * 
	 * @param driver
	 * @param dbHost
	 * @param prop
	 * @return connection to data base
	 */
	public static Connection getConnection(String driver, String dbHost, Properties prop){
		Connection conn = null;
		try {
			Class.forName(driver);// 初始化驱动
			conn = DriverManager.getConnection(dbHost, prop);// 创建连接
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return conn;
	}

	/**
	 * 断开连接
	 * 
	 * @param conn
	 */
	public static void disconnect(Connection conn){
		try {
			if (!conn.isClosed())
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建一个Statement对象
	 * 
	 * @param conn
	 * @return create one Statement instance
	 */
	public static Statement createStatement(Connection conn){
		Statement stmt = null;
		try {
			if (!conn.isClosed())
				stmt = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return stmt;
	}

	/**
	 * 创建一个PreparedStatement对象
	 * 
	 * @param conn
	 * @param sql
	 * @return create one PreparedStatement instance
	 */
	public static PreparedStatement prepareStatement(Connection conn, String sql){
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return pstmt;
	}

	/**
	 * 执行语句sql
	 * 
	 * @param stmt
	 * @param sql
	 * @return true if the first result is a ResultSet object; false if it is an
	 *         update count or there are no results
	 */
	public static boolean execute(Statement stmt, String sql){
		boolean bool = false;
		try {
			bool = stmt.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return bool;
	}

	/**
	 * 如果执行的是插入INSERT、更新UPDATE和删除DELETE，可以使用executeUpdate()
	 * 
	 * @param stmt
	 * @param sql
	 * @return 执行更新操作(insert, update, delete)
	 */
	public static int executeUpdate(Statement stmt, String sql){
		int code = 0;
		try {
			if (!stmt.isClosed())
				code = stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return code;
	}

	/**
	 * execute the PreparedStatement instance
	 * 
	 * @param pstmt
	 * @return true if succeed, false elsewise
	 */
	public static boolean execute(PreparedStatement pstmt){
		boolean ok = false;
		try {
			ok = pstmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ok;
	}

	/**
	 * 执行更新操作
	 * 
	 * @param pstmt
	 * @return code
	 */
	public static int executeUpdate(PreparedStatement pstmt){
		int code = 0;
		try {
			if (!pstmt.isClosed())
				code = pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return code;
	}

	/**
	 * 执行以后并返回受影响的结果集
	 * 
	 * @param pstmt
	 * @return result set from data base
	 */
	public static ResultSet executeQuery(PreparedStatement pstmt){
		ResultSet rs = null;
		try {
			rs = pstmt.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return rs;
	}

	/**
	 * 创建一个表格,并定义其格式
	 * 
	 * @param conn
	 * @param tblName
	 * @param args
	 * @return
	 */
	public static boolean createTable(Connection conn, String tblName, String args){
		String sql = "CREATE TABLE IF NOT EXISTS " + tblName + "(" + args + ")";
		Statement stmt = createStatement(conn);
		return execute(stmt, sql);
	}

	/**
	 * 创建导师信息表
	 * 
	 * @param tblName
	 * @param conn
	 */
	public static void buildTutorTable(String tblName, Connection conn){
		String sql = "ProfessorInfoID int UNIQUE AUTO_INCREMENT PRIMARY KEY,ProfessorPostID int null,"
		        + "UniversityID int null,UniversityName nvarchar(32) null,EducationBackgroundID int null,"
		        + "ProfessorTypeID int null,RecruitPostgraduate int null,ProfessorClicks int null default 0,"
		        + "displayAble int null,UsersID int null default 0,ProfessorPhoto nvarchar(100) null,"
		        + "ProfessorName nvarchar(32) null,ProfessorSex nvarchar(6) null,ProfessorBirthday nvarchar(32) null,"
		        + "ProfessorEmail nvarchar(64) null,ProfessorPhone nvarchar(64) null,RecruitSubject nvarchar(200) null,"
		        + "ProfessorField text null,ProfessorTypes nvarchar(500) null,ProfessorIntroduce text null";

		createTable(conn, tblName, sql);
	}

	/**
	 * 获取编译后的PreparedStatement对象
	 * 
	 * @param conn
	 * @return
	 */
	public static PreparedStatement getPreparedStatement(Connection conn){
		PreparedStatement pstmt = null;
		String preparedSql = "INSERT INTO TutorNet(ProfessorPostID,UniversityID,UniversityName,"
		        + "EducationBackgroundID,ProfessorTypeID,RecruitPostgraduate,ProfessorPhoto,ProfessorName,"
		        + "ProfessorSex,ProfessorBirthday,ProfessorEmail,ProfessorPhone,RecruitSubject,"
		        + "ProfessorField,ProfessorTypes,ProfessorIntroduce) VALUES" + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		pstmt = prepareStatement(conn, preparedSql);
		return pstmt;
	}

	/**
	 * 提供PreparedStatement对象的插入数据
	 * 
	 * @param pstmt
	 * @param maps
	 */
	public static void setDatum(PreparedStatement pstmt, Map<String, String> maps){
		try {
			String intro = maps.get("Intro");
			String sex = "";
			if (intro != null) {
				intro = intro.replaceAll("'", "''");
				if (intro.contains("男，") || intro.contains("男,"))
					sex = "男";
				else if (intro.contains("女，") || intro.contains("女,"))
					sex = "女";
			}
			pstmt.setString(16, intro);
			maps.remove("Intro");// 已经导入，可以从maps删除

			String title = maps.get("Title");
			int titleId = 0;
			if (title.equals("教授"))
				titleId = 4;
			else if (title.equals("副教授"))
				titleId = 3;
			else if (title.equals("讲师"))
				titleId = 2;
			else if (title.equals("研究员"))
				titleId = 9;
			pstmt.setInt(1, titleId);

			pstmt.setInt(2, 245);
			pstmt.setString(3, "大连理工大学");
			String degree = maps.get("Degree");
			int degreeId = 0;
			if (degree.equals("博士"))
				degreeId = 4;
			else if (degree.equals("硕士"))
				degreeId = 3;
			else if (degree.equals("学士"))
				degreeId = 2;

			pstmt.setInt(4, degreeId);

			String level = maps.get("Level");
			int levelId = 0;
			if (level.equals("博导"))
				levelId = 2;
			else if (level.equals("硕导"))
				levelId = 1;

			pstmt.setInt(5, levelId);

			int recruitId = 0;
			if (levelId == 1)
				recruitId = 1;
			else
				recruitId = 2;

			pstmt.setInt(6, recruitId);
			pstmt.setString(7, "ProfessorPhoto1");
			pstmt.setString(8, maps.get("Name"));
			pstmt.setString(9, sex);// 性别
			pstmt.setString(10, maps.get("Birth"));
			pstmt.setString(11, maps.get("Email"));
			pstmt.setString(12, maps.get("Phone"));

			// 对于有多个招生方向，需要重复绑定，因此可以第一个插入，其他循环
			pstmt.setString(13, maps.get("Subject"));
			pstmt.setString(14, maps.get("Field"));
			pstmt.setString(15, maps.get("Expert"));

			System.out.println(maps.get("Name"));
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * 从文件中导入数据到数据库中:只有一组数据
	 * 
	 * @param src
	 * @param conn
	 */
	public static void insertDatum(String src, Connection conn){
		Map<String, String> maps = new HashMap<String, String>();
		PreparedStatement pstmt = getPreparedStatement(conn);
		setDatum(pstmt, maps);
		execute(pstmt);
	}

	/**
	 * 从文件中导入数据到数据库：存在多组数据
	 * 
	 * @param src
	 * @param encoding
	 * @param lineCount
	 * @param conn
	 */
	public static void insertDatum(String src, String encoding, int lineCount, Connection conn){
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(src), encoding));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		PreparedStatement pstmt = getPreparedStatement(conn);
		try {
			while (br.readLine() != null) {
				HashMap<String, String> maps = new HashMap<String, String>();
				//				FileUtil.loadResource(br, maps, lineCount);
				setDatum(pstmt, maps);
				execute(pstmt);
			}
			br.close();
			pstmt.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * 从数据库中检索数据
	 * 
	 * @param sql
	 * @param conn
	 * @return retrieval data from data base
	 */
	public static ResultSet getResultSet(String sql, Connection conn){
		Statement stmt = createStatement(conn);
		ResultSet rs = null;
		try {
			rs = stmt.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return rs;
	}

	/**
	 * 关闭ResultSet对象
	 * 
	 * @param rs
	 */
	public static void closeResultSet(ResultSet rs){
		try {
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * 获取ResultSet的行数
	 * 
	 * @param rs
	 * @return
	 */
	public static int getRowNum(ResultSet rs){
		int sz = 0;
		try {
			rs.last(); // cursor定位到最后一行
			sz = rs.getRow(); // 获取最后一行的行标
			rs.beforeFirst(); // 恢复cursor初始位置,避免使用rs时出错
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
		return sz;
	}
}