package project.gym;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    public static void init() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("DB 드라이버 로딩 성공");
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로딩 실패");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/gym_db?serverTimezone=UTC&useUnicode=yes&characterEncoding=UTF-8",
            "root", "rootroot"
        );
    }
}