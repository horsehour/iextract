package com.horsehour.search.crawler.prof;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import com.horsehour.util.JDBCUtil;

/**
 * ProfPorter主要执行同数据库的交互，查找或插入Professor数据
 * 
 * @author Chunheng Jiang
 * @version 1.0
 * @since 20120813
 */
public class ProfPorter {
	/**
	 * 利用同数据库的连接，获取编译后的PreparedStatement对象
	 * 
	 * @param conn
	 * @return PreparedStatement
	 */
	public static PreparedStatement getPreparedStatement(Connection conn){
		PreparedStatement pstmt = null;
		// TODO:根据数据库实际情况调整
		// name,sex,birth,univ,univId,email,phone,title,level,degree,expert,
		// field,subject(单个导师允许多个专业招生),certified招生类型（暂定博导都可招，硕导只招硕士）,resume,photo
		String preparedSql = "INSERT INTO TutorNet(ProfessorName,ProfessorSex,ProfessorBirthday,"
		        + "UniversityName,UniversityID,ProfessorEmail,ProfessorPhone,"
		        + "ProfessorPostID,ProfessorTypeID,EducationBackgroundID,ProfessorTypes,"
		        + "RecruitSubject,ProfessorField,RecruitPostgraduate,ProfessorIntroduce,ProfessorPhoto)"
		        + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		// TODO:由于photo是同profId关联的，因此只能等profId生成以后再导入
		pstmt = JDBCUtil.prepareStatement(conn, preparedSql);

		return pstmt;
	}

	/**
	 * 直接将Professor对象传入提供插入数据
	 * 
	 * @param pstmt
	 * @param prof
	 */
	public static void setDatum(PreparedStatement pstmt, Professor prof){
		try {
			pstmt.setString(1, prof.getName());
			pstmt.setString(2, prof.getGender());
			pstmt.setString(3, prof.getBirth());

			pstmt.setString(4, prof.getUniv());
			pstmt.setInt(5, retrieveUnivId(prof.getUniv()));
			pstmt.setString(6, prof.getEmail());
			pstmt.setString(7, prof.getPhone());

			pstmt.setInt(8, getTitleId(prof.getTitle()));
			pstmt.setInt(9, getLevelId(prof.getLevel()));
			pstmt.setInt(10, getDegreeId(prof.getDegree()));
			pstmt.setString(11, prof.getExpert());

			pstmt.setString(12, retrieveSupSubjectName(prof.getSubject()));
			pstmt.setString(13, prof.getField());
			pstmt.setInt(14, getCertified(prof.getLevel()));// 招生资格（是否招生）

			pstmt.setString(15, prof.getResume());

		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * 使用map为PreparedStatement对象提供插入数据
	 * 
	 * @param pstmt
	 * @param maps
	 */
	public static void setDatum(PreparedStatement pstmt, HashMap<String, String> maps){
		try {
			pstmt.setString(1, maps.get("Name"));
			pstmt.setString(2, maps.get("Gender"));
			pstmt.setString(3, maps.get("Birth"));

			pstmt.setString(4, maps.get("Univ"));
			pstmt.setInt(5, retrieveUnivId(maps.get("Univ")));

			pstmt.setString(6, maps.get("Email"));
			pstmt.setString(7, maps.get("Phone"));

			pstmt.setInt(8, getTitleId(maps.get("Title")));
			pstmt.setInt(9, getLevelId(maps.get("Level")));
			pstmt.setInt(10, getDegreeId(maps.get("Degree")));
			pstmt.setString(11, maps.get("Expert"));

			pstmt.setString(12, maps.get("Subject"));
			pstmt.setString(13, maps.get("Field"));
			pstmt.setInt(14, getCertified(maps.get("Certified")));// 招生资格（是否招生）

			pstmt.setString(15, maps.get("Resume"));

			// TODO:由于photo是同profId关联的，因此只能等profId生成以后再导入
			pstmt.setString(16, maps.get("Photo"));

		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * 执行插入-将某个Professor的信息从文件中读取-固定格式
	 * 
	 * @param src
	 * @param enc
	 * @param conn
	 */
	public static void insertDatumFrom(String src, String enc, Connection conn){
		HashMap<String, String> map = new HashMap<String, String>();
		Util.loadResource(src, enc, map);// enc是src的编码
		PreparedStatement pstmt = getPreparedStatement(conn);
		setDatum(pstmt, map);
		JDBCUtil.execute(pstmt);
	}

	/**
	 * 执行插入-将多个Professor的信息从文件中读取-固定格式-固定行数lineCount一组
	 * 
	 * @param src
	 * @param enc
	 * @param lineCount
	 * @param conn
	 */
	public static void insertDatumFrom(String src, String enc, int lineCount, Connection conn){
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(src), enc));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}

		PreparedStatement pstmt = getPreparedStatement(conn);
		try {
			// TODO:注意第一行没有用，否者会丢失
			while (br.readLine() != null) {
				HashMap<String, String> maps = new HashMap<String, String>();
				Util.loadResource(br, maps, lineCount);
				setDatum(pstmt, maps);
				JDBCUtil.execute(pstmt);
			}
			// 关闭BufferedReader对象
			br.close();

			// 关闭PreparedStatement对象
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
	 * 检索学校信息表，查找学校的id
	 * 
	 * @param univ
	 * @return university's id
	 */
	public static int retrieveUnivId(String univ){
		int id = -1;
		Connection conn = JDBCUtil.getConnection();
		String sql = "SELECT * FROM UniversityInfo WHERE UniversityName = '" + univ + "'";
		ResultSet rs = JDBCUtil.getResultSet(sql, conn);
		try {
			id = rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
		return id;
	}

	/**
	 * 由名称检索二级学科subSubject的id
	 * 
	 * @param subject
	 * @return 检索结果集合
	 */
	public static ResultSet retrieveSubSubjectId(String subject){
		Connection conn = JDBCUtil.getConnection();
		String sql = "SELECT * FROM SecondCourse WHERE SecondCourseName='" + subject + "'";
		ResultSet rs = JDBCUtil.getResultSet(sql, conn);
		return rs;
	}

	/**
	 * 检查是否存在,如果存在则取出一级学科id，subjectId传入时作为二级学科id，输出时作为一级学科id
	 * 
	 * @param subjectId
	 * @param univId
	 * @return true is exist, false else
	 */
	public static boolean check(int subjectId, int univId){
		boolean ret = false;
		Connection conn = JDBCUtil.getConnection();
		String sql = "SELECT * FROM UniversityDepartments WHERE UniversityID=" + univId + " AND SecondCourseid= " + subjectId;
		ResultSet rs = JDBCUtil.getResultSet(sql, conn);
		// 如果检索存在
		if (JDBCUtil.getRowNum(rs) > 1)
			ret = true;
		try {
			subjectId = rs.getInt("FirstCourseid");// 解析一级学科id
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return ret;
	}

	/**
	 * 获取一级学科的id
	 * 
	 * @param subject
	 * @param univId
	 * @return super subject id
	 */
	public static int getSupSubjectId(String subject, int univId){
		int id = -1;
		ResultSet rs = retrieveSubSubjectId(subject);

		if (JDBCUtil.getRowNum(rs) > 1)
			try {

				while (rs.next()) {
					id = rs.getInt("SecondCourseid");
					if (check(id, univId))// 若存在
						break;
				}

			} catch (SQLException e) {
				e.printStackTrace();
				return -1;
			}

		return id;
	}

	/**
	 * 根据名称（二级学科、学校）检索二级学科的一级学科名称(supSubjectName)
	 * 
	 * @param subSubject
	 * @param univ
	 * @return sub subject name
	 */
	public static String retrieveSupSubjectName(String subSubject, String univ){
		String supSubjectName = "";
		int univId = retrieveUnivId(univ);
		int supSubjectId = getSupSubjectId(subSubject, univId);
		Connection conn = JDBCUtil.getConnection();

		String sql = "SELECT * FROM FirstCourse WHERE FirstCourseid=" + supSubjectId;
		ResultSet rs = JDBCUtil.getResultSet(sql, conn);
		if (JDBCUtil.getRowNum(rs) == 1)
			try {
				supSubjectName = rs.getString("FirstCourseName");
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		return supSubjectName;
	}

	/**
	 * 根据名称或者代码检索二级学科的一级学科名称
	 * 
	 * @param subSubject
	 * @param univId
	 * @return sup subject name
	 */
	public static String retrieveSupSubjectName(String subSubject, int univId){
		String supSubjectName = "";
		int supSubjectId = getSupSubjectId(subSubject, univId);
		Connection conn = JDBCUtil.getConnection();

		String sql = "SELECT * FROM FirstCourse WHERE FirstCourseid=" + supSubjectId;

		ResultSet rs = JDBCUtil.getResultSet(sql, conn);
		if (JDBCUtil.getRowNum(rs) == 1)
			try {
				supSubjectName = rs.getString("FirstCourseName");
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		return supSubjectName;
	}

	/**
	 * 由二级学科代码检索一级学科名称
	 * 
	 * @param subSubjectCode
	 * @return super subject name correspond to given sub subject code
	 */
	public static String retrieveSupSubjectName(String subSubjectCode){
		String supSubjectName = "";
		String supSubjectCode = subSubjectCode.substring(0, 3);// 取前四位
		// 利用一级学科代码检索其名称
		String sql = "SELECT * FROM FirstCourse WHERE FirstCourseCode=" + supSubjectCode;
		Connection conn = JDBCUtil.getConnection();
		ResultSet rs = JDBCUtil.getResultSet(sql, conn);
		try {
			supSubjectName = rs.getString("FirstCourseName");
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		return supSubjectName;
	}

	/**
	 * 根据职称判定其在数据库中存储的id号
	 * 
	 * @param title
	 * @return title id
	 */
	public static int getTitleId(String title){
		int id = -1;
		if (title.equals("教授"))
			id = 4;
		else if (title.equals("副教授"))
			id = 3;
		else if (title.equals("讲师"))
			id = 2;
		else if (title.equals("研究员"))
			id = 9;

		return id;
	}

	/**
	 * 如果职称本就是数值，但以字符串存储（不合理），解析出来
	 * 
	 * @param title
	 * @return title id
	 */
	public static int parseTitleId(String title){
		int id = -1;
		id = Integer.parseInt(title);
		return id;
	}

	/**
	 * 解析学校的id
	 * 
	 * @param univ
	 * @return university id
	 */
	public static int parseUnivId(String univ){
		int id = -1;
		id = Integer.parseInt(univ);
		return id;
	}

	/**
	 * 将level从名称转化到id
	 * 
	 * @param level
	 * @return profLevel
	 */
	public static int getLevelId(String level){
		int id = -1;
		if (level.equals("硕导"))
			id = 1;
		else if (level.equals("博导"))
			id = 2;
		return id;
	}

	/**
	 * 取得学历的id
	 * 
	 * @param degree
	 * @return degree id
	 */
	public static int getDegreeId(String degree){
		int id = -1;
		if (degree.equals("专科"))
			id = 1;
		else if (degree.equals("本科"))
			id = 2;
		else if (degree.equals("硕士"))
			id = 3;
		else if (degree.equals("博士"))
			id = 4;
		return id;
	}

	/**
	 * 简单地根据level确定
	 * 
	 * @param level
	 * @return certified level
	 */
	public static int getCertified(String level){
		int id = -1;
		int levelId = getLevelId(level);
		if (levelId == 1)// 硕导只能招生硕士
			id = 1;
		else if (levelId == 2)// 博导可招硕士和博士
			id = 3;

		return id;
	}
}