package Project;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Register extends JFrame {
    public Register() {
        Font font = new Font("Arial", Font.PLAIN, 18);

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField(15);
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField(15);
        JLabel confirmPassLabel = new JLabel("Confirm Password:");
        JPasswordField confirmPassField = new JPasswordField(15);
        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField(20);

        JButton registerBtn = new JButton("Register");

        // Set font
        userLabel.setFont(font);
        userField.setFont(font);
        passLabel.setFont(font);
        passField.setFont(font);
        confirmPassLabel.setFont(font);
        confirmPassField.setFont(font);
        emailLabel.setFont(font);
        emailField.setFont(font);
        registerBtn.setFont(font);

        // Set layout and bounds
        setLayout(null);
        userLabel.setBounds(100, 40, 150, 30);
        userField.setBounds(250, 40, 200, 30);
        passLabel.setBounds(100, 90, 150, 30);
        passField.setBounds(250, 90, 200, 30);
        confirmPassLabel.setBounds(100, 140, 200, 30);
        confirmPassField.setBounds(250, 140, 200, 30);
        emailLabel.setBounds(100, 190, 150, 30);
        emailField.setBounds(250, 190, 200, 30);
        registerBtn.setBounds(180, 250, 140, 40);

        // Add components
        add(userLabel); add(userField);
        add(passLabel); add(passField);
        add(confirmPassLabel); add(confirmPassField);
        add(emailLabel); add(emailField);
        add(registerBtn);

        // Action Listener
        registerBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            String confirmPassword = new String(confirmPassField.getPassword()).trim();
            String email = emailField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required");
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match");
                return;
            }

            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/library_management", "root", "PHW#84#jeor")) {
                // Check if username already exists
                String checkQuery = "SELECT * FROM ulogin WHERE username = ?";
                PreparedStatement checkStmt = con.prepareStatement(checkQuery);
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Username already exists. Please choose another.");
                    return;
                }

                // Insert new user
                String sql = "INSERT INTO ulogin(username, password, email) VALUES (?, ?, ?)";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, username);
                pst.setString(2, password);
                pst.setString(3, email);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(this, "Registration Successful");
                new Ulogin(); // redirect to login
                dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        // Frame setup
        setTitle("User Registration");
        setSize(550, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }
}
