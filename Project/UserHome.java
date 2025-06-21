package Project;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class UserHome extends JFrame {
    private String currentUser;
    private JTable bookTable, issueTable;
    private JTextField searchField;

    public UserHome(String username) {
        currentUser = username;
        setTitle("User Dashboard - Library Management");
        setSize(950, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel welcome = new JLabel("Welcome, " + currentUser);
        welcome.setFont(new Font("Arial", Font.BOLD, 22));
        welcome.setBounds(20, 10, 400, 30);
        add(welcome);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBounds(800, 10, 100, 30);
        add(logoutBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBounds(680, 10, 100, 30);
        add(refreshBtn);

        // Search bar
        searchField = new JTextField();
        searchField.setBounds(20, 60, 300, 25);
        add(searchField);

        JButton searchBtn = new JButton("Search");
        searchBtn.setBounds(330, 60, 90, 25);
        add(searchBtn);

        JLabel allBooksLbl = new JLabel("Available Books:");
        allBooksLbl.setFont(new Font("Arial", Font.BOLD, 18));
        allBooksLbl.setBounds(20, 90, 200, 25);
        add(allBooksLbl);

        bookTable = new JTable();
        JScrollPane scrollBooks = new JScrollPane(bookTable);
        scrollBooks.setBounds(20, 120, 880, 180);
        add(scrollBooks);

        JButton requestBtn = new JButton("Request Selected Book");
        requestBtn.setBounds(680, 310, 220, 30);
        add(requestBtn);

        JLabel issuedBooksLbl = new JLabel("Your Issued Books:");
        issuedBooksLbl.setFont(new Font("Arial", Font.BOLD, 18));
        issuedBooksLbl.setBounds(20, 350, 250, 25);
        add(issuedBooksLbl);

        issueTable = new JTable();
        JScrollPane scrollIssued = new JScrollPane(issueTable);
        scrollIssued.setBounds(20, 380, 880, 250);
        add(scrollIssued);

        // Load tables
        loadAvailableBooks("");
        loadIssuedBooks();

        // Actions
        refreshBtn.addActionListener(e -> {
            loadAvailableBooks("");
            loadIssuedBooks();
        });

        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            loadAvailableBooks(keyword);
        });

        logoutBtn.addActionListener(e -> {
            new Landing();
            dispose();
        });

        requestBtn.addActionListener(e -> requestSelectedBook());

        setVisible(true);
    }

    private void loadAvailableBooks(String keyword) {
        try (Connection con = DBUtil.getConnection()) {
            String sql = "SELECT book_id, title, author, genre, available FROM books WHERE title LIKE ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, "%" + keyword + "%");
            ResultSet rs = pst.executeQuery();

            DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Title", "Author", "Genre", "Status"}, 0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genre"),
                        rs.getString("available").equals("yes") ? "Available" : "Issued"
                });
            }

            bookTable.setModel(model);
            bookTable.setAutoCreateRowSorter(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading books: " + e.getMessage());
        }
    }

    private void loadIssuedBooks() {
        try (Connection con = DBUtil.getConnection()) {
            String sql = "SELECT b.title, t.issue_date, t.due_date, t.return_status " +
                    "FROM transactions t " +
                    "JOIN books b ON t.book_id = b.book_id " +
                    "JOIN ulogin u ON t.id = u.id " +
                    "WHERE u.username = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, currentUser);
            ResultSet rs = pst.executeQuery();

            DefaultTableModel model = new DefaultTableModel(new String[]{"Title", "Issued", "Due", "Status"}, 0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("title"),
                        rs.getDate("issue_date"),
                        rs.getDate("due_date"),
                        rs.getString("return_status")
                });
            }

            issueTable.setModel(model);
            issueTable.setDefaultRenderer(Object.class, new OverdueRenderer());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading issued books: " + e.getMessage());
        }
    }

    private void requestSelectedBook() {
        int row = bookTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to request.");
            return;
        }

        int bookId = (int) bookTable.getValueAt(row, 0);

        // Real-time check for availability
        if (!isBookAvailable(bookId)) {
            JOptionPane.showMessageDialog(this, "This book is already issued.");
            return;
        }

        try (Connection con = DBUtil.getConnection()) {
            String checkSql = "SELECT * FROM book_requests WHERE username = ? AND book_id = ?";
            PreparedStatement checkStmt = con.prepareStatement(checkSql);
            checkStmt.setString(1, currentUser);
            checkStmt.setInt(2, bookId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "You have already requested this book.");
                return;
            }

            String sql = "INSERT INTO book_requests (username, book_id, request_date) VALUES (?, ?, NOW())";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, currentUser);
            pst.setInt(2, bookId);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Book request sent successfully!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error requesting book: " + e.getMessage());
        }
    }

    private boolean isBookAvailable(int bookId) {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement pst = con.prepareStatement(
                     "SELECT COUNT(*) FROM transactions WHERE book_id = ? AND return_status = 'Not Returned'"
             )) {
            pst.setInt(1, bookId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;  // True if no active transaction exists
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error checking availability: " + e.getMessage());
        }
        return false;
    }



    // Custom renderer to highlight overdue rows
    static class OverdueRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, val, isSel, hasFocus, row, col);
            String status = (String) table.getValueAt(row, 3);
            java.sql.Date dueDate = (java.sql.Date) table.getValueAt(row, 2);
            java.util.Date today = new java.util.Date();

            if ("Not Returned".equals(status) && dueDate.before(today)) {
                c.setBackground(Color.PINK);
            } else {
                c.setBackground(Color.WHITE);
            }
            return c;
        }
    }

    public static void main(String[] args) {
        new UserHome("Neel");
    }
}
