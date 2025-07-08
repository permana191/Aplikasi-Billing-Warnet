    package com.cybercafe.billing;

    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.PreparedStatement;
    import java.sql.SQLException;

import javax.swing.JOptionPane;

    public class DatabaseManager {

        private static final String DB_URL = "jdbc:mysql://localhost:3306/cybercafe_db";
        private static final String DB_USER = "root"; // Default user untuk XAMPP
        private static final String DB_PASSWORD = ""; // Default password untuk XAMPP (kosong)

        // Metode untuk mendapatkan koneksi ke database
        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }

        // Metode untuk menyimpan receipt ke database
        public static void saveReceipt(String customerName, String sessionType, String duration,
                                       double sessionBill, double productsBill, double totalBill,
                                       String receiptText) {
            String sql = "INSERT INTO receipts (customer_name, session_type, duration, session_bill, products_bill, total_bill, receipt_text) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // Mengatur nilai untuk setiap placeholder
                pstmt.setString(1, customerName);
                pstmt.setString(2, sessionType);
                pstmt.setString(3, duration);
                pstmt.setDouble(4, sessionBill);
                pstmt.setDouble(5, productsBill);
                pstmt.setDouble(6, totalBill);
                pstmt.setString(7, receiptText);

                // Menjalankan pernyataan SQL
                pstmt.executeUpdate();
                System.out.println("Receipt saved to database successfully!");

            } catch (SQLException e) {
                System.err.println("Error saving receipt to database: " + e.getMessage());
                System.err.println("SQL State: " + e.getSQLState());
                System.err.println("Vendor Error Code: " + e.getErrorCode());
                e.printStackTrace(); // Ini akan mencetak stack trace lengkap
                JOptionPane.showMessageDialog(null, "Error saving receipt to database: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    