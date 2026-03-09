package com.gsms.gsms.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * JDBC直接访问数据库示例（仅用于演示，不推荐在生产代码中使用）
 *
 * ⚠️ 注意：实际项目中应该使用 MyBatis Mapper 或 Spring Data JPA，
 * 而不是直接使用 JDBC
 */
public class JdbcExample {

    public static void main(String[] args) {
        // 数据库连接信息（来自 application-dev.yml）
        String url = "jdbc:mysql://49.235.153.206:31266/gsms?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8";
        String username = "root";
        String password = "mysql_b8PdXs";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // 1. 加载驱动（MySQL 8.0+ 使用 com.mysql.cj.jdbc.Driver）
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 2. 建立连接
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("✅ 数据库连接成功！");

            // 3. 执行查询
            String sql = "SELECT id, username, nickname, email, phone FROM sys_user WHERE is_deleted = 0";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            // 4. 处理结果集
            System.out.println("\n📋 用户列表：");
            System.out.println("--------------------------------------------------");
            while (rs.next()) {
                Long id = rs.getLong("id");
                String username2 = rs.getString("username");
                String nickname = rs.getString("nickname");
                String email = rs.getString("email");
                String phone = rs.getString("phone");

                System.out.printf("ID: %d, 用户名: %s, 昵称: %s, 邮箱: %s, 电话: %s%n",
                        id, username2, nickname, email, phone);
            }

        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL驱动未找到，请确认依赖已添加");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ 数据库操作失败：" + e.getMessage());
            e.printStackTrace();
        } finally {
            // 5. 关闭资源（倒序关闭）
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 使用 try-with-resources 自动关闭资源（Java 7+ 推荐）
     */
    public static void queryWithTryWithResources() {
        String url = "jdbc:mysql://49.235.153.206:31266/gsms?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8";
        String username = "root";
        String password = "mysql_b8PdXs";

        String sql = "SELECT COUNT(*) as total FROM sys_user WHERE is_deleted = 0";

        // try-with-resources 会自动关闭 Connection、PreparedStatement、ResultSet
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                int total = rs.getInt("total");
                System.out.println("📊 用户总数：" + total);
            }

        } catch (Exception e) {
            System.err.println("❌ 查询失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 带参数的查询示例（防止SQL注入）
     */
    public static void queryWithParameter(String targetUsername) {
        String url = "jdbc:mysql://49.235.153.206:31266/gsms?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8";
        String username = "root";
        String password = "mysql_b8PdXs";

        String sql = "SELECT * FROM sys_user WHERE username = ? AND is_deleted = 0";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 设置参数（使用占位符 ?，防止SQL注入）
            pstmt.setString(1, targetUsername);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("✅ 找到用户：");
                    System.out.println("  ID: " + rs.getLong("id"));
                    System.out.println("  用户名: " + rs.getString("username"));
                    System.out.println("  昵称: " + rs.getString("nickname"));
                    System.out.println("  邮箱: " + rs.getString("email"));
                    System.out.println("  电话: " + rs.getString("phone"));
                } else {
                    System.out.println("⚠️ 未找到用户：" + targetUsername);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ 查询失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 更新操作示例
     */
    public static void updateExample(Long userId, String newNickname) {
        String url = "jdbc:mysql://49.235.153.206:31266/gsms?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8";
        String username = "root";
        String password = "mysql_b8PdXs";

        String sql = "UPDATE sys_user SET nickname = ?, update_time = NOW() WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 设置参数
            pstmt.setString(1, newNickname);
            pstmt.setLong(2, userId);

            // 执行更新
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ 更新成功，影响行数：" + affectedRows);
            } else {
                System.out.println("⚠️ 未找到要更新的记录");
            }

        } catch (Exception e) {
            System.err.println("❌ 更新失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
