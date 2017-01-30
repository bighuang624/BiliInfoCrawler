package homework_5;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBC {
	
	public static Connection getConnection(){
		Connection conn = null;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Bilibili2","root","");
		} catch(Exception e){
			e.printStackTrace();
		}
		return conn;
	}

	public static void insertBilibiliData(Connection conn, Bilibili bilibili) throws SQLException{
		String insertSQL = "INSERT INTO bilibili (aid,tid,title,tname,favorite,coin,author) VALUES (?,?,?,?,?,?,?)";
		PreparedStatement ps = conn.prepareStatement(insertSQL);
		ps.setInt(1, bilibili.getAid());
		ps.setInt(2, bilibili.getTid());
		ps.setString(3, bilibili.getTitle());
		ps.setString(4, bilibili.getTname());
		ps.setInt(5, bilibili.getFavorite());
		ps.setInt(6, bilibili.getCoin());
		ps.setString(7, bilibili.getAuthor());
		ps.execute();
	}
	
	public static void updateBilibiliData(Connection conn, int aid, Bilibili bilibili) throws SQLException{
		String updateSQL = "UPDATE bilibili SET tid = ?,title = ?,tname = ?,favorite = ?,coin = ?,author = ? WHERE aid = ?";
		PreparedStatement ps = conn.prepareStatement(updateSQL);
		ps.setInt(1, bilibili.getTid());
		ps.setString(2, bilibili.getTitle());
		ps.setString(3, bilibili.getTname());
		ps.setInt(4, bilibili.getFavorite());
		ps.setInt(5, bilibili.getCoin());
		ps.setString(6, bilibili.getAuthor());
		ps.setInt(7, bilibili.getAid());
		ps.execute();
	}
	
	public static void deleteBilibiliData(Connection conn, int aid) throws SQLException{
		String deleteSQL = "DELETE FROM bilibili WHERE aid = ?";
		PreparedStatement ps = conn.prepareStatement(deleteSQL);
		ps.setInt(1, aid);
		ps.execute();
	}
	
	public static void searchBilibiliData(Connection conn, int aid) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM bilibili WHERE aid = ?");
		ps.setInt(1, aid);
		
		ResultSet rs = ps.executeQuery();
		
		while(rs.next()){
			System.out.print("aid:"+rs.getInt("aid")+" ");
			System.out.print("tid:"+rs.getInt("tid")+" ");
			System.out.print("title:"+rs.getString("title")+" ");
			System.out.print("tname:"+rs.getString("tname")+" ");
			System.out.print("favorite:"+rs.getInt("favorite")+" ");
			System.out.print("coin:"+rs.getInt("coin")+" ");
			System.out.print("author:"+rs.getString("author")+" ");
			System.out.println();
		}
	}
	
	public static String[] searchTop3(Connection conn, int tID) throws SQLException{
        PreparedStatement ps = conn.prepareStatement("SELECT aid,title,favorite FROM `Bilibili`.`bilibili` WHERE tid="+tID+" ORDER BY favorite DESC limit 3");
		
		ResultSet rs = ps.executeQuery();
		String[] info = new String[9];
		int i = 0;
		
		while(rs.next()){
			info[i] = String.valueOf(rs.getInt("aid"));
			info[i+3] = rs.getString("title");
			info[i+6] = String.valueOf(rs.getInt("favorite"));
			i++;
		}
		
		return info;
	}
}
