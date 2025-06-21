package Project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class BookRequestManager extends JFrame {

    JTable requestTable;

    public BookRequestManager() {
        setTitle("Book Requests - Admin");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(null);

        JLabel title = new JLabel("Pending Book Requests");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBounds(20, 10, 300, 30);
        add(title);

        requestTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(requestTable);
        scrollPane.setBounds(20, 60, 740, 220);
        add(scrollPane);

        JButton issueBtn = new JButton("Issue Selected Book");
        issueBtn.setBounds(20, 300, 200, 30);
        add(issueBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBounds(240, 300, 100, 30);
        add(refreshBtn);

        loadRequests();

        JButton backBtn = new JButton("Back");
        backBtn.setBounds(360, 300, 100, 30);
        add(backBtn);

        issueBtn.addActionListener(e -> issueSelectedBook());
        refreshBtn.addActionListener(e -> loadRequests());
        backBtn.addActionListener(e -> {
            dispose(); // close current window
            new Home(); // open Admin Home screen
        });

        setVisible(true);
    }

    private void loadRequests() {
        try (Connection con = DBUtil.getConnection()) {
            String sql = "SELECT br.request_id, br.username, br.book_id, b.title, br.request_date " +
                    "FROM book_requests br JOIN books b ON br.book_id = b.book_id";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            DefaultTableModel model = new DefaultTableModel(
                    new String[]{"Request ID", "Username", "Book ID", "Book Title", "Request Date"}, 0);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("request_id"),
                        rs.getString("username"),
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getDate("request_date")
                });
            }

            requestTable.setModel(model);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading requests: " + e.getMessage());
        }
    }

    private void issueSelectedBook() {
        int row = requestTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request.");
            return;
        }

        int requestId = (int) requestTable.getValueAt(row, 0);
        String username = requestTable.getValueAt(row, 1).toString();
        int bookId = (int) requestTable.getValueAt(row, 2);

        try (Connection con = DBUtil.getConnection()) {

            // 1. Get user ID from username
            int userId = -1;
            PreparedStatement getUser = con.prepareStatement("SELECT id FROM ulogin WHERE username = ?");
            getUser.setString(1, username);
            ResultSet userRs = getUser.executeQuery();
            if (userRs.next()) {
                userId = userRs.getInt("id");
            } else {
                JOptionPane.showMessageDialog(this, "User not found.");
                return;
            }

            // 2. Check if book is already issued
            PreparedStatement checkIssued = con.prepareStatement(
                    "SELECT COUNT(*) FROM transactions WHERE book_id = ? AND return_status = 'Not Returned'");
            checkIssued.setInt(1, bookId);
            ResultSet rs = checkIssued.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "This book is already issued.");
                return;
            }

            // 3. Issue the book
            LocalDate issueDate = LocalDate.now();
            LocalDate dueDate = issueDate.plusDays(14); // 2-week period

            String issueSql = "INSERT INTO transactions (id, book_id, issue_date, due_date, return_status) " +
                    "VALUES (?, ?, ?, ?, 'Not Returned')";
            PreparedStatement issueStmt = con.prepareStatement(issueSql);
            issueStmt.setInt(1, userId);
            issueStmt.setInt(2, bookId);
            issueStmt.setDate(3, Date.valueOf(issueDate));
            issueStmt.setDate(4, Date.valueOf(dueDate));
            issueStmt.executeUpdate();

            // 4. Delete the request
            PreparedStatement deleteRequest = con.prepareStatement("DELETE FROM book_requests WHERE request_id = ?");
            deleteRequest.setInt(1, requestId);
            deleteRequest.executeUpdate();

            JOptionPane.showMessageDialog(this, "Book issued successfully!");
            loadRequests();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error issuing book: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new BookRequestManager();
    }
}
