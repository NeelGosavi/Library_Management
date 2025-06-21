package Project;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Home extends JFrame {

    // Labels for dynamic data
    private JLabel totalBooksLabel, totalMembersLabel, activeTransLabel, overdueBooksLabel;

    public Home() {
        setTitle("Library Management - Admin Dashboard");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Menu Bar (Navbar)
        JMenuBar menuBar = new JMenuBar();

        JMenu manageMenu = new JMenu("Manage");
        JMenuItem booksItem = new JMenuItem("Books");
        JMenuItem membersItem = new JMenuItem("Members");
        JMenuItem transactionsItem = new JMenuItem("Transactions");
        JMenuItem requestItem = new JMenuItem("Book Requests");

        manageMenu.add(booksItem);
        manageMenu.add(membersItem);
        manageMenu.add(transactionsItem);
        manageMenu.add(requestItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        helpMenu.add(aboutItem);

        JMenu logoutMenu = new JMenu("Logout");
        logoutMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dispose();
                new Landing();
            }
        });

        menuBar.add(manageMenu);
        menuBar.add(helpMenu);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(logoutMenu);
        setJMenuBar(menuBar);

        // Title
        JLabel title = new JLabel("Library Admin Dashboard", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Dashboard Panel
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        totalBooksLabel = createStatBox("Total Books", "...");
        totalMembersLabel = createStatBox("Total Members", "...");
        activeTransLabel = createStatBox("Active Transactions", "...");
        overdueBooksLabel = createStatBox("Overdue Books", "...");

        statsPanel.add(totalBooksLabel);
        statsPanel.add(totalMembersLabel);
        statsPanel.add(activeTransLabel);
        statsPanel.add(overdueBooksLabel);

        add(statsPanel, BorderLayout.CENTER);

        // Menu Actions
        booksItem.addActionListener(e -> openPage(new BookManager()));
        membersItem.addActionListener(e -> openPage(new MemberManager()));
        transactionsItem.addActionListener(e -> openPage(new TransactionManager()));
        aboutItem.addActionListener(e -> new AboutPage());
        requestItem.addActionListener(e -> openPage(new BookRequestManager()));

        // Load stats from DB
        loadDashboardStats();

        setVisible(true);
    }

    private JLabel createStatBox(String title, String value) {
        JLabel box = new JLabel(formatStat(title, value), JLabel.CENTER);
        box.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        box.setOpaque(true);
        box.setBackground(new Color(230, 240, 255));
        box.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return box;
    }

    private String formatStat(String title, Object value) {
        return "<html><center><b>" + title + "</b><br>" + value + "</center></html>";
    }

    private void loadDashboardStats() {
        totalBooksLabel.setText(formatStat("Total Books", getTotalBooks()));
        totalMembersLabel.setText(formatStat("Total Members", getTotalMembers()));
        activeTransLabel.setText(formatStat("Active Transactions", getActiveTransactions()));
        overdueBooksLabel.setText(formatStat("Overdue Books", getOverdueBooks()));
    }

    private int getTotalBooks() {
        return fetchSingleInt("SELECT COUNT(*) FROM books");
    }

    private int getTotalMembers() {
        return fetchSingleInt("SELECT COUNT(*) FROM ulogin");
    }

    private int getActiveTransactions() {
        return fetchSingleInt("SELECT COUNT(*) FROM transactions WHERE return_status = 'Not Returned'");
    }

    private int getOverdueBooks() {
        return fetchSingleInt("SELECT COUNT(*) FROM transactions WHERE return_status = 'Not Returned' AND due_date < CURDATE()");
    }

    private int fetchSingleInt(String query) {
        int result = 0;
        try (Connection con = DBUtil.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                result = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void openPage(JFrame frame) {
        frame.setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        new Home();
    }
}
