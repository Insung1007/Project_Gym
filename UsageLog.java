package project.gym;

import java.sql.*;
import java.time.LocalDateTime;

public class UsageLog {


    public static void startUsage(int memberId, LocalDateTime startTime) {
        try (Connection conn = DBUtil.getConnection()) {
            // usage_log 테이블에 새로운 시작 시간 추가
            String sql = "INSERT INTO usage_log (member_id, start_time) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId); // 회원 id
            pstmt.setTimestamp(2, Timestamp.valueOf(startTime)); // 이용 시작 시간
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    public static void endUsage(int memberId, LocalDateTime endTime) {
        try (Connection conn = DBUtil.getConnection()) {
            // usage_log 테이블에서 end_time이 비어 있는 가장 최근 기록의 종료 시간만 채움
            String sql = "UPDATE usage_log SET end_time = ? WHERE member_id = ? AND end_time IS NULL ORDER BY start_time DESC LIMIT 1";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, Timestamp.valueOf(endTime)); // 퇴실 시간
            pstmt.setInt(2, memberId); // 회원 id
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
