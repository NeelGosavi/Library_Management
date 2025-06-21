package Project;

import javax.swing.*;
import java.awt.*;

public class AboutPage extends JFrame {

    public AboutPage() {
        setTitle("About - Library Management System");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Library Management System", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        JTextArea aboutText = new JTextArea(
                "Version: 1.0\n" +
                        "Developed by: Neel Gosavi\n\n" +
                        "This system allows administrators to manage books, members,\n" +
                        "and transactions efficiently within a library environment."
        );
        aboutText.setEditable(false);
        aboutText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        aboutText.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(title, BorderLayout.NORTH);
        add(aboutText, BorderLayout.CENTER);

        setVisible(true);
    }
}
