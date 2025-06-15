package project.gym;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField nameField, phoneField;

    public LoginFrame() {
        setTitle("헬스메이트 - 로그인");
        setSize(400, 250);
        setLocationRelativeTo(null); // 창 중앙 정렬
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 레이아웃 설정
        JPanel contentPane = new JPanel(new GridBagLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        contentPane.setBackground(Color.WHITE);
        setContentPane(contentPane);

        Font labelFont = new Font("맑은 고딕", Font.BOLD, 14);
        Font fieldFont = new Font("맑은 고딕", Font.PLAIN, 14);

        // 이름 라벨
        JLabel nameLabel = new JLabel("이름");
        nameLabel.setFont(labelFont);
        GridBagConstraints gbcLabel1 = new GridBagConstraints();
        gbcLabel1.gridx = 0;
        gbcLabel1.gridy = 0;
        gbcLabel1.insets = new Insets(10, 10, 10, 10);
        gbcLabel1.anchor = GridBagConstraints.WEST;
        contentPane.add(nameLabel, gbcLabel1);

        // 이름 필드
        nameField = new JTextField();
        nameField.setFont(fieldFont);
        GridBagConstraints gbcField1 = new GridBagConstraints();
        gbcField1.gridx = 1;
        gbcField1.gridy = 0;
        gbcField1.weightx = 1.0;
        gbcField1.fill = GridBagConstraints.HORIZONTAL;
        gbcField1.insets = new Insets(10, 10, 10, 10);
        contentPane.add(nameField, gbcField1);

        // 전화번호 라벨
        JLabel phoneLabel = new JLabel("전화번호 뒤 4자리");
        phoneLabel.setFont(labelFont);
        GridBagConstraints gbcLabel2 = new GridBagConstraints();
        gbcLabel2.gridx = 0;
        gbcLabel2.gridy = 1;
        gbcLabel2.insets = new Insets(10, 10, 10, 10);
        gbcLabel2.anchor = GridBagConstraints.WEST;
        contentPane.add(phoneLabel, gbcLabel2);

        // 전화번호 필드
        phoneField = new JTextField();
        phoneField.setFont(fieldFont);
        GridBagConstraints gbcField2 = new GridBagConstraints();
        gbcField2.gridx = 1;
        gbcField2.gridy = 1;
        gbcField2.weightx = 1.0;
        gbcField2.fill = GridBagConstraints.HORIZONTAL;
        gbcField2.insets = new Insets(10, 10, 10, 10);
        contentPane.add(phoneField, gbcField2);

        // 로그인 버튼
        JButton loginButton = new JButton("로그인");
        loginButton.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        loginButton.setBackground(new Color(70, 130, 180)); // SteelBlue
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(150, 40));
        GridBagConstraints gbcButton = new GridBagConstraints();
        gbcButton.gridx = 0;
        gbcButton.gridy = 2;
        gbcButton.gridwidth = 2;
        gbcButton.insets = new Insets(20, 10, 10, 10);
        gbcButton.anchor = GridBagConstraints.CENTER;
        contentPane.add(loginButton, gbcButton);

        // 이벤트 등록
        ActionListener loginAction = e -> login();
        loginButton.addActionListener(loginAction);
        nameField.addActionListener(loginAction);
        phoneField.addActionListener(loginAction);

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
