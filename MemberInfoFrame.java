package project.gym;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MemberInfoFrame extends JFrame {
    private JLabel nameLabel, daysLeftLabel, goalWeightLabel, attendanceLabel, diffLabel, emailLabel;
    private JLabel durationLabel, nowWeightLabel;
    private int memberId;
    private int attendanceDays;
    private int membershipMonths;
    private LocalDate startDate;
    private LocalDate lastAttendanceDate;
    private Float goalWeight;
    private Float currentWeight;
    private LocalDateTime liveStartTime;
    private Timer sessionTimer;

    // 버튼들을 클래스 필드로 선언
    private JButton startBtn, endBtn, attendBtn, goalBtn, nowBtn, chartBtn, EndBtn;

    public MemberInfoFrame(ResultSet rs) throws SQLException {
        setTitle("회원 정보");
        setSize(532, 458);
        getContentPane().setLayout(null);

        // DB 정보 추출
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


        nameLabel = new JLabel("회원 이름: " + name);
        nameLabel.setFont(new Font("굴림", Font.BOLD, 15));
        nameLabel.setBounds(10, 13, 162, 18);
        getContentPane().add(nameLabel);

        emailLabel = new JLabel("이메일 : " + email);
        emailLabel.setFont(new Font("굴림", Font.BOLD, 15));
        emailLabel.setBounds(10, 37, 293, 18);
        getContentPane().add(emailLabel);

        daysLeftLabel = new JLabel("회원권 만료까지 : " + calculateDaysLeft() + "일");
        daysLeftLabel.setFont(new Font("굴림", Font.BOLD, 15));
        daysLeftLabel.setBounds(9, 54, 200, 24);
        getContentPane().add(daysLeftLabel);

        attendanceLabel = new JLabel("출석 일수: " + attendanceDays + "일");
        attendanceLabel.setBounds(364, 211, 120, 30);
        getContentPane().add(attendanceLabel);

        goalWeightLabel = new JLabel("목표 체중: " + (goalWeight != null ? goalWeight + "kg" : "미설정"));
        goalWeightLabel.setFont(new Font("굴림", Font.BOLD, 12));
        goalWeightLabel.setBounds(9, 76, 200, 25);
        getContentPane().add(goalWeightLabel);

        nowWeightLabel = new JLabel("현재 체중: " + (currentWeight != null ? currentWeight + "kg" : "미설정"));
        nowWeightLabel.setFont(new Font("굴림", Font.BOLD, 12));
        nowWeightLabel.setBounds(9, 107, 180, 18);
        getContentPane().add(nowWeightLabel);
        
        diffLabel = new JLabel(goalWeight != null && currentWeight != null ? getGoalDiffText(currentWeight) : " ");
        diffLabel.setBounds(9, 135, 250, 25);
        getContentPane().add(diffLabel);


        durationLabel = new JLabel("오늘 이용 시간: -");
        durationLabel.setBounds(364, 237, 154, 25);
        getContentPane().add(durationLabel);

        // 버튼 초기화
        attendBtn = new JButton("출석");
        attendBtn.setBounds(359, 15, 128, 63);
        getContentPane().add(attendBtn);

        endBtn = new JButton("이용 종료");
        endBtn.setBounds(359, 132, 128, 30);
        getContentPane().add(endBtn);

        JTextField weightField = new JTextField();
        weightField.setBounds(72, 347, 137, 23);
        getContentPane().add(weightField);

        goalBtn = new JButton("목표 체중");
        goalBtn.setBounds(10, 380, 92, 23);
        getContentPane().add(goalBtn);

        nowBtn = new JButton("현재 체중");
        nowBtn.setBounds(112, 380, 92, 23);
        getContentPane().add(nowBtn);

        JLabel weightLabel = new JLabel("체중 입력:");
        weightLabel.setBounds(10, 347, 64, 23);
        getContentPane().add(weightLabel);

        startBtn = new JButton("이용 시작");
        startBtn.setBounds(359, 88, 128, 30);
        getContentPane().add(startBtn);
        
        chartBtn = new JButton("이용시간 그래프");
        chartBtn.setBounds(359, 171, 128, 30);
        getContentPane().add(chartBtn);

        EndBtn = new JButton("로그아웃");
        EndBtn.setBounds(351, 360, 136, 40);
        getContentPane().add(EndBtn);
        
        // 액션 리스너 연결
        attendBtn.addActionListener(e -> markAttendance());
        startBtn.addActionListener(e -> markStart());
        endBtn.addActionListener(e -> markEnd());
        goalBtn.addActionListener(e -> setGoalWeight(weightField.getText()));
        nowBtn.addActionListener(e -> compareWeight(weightField.getText()));
        chartBtn.addActionListener(e -> {
            new UsageChartFrame(memberId).setVisible(true);
        });
        EndBtn.addActionListener(e -> {
            if (sessionTimer != null) sessionTimer.stop();
            dispose();
            new Main().setVisible(true);
        });

        // 창이 로드될 때 이용 상태(버튼, 타이머) 초기화
        initializeUsageState();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    
    //로그인 시 회원의 현재 이용 상태를 확인하고 버튼과 타이머를 설정
    private void initializeUsageState() {
        try (Connection conn = DBUtil.getConnection()) {
            // 오늘 날짜에 시작하고 아직 종료되지 않은 가장 최근의 이용 기록을 찾습니다.
            String sql = "SELECT start_time FROM usage_log WHERE member_id = ? AND end_time IS NULL AND DATE(start_time) = CURDATE() ORDER BY start_time DESC LIMIT 1";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // 이용 중인 기록이 있으면 타이머를 이어서 시작
                liveStartTime = rs.getTimestamp("start_time").toLocalDateTime();
                startLiveDurationUpdate();
                startBtn.setEnabled(false);
                endBtn.setEnabled(true);
            } else {
                // 이용 중인 기록이 없으면 초기 상태로 설정
                liveStartTime = null;
                if (sessionTimer != null) {
                    sessionTimer.stop();
                }
                durationLabel.setText("오늘 이용 시간: -");
                startBtn.setEnabled(true);
                endBtn.setEnabled(false);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "이용 상태 확인 중 DB 오류 발생");
            // 오류 발생 시 기본 상태로 설정
            startBtn.setEnabled(true);
            endBtn.setEnabled(false);
        }
    }


    private long calculateDaysLeft() {
        LocalDate endDate = startDate.plusMonths(membershipMonths);
        return ChronoUnit.DAYS.between(LocalDate.now(), endDate);
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

            attendanceLabel.setText("출석 일수: " + attendanceDays + "일");
            JOptionPane.showMessageDialog(this, "출석 완료!");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB 오류 발생");
        }
    }

    
    //이용 시작 버튼 클릭 시 호출
    //usage_log 테이블에 시작 시간을 기록하고 타이머를 작동
    private void markStart() {
        LocalDate today = LocalDate.now();

        // 오늘 출석했는지 확인
        if (lastAttendanceDate == null || !lastAttendanceDate.equals(today)) {
            JOptionPane.showMessageDialog(this, "먼저 출석 버튼을 눌러주세요.");
            return;
        }
        
        // 이미 시작한 경우 중복 실행 방지
        if (liveStartTime != null) {
            JOptionPane.showMessageDialog(this, "이미 이용 중입니다.");
            return;
        }

        // DB에 이용 시작 기록
        liveStartTime = LocalDateTime.now();
        UsageLog.startUsage(memberId, liveStartTime);

        JOptionPane.showMessageDialog(this, "이용 시작 시간이 기록되었습니다.");
        durationLabel.setText("오늘 이용 시간: 0분 0초");
        startLiveDurationUpdate();

        // 버튼 상태 변경
        startBtn.setEnabled(false);
        endBtn.setEnabled(true);
    }

    
    //이용 종료 버튼 클릭 시 호출
    //usage_log 테이블에 종료 시간을 기록하고 타이머를 정지
    private void markEnd() {
        if (liveStartTime == null) {
            JOptionPane.showMessageDialog(this, "이용 시작 상태가 아닙니다.");
            return;
        }

        // 타이머 정지
        if (sessionTimer != null) {
            sessionTimer.stop();
            sessionTimer = null;
        }

        // DB에 이용 종료 기록
        LocalDateTime endTime = LocalDateTime.now();
        UsageLog.endUsage(memberId, endTime);

        long minutes = ChronoUnit.MINUTES.between(liveStartTime, endTime);
        long seconds = ChronoUnit.SECONDS.between(liveStartTime, endTime) % 60;
        
        JOptionPane.showMessageDialog(this, "이용이 종료되었습니다.");
        
        // UI 초기화
        durationLabel.setText("최종 이용: " + minutes + "분 " + seconds + "초");
        liveStartTime = null; // 시작 시간 초기화
        
        // 버튼 상태 변경: 이용 종료 후 다시 시작할 수 있도록 설정
        endBtn.setEnabled(false);
        startBtn.setEnabled(true); 
    }


    private void startLiveDurationUpdate() {
        if (sessionTimer != null) {
            sessionTimer.stop();
        }

        sessionTimer = new Timer(1000, e -> {
            if (liveStartTime != null) {
                long minutes = ChronoUnit.MINUTES.between(liveStartTime, LocalDateTime.now());
                long seconds = ChronoUnit.SECONDS.between(liveStartTime, LocalDateTime.now()) % 60;
                durationLabel.setText("오늘 이용 시간: " + minutes + "분 " + seconds + "초");
            }
        });
        sessionTimer.start();
    }

    private void setGoalWeight(String weightStr) {
        try {
            float weight = Float.parseFloat(weightStr);
            goalWeight = weight;

            try (Connection conn = DBUtil.getConnection()) {
                String sql = "UPDATE members SET goal_weight = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setFloat(1, weight);
                pstmt.setInt(2, memberId);
                pstmt.executeUpdate();

                goalWeightLabel.setText("목표 체중: " + weight + "kg");
                diffLabel.setText(currentWeight != null ? getGoalDiffText(currentWeight) : "");
                JOptionPane.showMessageDialog(this, "목표 체중 설정 완료");
            }
        } catch (NumberFormatException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "유효한 숫자를 입력하세요");
        }
    }

    private void compareWeight(String weightStr) {
        try {
            float nowWeight = Float.parseFloat(weightStr);
            currentWeight = nowWeight;
            nowWeightLabel.setText("현재 체중: " + nowWeight + "kg");

            try (Connection conn = DBUtil.getConnection()) {
                String sql = "UPDATE members SET current_weight = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setFloat(1, nowWeight);
                pstmt.setInt(2, memberId);
                pstmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "DB에 현재 체중 저장 실패");
            }

            if (goalWeight == null) {
                JOptionPane.showMessageDialog(this, "목표 체중이 설정되지 않았습니다.");
                diffLabel.setText("");
                return;
            }

            diffLabel.setText(getGoalDiffText(nowWeight));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "체중 입력 오류");
        }
    }

    private String getGoalDiffText(Float nowWeight) {
        if (goalWeight == null) return "목표 체중 미설정";
        if (nowWeight == null) return "";

        float diff = nowWeight - goalWeight;
        if (diff == 0f) return "🎉 목표 체중 달성!";
        return "목표까지 " + String.format("%.1f", Math.abs(diff)) + "kg " + (diff > 0 ? "감량 필요" : "증가 필요");
    }
}
