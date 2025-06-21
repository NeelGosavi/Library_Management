package Project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class MemberManager extends JFrame {
    JTable table;
    DefaultTableModel model;
    JLabel countLabel;
    JTextField searchField;

    public MemberManager() {
        setTitle("Registered Members");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel heading = new JLabel("Registered Users", JLabel.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 24));
        add(heading, BorderLayout.NORTH);

        // Updated Table Model with Last Login column
        model = new DefaultTableModel(new String[]{"ID", "Username", "Email", "Last Login"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        searchField = new JTextField(15);
        JButton searchBtn = new JButton("Search");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");
        JButton backBtn = new JButton("Back");

        countLabel = new JLabel("Total Users: 0");
        countLabel.setFont(new Font("Arial", Font.BOLD, 16));

        bottomPanel.add(new JLabel("Search Username:"));
        bottomPanel.add(searchField);
        bottomPanel.add(searchBtn);
        bottomPanel.add(deleteBtn);
        bottomPanel.add(refreshBtn);
        bottomPanel.add(countLabel);
        bottomPanel.add(backBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        loadMembers();

        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            loadMembers();
        });

        deleteBtn.addActionListener(e -> deleteSelectedUser());

        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            searchMembers(keyword);
        });

        backBtn.addActionListener(a ->{
            new Home();
            dispose();
        });

        setVisible(true);
    }

    private void loadMembers() {
        model.setRowCount(0);
        int count = 0;
        try (Connection con = DBUtil.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, username, email, last_login FROM ulogin")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getTimestamp("last_login") != null ? rs.getTimestamp("last_login").toString() : "Never"
                });
                count++;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading members: " + ex.getMessage());
        }
        countLabel.setText("Total Users: " + count);
    }

    private void searchMembers(String keyword) {
        model.setRowCount(0);
        int count = 0;
        try (Connection con = DBUtil.getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT id, username, email, last_login FROM ulogin WHERE username LIKE ?")) {
            pst.setString(1, "%" + keyword + "%");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getTimestamp("last_login") != null ? rs.getTimestamp("last_login").toString() : "Never"
                });
                count++;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Search failed: " + ex.getMessage());
        }
        countLabel.setText("Search Results: " + count);
    }

    private void deleteSelectedUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this user?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            int userId = (int) model.getValueAt(selectedRow, 0);

            try (Connection con = DBUtil.getConnection();
                 PreparedStatement pst = con.prepareStatement("DELETE FROM ulogin WHERE id = ?")) {
                pst.setInt(1, userId);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "User deleted successfully.");
                loadMembers();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        new MemberManager();
    }
}
