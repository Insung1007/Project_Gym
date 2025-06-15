package project.gym;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

public class MemberInfoFrame extends JFrame {
    private int memberId;
    private Float goalWeight, currentWeight;
    private LocalDate startDate, lastAttendanceDate;
    private int membershipMonths, attendanceDays;
    private JLabel durationLabel, goalWeightLabel, nowWeightLabel, diffLabel, attendanceLabel;
    private LocalDateTime liveStartTime;
    private Timer sessionTimer;

    public MemberInfoFrame(ResultSet rs) throws SQLException {
        setTitle("헬스메이트 - 회원 정보");
        setSize(600, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 메인 레이아웃
        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setContentPane(container);

        // DB에서 회원 정보 가져오기
        memberId = rs.getInt("id");
        String name = rs.getString("name");
        attendanceDays = rs.getInt("attendance_days");
        membershipMonths = rs.getInt("membership_months");
        startDate = rs.getDate("start_date").toLocalDate();
        Date lastDate = rs.getDate("last_attendance_date");
        lastAttendanceDate = (lastDate != null) ? lastDate.toLocalDate() : null;
        goalWeight = rs.getFloat("goal_weight");
        if (rs.wasNull()) goalWeight = null;
        currentWeight = rs.getFloat("current_weight");
        if (rs.wasNull()) currentWeight = null;
        String email = rs.getString("email");

        // 회원 정보 표시
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        infoPanel.add(label("👤 이름: " + name));
        infoPanel.add(label("📧 이메일: " + email));
        infoPanel.add(label("📅 만료까지: " + ChronoUnit.DAYS.between(LocalDate.now(), startDate.plusMonths(membershipMonths)) + "일"));

        attendanceLabel = label("✅ 출석 일수: " + attendanceDays + "일");
        infoPanel.add(attendanceLabel);

        goalWeightLabel = label("🎯 목표 체중: " + (goalWeight != null ? goalWeight + "kg" : "미설정"));
        infoPanel.add(goalWeightLabel);

        nowWeightLabel = label("📏 현재 체중: " + (currentWeight != null ? currentWeight + "kg" : "미설정"));
        infoPanel.add(nowWeightLabel);

        diffLabel = label(goalWeight != null && currentWeight != null ? getGoalDiffText(currentWeight) : "");
        infoPanel.add(diffLabel);

        durationLabel = label("⏱ 오늘 이용 시간: -");
        infoPanel.add(durationLabel);

        container.add(infoPanel, BorderLayout.CENTER);

        // 체중 입력 필드와 관련 버튼
        JPanel weightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        weightPanel.add(new JLabel("체중 입력:"));
        JTextField weightInput = new JTextField(8);
        weightPanel.add(weightInput);

        JButton goalBtn = new JButton("목표 체중");
        JButton nowBtn = new JButton("현재 체중");
        weightPanel.add(goalBtn);
        weightPanel.add(nowBtn);
        container.add(weightPanel, BorderLayout.SOUTH);

        // 버튼 영역
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton attendBtn = new JButton("출석");
        JButton startBtn = new JButton("이용 시작");
        JButton endBtn = new JButton("이용 종료");
        JButton chartBtn = new JButton("이용 그래프");
        JButton logoutBtn = new JButton("로그아웃");

        btnPanel.add(attendBtn);
        btnPanel.add(startBtn);
        btnPanel.add(endBtn);
        btnPanel.add(chartBtn);
        btnPanel.add(logoutBtn);
        container.add(btnPanel, BorderLayout.NORTH);

        // 버튼 이벤트 처리
        attendBtn.addActionListener(e -> markAttendance());
        startBtn.addActionListener(e -> markStart(startBtn, endBtn));
        endBtn.addActionListener(e -> markEnd(startBtn, endBtn));
        goalBtn.addActionListener(e -> setGoalWeight(weightInput.getText()));
        nowBtn.addActionListener(e -> compareWeight(weightInput.getText()));
        chartBtn.addActionListener(e -> new UsageChartFrame(memberId).setVisible(true));
        logoutBtn.addActionListener(e -> {
            if (sessionTimer != null) sessionTimer.stop();
            dispose();
            new Main().setVisible(true);
        });

        initializeUsageState(startBtn, endBtn);
        setVisible(true);
    }

    // 기본 스타일 적용
    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        return l;
    }

    private void initializeUsageState(JButton startBtn, JButton endBtn) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT start_time FROM usage_log WHERE member_id = ? AND end_time IS NULL AND DATE(start_time) = CURDATE() ORDER BY start_time DESC LIMIT 1";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                liveStartTime = rs.getTimestamp("start_time").toLocalDateTime();
                startLiveDurationUpdate();
                startBtn.setEnabled(false);
                endBtn.setEnabled(true);
            } else {
                liveStartTime = null;
                if (sessionTimer != null) sessionTimer.stop();
                durationLabel.setText("⏱ 오늘 이용 시간: -");
                startBtn.setEnabled(true);
                endBtn.setEnabled(false);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "이용 상태 확인 중 오류 발생");
        }
    }

    private void markAttendance() {
        LocalDate today = LocalDate.now();
        if (lastAttendanceDate != null && lastAttendanceDate.equals(today)) {
            JOptionPane.showMessageDialog(this, "오늘은 이미 출석했습니다!");
            return;
        }

        attendanceDays++;
        lastAttendanceDate = today;

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "UPDATE members SET attendance_days = ?, last_attendance_date = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, attendanceDays);
            pstmt.setDate(2, Date.valueOf(today));
            pstmt.setInt(3, memberId);
            pstmt.executeUpdate();

            attendanceLabel.setText("✅ 출석 일수: " + attendanceDays + "일");
            JOptionPane.showMessageDialog(this, "출석 완료!");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB 오류");
        }
    }

    private void markStart(JButton startBtn, JButton endBtn) {
        LocalDate today = LocalDate.now();
        if (lastAttendanceDate == null || !lastAttendanceDate.equals(today)) {
            JOptionPane.showMessageDialog(this, "먼저 출석을 해주세요.");
            return;
        }
        if (liveStartTime != null) {
            JOptionPane.showMessageDialog(this, "이미 이용 중입니다.");
            return;
        }

        liveStartTime = LocalDateTime.now();
        UsageLog.startUsage(memberId, liveStartTime);
        JOptionPane.showMessageDialog(this, "이용 시작 기록됨.");
        startLiveDurationUpdate();
        startBtn.setEnabled(false);
        endBtn.setEnabled(true);
    }

    private void markEnd(JButton startBtn, JButton endBtn) {
        if (liveStartTime == null) {
            JOptionPane.showMessageDialog(this, "이용 시작 상태가 아닙니다.");
            return;
        }

        if (sessionTimer != null) sessionTimer.stop();
        LocalDateTime endTime = LocalDateTime.now();
        UsageLog.endUsage(memberId, endTime);

        long minutes = ChronoUnit.MINUTES.between(liveStartTime, endTime);
        long seconds = ChronoUnit.SECONDS.between(liveStartTime, endTime) % 60;
        durationLabel.setText("최종 이용: " + minutes + "분 " + seconds + "초");
        JOptionPane.showMessageDialog(this, "이용 종료 완료");

        liveStartTime = null;
        startBtn.setEnabled(true);
        endBtn.setEnabled(false);
    }

    private void startLiveDurationUpdate() {
        if (sessionTimer != null) sessionTimer.stop();

        sessionTimer = new Timer(1000, e -> {
            if (liveStartTime != null) {
                long minutes = ChronoUnit.MINUTES.between(liveStartTime, LocalDateTime.now());
                long seconds = ChronoUnit.SECONDS.between(liveStartTime, LocalDateTime.now()) % 60;
                durationLabel.setText("⏱ 오늘 이용 시간: " + minutes + "분 " + seconds + "초");
            }
        });
        sessionTimer.start();
    }

    private void setGoalWeight(String input) {
        try {
            float weight = Float.parseFloat(input);
            goalWeight = weight;

            try (Connection conn = DBUtil.getConnection()) {
                String sql = "UPDATE members SET goal_weight = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setFloat(1, weight);
                pstmt.setInt(2, memberId);
                pstmt.executeUpdate();
            }

            goalWeightLabel.setText("🎯 목표 체중: " + weight + "kg");
            diffLabel.setText(currentWeight != null ? getGoalDiffText(currentWeight) : "");
            JOptionPane.showMessageDialog(this, "목표 체중 설정 완료");
        } catch (NumberFormatException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "올바른 숫자를 입력해주세요.");
        }
    }

    private void compareWeight(String input) {
        try {
            float nowWeight = Float.parseFloat(input);
            currentWeight = nowWeight;
            nowWeightLabel.setText("📏 현재 체중: " + nowWeight + "kg");

            try (Connection conn = DBUtil.getConnection()) {
                String sql = "UPDATE members SET current_weight = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setFloat(1, nowWeight);
                pstmt.setInt(2, memberId);
                pstmt.executeUpdate();
            }

            diffLabel.setText(getGoalDiffText(nowWeight));
        } catch (NumberFormatException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "유효한 체중을 입력하세요.");
        }
    }

    private String getGoalDiffText(Float nowWeight) {
        if (goalWeight == null) return "🎯 목표 체중 미설정";
        float diff = nowWeight - goalWeight;
        if (diff == 0f) return "🎉 목표 체중 달성!";
        return "📌 목표까지 " + String.format("%.1f", Math.abs(diff)) + "kg " + (diff > 0 ? "감량 필요" : "증가 필요");
    }
}
