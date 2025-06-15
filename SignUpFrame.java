package project.gym;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;

public class SignUpFrame extends JFrame {
    private JTextField nameField, phoneField, emailField;
    private JCheckBox cabinetBox;
    private JLabel priceLabel;
    private JRadioButton threeMonth, sixMonth, twelveMonth;
    private ButtonGroup membershipGroup;

    public SignUpFrame() {
        setTitle("헬스메이트 - 회원가입");
        setSize(500, 500);
        setLocationRelativeTo(null); // 화면 중앙 정렬
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel contentPane = new JPanel(new GridBagLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        contentPane.setBackground(Color.WHITE);
        setContentPane(contentPane);

        Font labelFont = new Font("맑은 고딕", Font.BOLD, 13);
        Font fieldFont = new Font("맑은 고딕", Font.PLAIN, 13);

        // 이름
        JLabel nameLabel = new JLabel("이름");
        nameLabel.setFont(labelFont);
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0; gbc1.gridy = 0;
        gbc1.insets = new Insets(10, 10, 10, 10);
        gbc1.anchor = GridBagConstraints.WEST;
        contentPane.add(nameLabel, gbc1);

        nameField = new JTextField();
        nameField.setFont(fieldFont);
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 1; gbc2.gridy = 0;
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.weightx = 1.0;
        gbc2.insets = new Insets(10, 10, 10, 10);
        contentPane.add(nameField, gbc2);

        // 전화번호
        JLabel phoneLabel = new JLabel("전화번호");
        phoneLabel.setFont(labelFont);
        GridBagConstraints gbc3 = new GridBagConstraints();
        gbc3.gridx = 0; gbc3.gridy = 1;
        gbc3.insets = new Insets(10, 10, 10, 10);
        gbc3.anchor = GridBagConstraints.WEST;
        contentPane.add(phoneLabel, gbc3);

        phoneField = new JTextField();
        phoneField.setFont(fieldFont);
        GridBagConstraints gbc4 = new GridBagConstraints();
        gbc4.gridx = 1; gbc4.gridy = 1;
        gbc4.fill = GridBagConstraints.HORIZONTAL;
        gbc4.weightx = 1.0;
        gbc4.insets = new Insets(10, 10, 10, 10);
        contentPane.add(phoneField, gbc4);

        // 이메일
        JLabel emailLabel = new JLabel("이메일");
        emailLabel.setFont(labelFont);
        GridBagConstraints gbc5 = new GridBagConstraints();
        gbc5.gridx = 0; gbc5.gridy = 2;
        gbc5.insets = new Insets(10, 10, 10, 10);
        gbc5.anchor = GridBagConstraints.WEST;
        contentPane.add(emailLabel, gbc5);

        emailField = new JTextField();
        emailField.setFont(fieldFont);
        GridBagConstraints gbc6 = new GridBagConstraints();
        gbc6.gridx = 1; gbc6.gridy = 2;
        gbc6.fill = GridBagConstraints.HORIZONTAL;
        gbc6.weightx = 1.0;
        gbc6.insets = new Insets(10, 10, 10, 10);
        contentPane.add(emailField, gbc6);

        // 멤버십 기간 라디오버튼
        threeMonth = new JRadioButton("3개월 (13만원)");
        sixMonth = new JRadioButton("6개월 (24만원)");
        twelveMonth = new JRadioButton("12개월 (40만원)");
        membershipGroup = new ButtonGroup();
        membershipGroup.add(threeMonth);
        membershipGroup.add(sixMonth);
        membershipGroup.add(twelveMonth);

        JPanel radioPanel = new JPanel(new GridLayout(1, 3));
        radioPanel.setBackground(Color.WHITE);
        radioPanel.add(threeMonth);
        radioPanel.add(sixMonth);
        radioPanel.add(twelveMonth);

        GridBagConstraints gbc7 = new GridBagConstraints();
        gbc7.gridx = 0; gbc7.gridy = 3;
        gbc7.gridwidth = 2;
        gbc7.insets = new Insets(10, 10, 10, 10);
        gbc7.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(radioPanel, gbc7);

        // 캐비닛 체크박스
        cabinetBox = new JCheckBox("캐비닛 사용 (1만원/월)");
        cabinetBox.setBackground(Color.WHITE);
        GridBagConstraints gbc8 = new GridBagConstraints();
        gbc8.gridx = 0; gbc8.gridy = 4;
        gbc8.gridwidth = 2;
        gbc8.insets = new Insets(10, 10, 10, 10);
        gbc8.anchor = GridBagConstraints.WEST;
        contentPane.add(cabinetBox, gbc8);

        // 총 요금 표시
        priceLabel = new JLabel("총 요금: 0원");
        GridBagConstraints gbc9 = new GridBagConstraints();
        gbc9.gridx = 0; gbc9.gridy = 5;
        gbc9.gridwidth = 2;
        gbc9.insets = new Insets(10, 10, 10, 10);
        contentPane.add(priceLabel, gbc9);

        // 가입 완료 버튼
        JButton submitBtn = new JButton("가입 완료");
        submitBtn.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        submitBtn.setBackground(new Color(46, 139, 87)); // SeaGreen
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);

        GridBagConstraints gbc10 = new GridBagConstraints();
        gbc10.gridx = 0; gbc10.gridy = 6;
        gbc10.gridwidth = 2;
        gbc10.insets = new Insets(20, 10, 10, 10);
        gbc10.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(submitBtn, gbc10);

        // 이벤트 연결
        cabinetBox.addActionListener(e -> updatePrice());
        threeMonth.addActionListener(e -> updatePrice());
        sixMonth.addActionListener(e -> updatePrice());
        twelveMonth.addActionListener(e -> updatePrice());

        submitBtn.addActionListener(e -> registerMember());

        setVisible(true);
    }

    private void updatePrice() {
        int price = 0;
        if (threeMonth.isSelected()) price = 130000;
        else if (sixMonth.isSelected()) price = 240000;
        else if (twelveMonth.isSelected()) price = 400000;

        if (cabinetBox.isSelected()) {
            if (threeMonth.isSelected()) price += 10000 * 3;
            else if (sixMonth.isSelected()) price += 10000 * 6;
            else if (twelveMonth.isSelected()) price += 10000 * 12;
        }

        priceLabel.setText("총 요금: " + price + "원");
    }

    private void registerMember() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        boolean cabinet = cabinetBox.isSelected();
        int months = threeMonth.isSelected() ? 3 : sixMonth.isSelected() ? 6 : 12;
        LocalDate now = LocalDate.now();

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO members (name, phone, membership_months, cabinet, start_date, email) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setInt(3, months);
            pstmt.setBoolean(4, cabinet);
            pstmt.setDate(5, Date.valueOf(now));
            pstmt.setString(6, email);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "회원가입 완료!");
            dispose();
            new LoginFrame();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB 저장 오류");
        }
    }
}
