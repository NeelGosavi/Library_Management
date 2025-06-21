package Project;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Alogin extends JFrame {
    public Alogin() {
        setTitle("Admin Login");
        setSize(600, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        Font f = new Font("Calibri", Font.PLAIN, 18);
        Font f2 = new Font("Arial", Font.BOLD, 24);

        JLabel title = new JLabel("Admin Login", JLabel.CENTER);
        title.setFont(f2);
        title.setBounds(200, 30, 200, 40);
        add(title);

        JLabel l1 = new JLabel("Username:");
        JTextField t1 = new JTextField();

        JLabel l2 = new JLabel("Password:");
        JPasswordField t2 = new JPasswordField();

        JButton b1 = new JButton("Login");
        JButton b2 = new JButton("Back");

        l1.setFont(f); l2.setFont(f);
        t1.setFont(f); t2.setFont(f);
        b1.setFont(f); b2.setFont(f);

        l1.setBounds(150, 100, 100, 30);
        t1.setBounds(270, 100, 180, 30);

        l2.setBounds(150, 150, 100, 30);
        t2.setBounds(270, 150, 180, 30);

        b1.setBounds(180, 220, 100, 40);
        b2.setBounds(310, 220, 100, 40);

        add(l1); add(t1);
        add(l2); add(t2);
        add(b1); add(b2);

        b1.addActionListener(e -> {
            String username = t1.getText();
            String password = new String(t2.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection con = DBUtil.getConnection()) {
                String sql = "SELECT * FROM alogin WHERE username=? AND password=?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, username);
                pst.setString(2, password);
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    new Home(); // or AdminHome if separate
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Credentials! Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        b2.addActionListener(e -> {
            new Landing();
            dispose();
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new Alogin();
    }
}
