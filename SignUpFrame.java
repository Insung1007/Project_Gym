package project.gym;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;

public class SignUpFrame extends JFrame {
	private JTextField nameField, phoneField;
	private JCheckBox cabinetBox;
	private JLabel priceLabel;
	private JRadioButton threeMonth, sixMonth, twelveMonth;
	private ButtonGroup membershipGroup;
	private JTextField emailField;

	public SignUpFrame() {
		setTitle("회원가입");
		setSize(507, 462);

		nameField = new JTextField();
		nameField.setBounds(237, 23, 209, 47);
		phoneField = new JTextField();
		phoneField.setBounds(237, 70, 209, 47);
		cabinetBox = new JCheckBox("캐비닛 사용 (1만원/월)");
		cabinetBox.setBounds(237, 273, 209, 47);
		priceLabel = new JLabel("총 요금: 0원");
		priceLabel.setBounds(63, 316, 164, 47);

		threeMonth = new JRadioButton("3개월 (13만원)");
		threeMonth.setBounds(28, 226, 209, 47);
		sixMonth = new JRadioButton("6개월 (24만원)");
		sixMonth.setBounds(237, 226, 209, 47);
		twelveMonth = new JRadioButton("12개월 (40만원)");
		twelveMonth.setBounds(28, 269, 209, 47);
		membershipGroup = new ButtonGroup();
		membershipGroup.add(threeMonth);
		membershipGroup.add(sixMonth);
		membershipGroup.add(twelveMonth);

		JButton submitBtn = new JButton("가입 완료");
		submitBtn.setBounds(237, 316, 209, 47);

		cabinetBox.addActionListener(e -> updatePrice());
		threeMonth.addActionListener(e -> updatePrice());
		sixMonth.addActionListener(e -> updatePrice());
		twelveMonth.addActionListener(e -> updatePrice());

		submitBtn.addActionListener(e -> registerMember());
		getContentPane().setLayout(null);

		JLabel label = new JLabel("이름");
		label.setBounds(28, 23, 209, 47);
		getContentPane().add(label);
		getContentPane().add(nameField);
		JLabel label_1 = new JLabel("전화번호");
		label_1.setBounds(28, 70, 209, 47);
		getContentPane().add(label_1);
		getContentPane().add(phoneField);

		getContentPane().add(threeMonth);
		getContentPane().add(sixMonth);
		getContentPane().add(twelveMonth);
		getContentPane().add(cabinetBox);
		getContentPane().add(priceLabel);
		getContentPane().add(submitBtn);

		JLabel emailLabel = new JLabel("이메일");
		emailLabel.setBounds(28, 118, 209, 47);
		getContentPane().add(emailLabel);

		emailField = new JTextField();
		emailField.setBounds(237, 118, 209, 47);
		getContentPane().add(emailField);
		emailField.setColumns(10);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void updatePrice() {
		int price = 0;
		if (threeMonth.isSelected())
			price = 130000;
		else if (sixMonth.isSelected())
			price = 240000;
		else if (twelveMonth.isSelected())
			price = 400000;

		if (cabinetBox.isSelected()) {
			if (threeMonth.isSelected())
				price += 10000 * 3;
			else if (sixMonth.isSelected())
				price += 10000 * 6;
			else if (twelveMonth.isSelected())
				price += 10000 * 12;
		}

		priceLabel.setText("총 요금: " + price + "원");
	}

	private void registerMember() {
		String email = emailField.getText();
		String name = nameField.getText();
		String phone = phoneField.getText();
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
			new LoginFrame(); // 로그인 창으로 이동
		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "DB 저장 오류");
		}
	}
}
