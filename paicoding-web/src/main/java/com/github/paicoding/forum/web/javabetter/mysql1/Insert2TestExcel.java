package com.github.paicoding.forum.web.javabetter.mysql1;

import java.sql.*;

import java.sql.*;
import java.time.LocalDate;

public class Insert2TestExcel {
    public static void main(String[] args) {
        // 连接 MySQL 数据库
        Connection conn = null;
        PreparedStatement pstmt = null;
        Statement stmt = null;
        try {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/pai_coding?useSSL=false&rewriteBatchedStatements=true",
                    "root",
                    ""
            );

            stmt = conn.createStatement();
//            // 创建表 test_excel,表的字段有 id，day，pu 和 pv
//            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS test_excel (" +
//                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
//                    "day DATE, " +
//                    "pu INT, " +
//                    "pv INT)"
//            );

            // 批量插入数据
            conn.setAutoCommit(false); // 关闭自动提交

            String insertSQL = "INSERT INTO request_count (host, cnt, date) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(insertSQL);

            int batchSize = 5000; // 批量大小
            LocalDate baseDate = Date.valueOf("2020-01-01").toLocalDate();
            for (int i = 0; i < 5000000; i++) {
                pstmt.setString(1, "127.0.0." + (i % 255));
                pstmt.setInt(2, 100 + i % 10000);

                pstmt.setDate(3, Date.valueOf(baseDate.plusDays(i % 31)));
                pstmt.addBatch();

                if (i % batchSize == 0) {
                    pstmt.executeBatch();
                }
            }
            pstmt.executeBatch();
            conn.commit(); // 手动提交

            // 查询数据
//            ResultSet rs = stmt.executeQuery("SELECT id, day, pu, pv FROM test_excel LIMIT 10");
//            while (rs.next()) {
//                System.out.printf("ID: %d, Day: %s, PU: %d, PV: %d%n",
//                        rs.getInt("id"), rs.getString("day"), rs.getInt("pu"), rs.getInt("pv"));
//            }

            // 查多少行
            ResultSet rs2 = stmt.executeQuery("SELECT COUNT(*) AS total FROM request_count");
            if (rs2.next()) {
                System.out.println("Total rows: " + rs2.getInt("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
