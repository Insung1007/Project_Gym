package project.gym;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.*;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.PlotOrientation;


//최근 7일간 회원의 헬스장 이용 시간을 꺾은선 그래프로 보여주는 프레임
public class UsageChartFrame extends JFrame {

    private int memberId; // 그래프를 보여줄 회원의 id

    public UsageChartFrame(int memberId) {
        this.memberId = memberId;
        setTitle("일주일 이용 시간 그래프");
        setSize(700, 500);
        setLocationRelativeTo(null); // 화면 중앙에 창 띄우기

        // DB에서 최근 7일 이용 시간 데이터를 불러옴
        DefaultCategoryDataset dataset = loadUsageData();

        // JFreeChart 라이브러리를 이용해 꺾은선 그래프 생성
        JFreeChart chart = ChartFactory.createLineChart(
                "최근 7일간 이용 시간", // 차트 제목
                "날짜",              // X축 라벨
                "이용 시간 (분)",     // Y축 라벨
                dataset,             // 데이터셋
                PlotOrientation.VERTICAL,
                true, true, false
        );

        // 차트 폰트 및 색상 등 꾸미기
        Font titleFont = new Font("맑은 고딕", Font.BOLD, 18);
        Font labelFont = new Font("맑은 고딕", Font.PLAIN, 13);
        chart.getTitle().setFont(titleFont);
        chart.setBackgroundPaint(Color.white);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(245, 245, 245));
        plot.setRangeGridlinePaint(Color.GRAY);

        // Y축(이용 시간) 범위 및 눈금 설정
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(0.0, 120.0); // 0~120분
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setLabelFont(labelFont);
        rangeAxis.setTickLabelFont(labelFont);

        // X축(날짜) 라벨 회전 등 설정
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0));
        domainAxis.setTickLabelFont(labelFont);
        domainAxis.setLabelFont(labelFont);

        // 꺾은선 그래프 선/점 모양 설정
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(79, 129, 189)); // 선 색상
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));  // 선 두께
        renderer.setSeriesShapesVisible(0, true);            // 점 표시
        renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-3, -3, 6, 6)); // 점 모양

        ChartPanel chartPanel = new ChartPanel(chart);
        chart.setPadding(new RectangleInsets(10, 10, 10, 10));
        setContentPane(chartPanel);
    }

    
    //DB에서 최근 7일간의 이용 시간 데이터를 불러와서 그래프용 데이터셋으로 만듦
    private DefaultCategoryDataset loadUsageData() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        LocalDate today = LocalDate.now();
        Map<String, Integer> dateToMinutes = new HashMap<>(); // 날짜별 이용 시간(분) 저장

        try (Connection conn = DBUtil.getConnection()) {
            // usage_log 테이블에서 최근 7일간의 출입 기록을 불러옴
            String sql = "SELECT start_time, end_time FROM usage_log " +
                         "WHERE member_id = ? AND start_time >= ? AND end_time IS NOT NULL";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId); // 회원 id
            pstmt.setTimestamp(2, java.sql.Timestamp.valueOf(today.minusDays(6).atStartOfDay()));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Timestamp startTs = rs.getTimestamp("start_time");
                Timestamp endTs = rs.getTimestamp("end_time");
                if (startTs != null && endTs != null) {
                    // 출입 시작 시간이 속한 날짜를 기준으로 이용 시간(분) 계산
                    LocalDate date = startTs.toLocalDateTime().toLocalDate();
                    long minutes = ChronoUnit.MINUTES.between(
                            startTs.toLocalDateTime(), endTs.toLocalDateTime());
                    String label = date.toString();
                    // 같은 날짜에 여러 번 출입했으면 누적 합산
                    dateToMinutes.put(label,
                            dateToMinutes.getOrDefault(label, 0) + (int) minutes);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB에서 이용 시간 불러오기 실패");
        }

        // 최근 7일간 날짜별로 데이터셋에 값 추가 (이용 기록이 없는 날은 0분)
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String label = date.toString();
            dataset.addValue(dateToMinutes.getOrDefault(label, 0), "이용시간", label);
        }

        return dataset;
    }
}
