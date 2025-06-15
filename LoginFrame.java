package project.gym;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField nameField, phoneField;

    public LoginFrame() {
        setTitle("로그인");
        setSize(300, 200);

        nameField = new JTextField();
        nameField.setBounds(133, 10, 143, 40);
        phoneField = new JTextField();
        phoneField.setBounds(133, 50, 143, 40);
        JButton loginButton = new JButton("로그인");
        loginButton.setBounds(65, 113, 143, 40);

        ActionListener loginAction = e -> login();
        loginButton.addActionListener(loginAction);

        phoneField.addActionListener(loginAction); // 엔터로 로그인
        nameField.addActionListener(loginAction);
        getContentPane().setLayout(null);

        JLabel label = new JLabel("이름");
        label.setBounds(23, 10, 143, 40);
        getContentPane().add(label); 
        getContentPane().add(nameField);
        JLabel label_1 = new JLabel("전화번호 뒤 4자리");
        label_1.setBounds(23, 50, 143, 40);
        getContentPane().add(label_1);
        getContentPane().add(phoneField);
        getContentPane().add(loginButton);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void login() {
        String name = nameField.getText().trim();
        String lastFourDigits = phoneField.getText().trim();

        if (lastFourDigits.length() != 4 || !lastFourDigits.matches("\\d{4}")) {
            JOptionPane.showMessageDialog(this, "전화번호 뒤 4자리를 정확히 입력해주세요.");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM members WHERE name = ? AND RIGHT(phone, 4) = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, lastFourDigits);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                dispose();
                new MemberInfoFrame(rs);
            } else {
                JOptionPane.showMessageDialog(this, "존재하지 않는 회원입니다.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB 오류 발생");
        }
    }
}
