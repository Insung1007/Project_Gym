package project.gym;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                DBUtil.init();
                Main frame = new Main();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Main() {
        setTitle("헬스장 회원 관리 메인");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 500);
        setLocationRelativeTo(null); // 화면 중앙 정렬

        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        setContentPane(contentPane);
        contentPane.setLayout(null);

        // 제목
        JLabel titleLabel = new JLabel("헬스메이트", SwingConstants.CENTER);
        titleLabel.setBounds(0, 0, 406, 39);
        titleLabel.setFont(new Font("나눔고딕", Font.BOLD, 28));
        titleLabel.setForeground(new Color(30, 144, 255));
        contentPane.add(titleLabel);

        // 중앙 이미지 삽입
        ImageIcon icon = new ImageIcon("gym.png"); // ← 이미지 파일명 지정
        Image scaledImage = icon.getImage().getScaledInstance(130, 130, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon("C:\\Users\\신선호\\Downloads\\images.png"));
        imageLabel.setBounds(0, 36, 406, 353);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(imageLabel);

        // 하단 버튼 패널
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBounds(0, 432, 406, 31);
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setLayout(new GridLayout(1, 2, 20, 0));

        JButton btnLogin = new JButton("로그인");
        btnLogin.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        btnLogin.setBackground(new Color(70, 130, 180));
        btnLogin.setForeground(Color.WHITE);
        buttonPanel.add(btnLogin);

        JButton btnSignUp = new JButton("회원가입");
        btnSignUp.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        btnSignUp.setBackground(new Color(100, 149, 237));
        btnSignUp.setForeground(Color.WHITE);
        buttonPanel.add(btnSignUp);

        contentPane.add(buttonPanel);

        // 이벤트 리스너
        btnSignUp.addActionListener(e -> {
            dispose();
            new SignUpFrame();
        });

        btnLogin.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });
    }
}
