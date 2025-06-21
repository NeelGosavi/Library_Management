package Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Landing extends JFrame {
    public Landing() {
        setTitle("Library Management System");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        Font titleFont = new Font("Arial", Font.BOLD, 28);
        Font btnFont = new Font("Calibri", Font.PLAIN, 18);

        JLabel heading = new JLabel("Library Management System", JLabel.CENTER);
        heading.setFont(titleFont);
        heading.setBounds(80, 40, 440, 50);
        add(heading);

        JButton adminLoginBtn = new JButton("Admin Login");
        JButton userLoginBtn = new JButton("User Login");

        adminLoginBtn.setFont(btnFont);
        userLoginBtn.setFont(btnFont);

        adminLoginBtn.setBounds(200, 130, 200, 50);
        userLoginBtn.setBounds(200, 200, 200, 50);

        add(adminLoginBtn);
        add(userLoginBtn);

        adminLoginBtn.addActionListener(e -> {
            new Alogin();
            dispose();
        });

        userLoginBtn.addActionListener(e -> {
            new Ulogin();  // You can also navigate to a "Register or Login" choice screen if needed
            dispose();
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new Landing();
    }
}
