package Project;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Ulogin extends JFrame {
    public Ulogin() {
        Font font = new Font("Arial", Font.PLAIN, 18);
        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField(15);
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField(15);
        JButton loginBtn = new JButton("Login");
        JButton registerLink = new JButton("Don't have an account? Register");

        userLabel.setFont(font);
        userField.setFont(font);
        passLabel.setFont(font);
        passField.setFont(font);
        loginBtn.setFont(font);
        registerLink.setFont(new Font("Arial", Font.PLAIN, 14));
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false);
        registerLink.setForeground(Color.BLUE);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        setLayout(null);
        userLabel.setBounds(100, 50, 100, 30);
        userField.setBounds(200, 50, 200, 30);
        passLabel.setBounds(100, 100, 100, 30);
        passField.setBounds(200, 100, 200, 30);
        loginBtn.setBounds(150, 160, 120, 40);
        registerLink.setBounds(100, 220, 250, 30);

        add(userLabel); add(userField);
        add(passLabel); add(passField);
        add(loginBtn); add(registerLink);

        loginBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required");
                return;
            }

            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/library_management", "root", "PHW#84#jeor")) {
                String sql = "SELECT * FROM ulogin WHERE username = ? AND password = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, username);
                pst.setString(2, password);
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    // ✅ Update last_login on successful login
                    String updateSql = "UPDATE ulogin SET last_login = NOW() WHERE username = ?";
                    PreparedStatement updatePst = con.prepareStatement(updateSql);
                    updatePst.setString(1, username);
                    updatePst.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Login Successful");
                    new UserHome(username);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials. Try again.");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        registerLink.addActionListener(e -> {
            new Register();
            dispose();
        });

        setTitle("User Login");
        setSize(500, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        new Ulogin();
    }
}
