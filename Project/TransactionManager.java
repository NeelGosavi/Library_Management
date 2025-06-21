package Project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

public class TransactionManager extends JFrame {
    JTable table;
    DefaultTableModel model;
    JLabel statsLabel;

    public TransactionManager() {
        setTitle("Transaction Manager");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel heading = new JLabel("Book Transactions", JLabel.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 24));
        add(heading, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{
                "S.No", "Username", "Book Title", "Issue Date", "Due Date", "Status", "Fine"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Completely non-editable
            }
        };

        table = new JTable(model) {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                String status = model.getValueAt(row, 5).toString();
                if (status.equalsIgnoreCase("Overdue")) {
                    c.setBackground(new Color(255, 204, 204));
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };
        table.getTableHeader().setReorderingAllowed(false);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout());
        JTextField searchField = new JTextField(15);
        JButton issueBtn = new JButton("Issue Book");
        JButton returnBtn = new JButton("Return Book");
        JButton refreshBtn = new JButton("Refresh");
        JButton exportBtn = new JButton("Export CSV");
        JButton backBtn = new JButton("Back");

        controlPanel.add(new JLabel("Search:"));
        controlPanel.add(searchField);
        controlPanel.add(issueBtn);
        controlPanel.add(returnBtn);
        controlPanel.add(refreshBtn);
        controlPanel.add(exportBtn);
        controlPanel.add(backBtn);
        add(controlPanel, BorderLayout.SOUTH);

        statsLabel = new JLabel("Total: 0 | Issued: 0 | Overdue: 0", JLabel.CENTER);
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        add(statsLabel, BorderLayout.NORTH);

        loadTransactions();

        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            loadTransactions();
        });

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filter(searchField.getText().trim());
            }
        });

        issueBtn.addActionListener(e -> issueBook());
        returnBtn.addActionListener(e -> returnBook());
        exportBtn.addActionListener(e -> exportToCSV());

        backBtn.addActionListener(a ->{
            new Home();
            dispose();
        });

        setVisible(true);
    }

    private void loadTransactions() {
        model.setRowCount(0);
        int total = 0, issued = 0, overdue = 0;

        try (Connection con = DBUtil.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT t.trans_id, u.username AS username, b.title AS book_title, " +
                             "t.issue_date, t.due_date, t.return_status " +
                             "FROM transactions t " +
                             "JOIN ulogin u ON t.id = u.id " +
                             "JOIN books b ON t.book_id = b.book_id")) {

            int serial = 1;
            while (rs.next()) {
                String status = rs.getString("return_status");
                LocalDate due = rs.getDate("due_date").toLocalDate();
                LocalDate now = LocalDate.now();
                String displayStatus = status;

                if (status.equals("Not Returned") && now.isAfter(due)) {
                    displayStatus = "Overdue";
                    overdue++;
                }

                String fine = displayStatus.equals("Overdue") ? "₹" + ChronoUnit.DAYS.between(due, now) * 5 : "₹0";

                model.addRow(new Object[]{
                        serial++,
                        rs.getString("username"),
                        rs.getString("book_title"),
                        rs.getDate("issue_date"),
                        rs.getDate("due_date"),
                        displayStatus,
                        fine
                });

                total++;
                if (status.equals("Not Returned")) issued++;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage());
        }

        statsLabel.setText("Total: " + total + " | Issued: " + issued + " | Overdue: " + overdue);
    }

    private void filter(String keyword) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
    }

    private void issueBook() {
        JTextField userIdField = new JTextField();
        JTextField bookIdField = new JTextField();
        Object[] fields = {
                "User ID (ulogin.id):", userIdField,
                "Book ID:", bookIdField
        };
        int opt = JOptionPane.showConfirmDialog(this, fields, "Issue Book", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            try {
                int userId = Integer.parseInt(userIdField.getText().trim());
                int bookId = Integer.parseInt(bookIdField.getText().trim());

                LocalDate today = LocalDate.now();
                LocalDate due = today.plusDays(14);

                try (Connection con = DBUtil.getConnection();
                     PreparedStatement pst = con.prepareStatement(
                             "INSERT INTO transactions (id, book_id, issue_date, due_date, return_status) VALUES (?, ?, ?, ?, 'Not Returned')")) {
                    pst.setInt(1, userId);
                    pst.setInt(2, bookId);
                    pst.setDate(3, Date.valueOf(today));
                    pst.setDate(4, Date.valueOf(due));
                    pst.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Book issued.");
                    loadTransactions();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter valid numeric IDs.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error issuing book: " + e.getMessage());
            }
        }
    }

    private void returnBook() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a transaction to return.");
            return;
        }

        int transId = Integer.parseInt(model.getValueAt(row, 0).toString());
        LocalDate due = ((Date) model.getValueAt(row, 4)).toLocalDate();
        LocalDate now = LocalDate.now();
        long fine = ChronoUnit.DAYS.between(due, now);
        int totalFine = (fine > 0) ? (int) fine * 5 : 0;

        try (Connection con = DBUtil.getConnection();
             PreparedStatement pst = con.prepareStatement(
                     "UPDATE transactions SET return_status = 'Returned' WHERE trans_id = ?")) {
            pst.setInt(1, transId);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Book returned. Fine: ₹" + totalFine);
            loadTransactions();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error returning book: " + e.getMessage());
        }
    }

    private void exportToCSV() {
        try (FileWriter fw = new FileWriter("transactions.csv")) {
            for (int i = 0; i < model.getColumnCount(); i++) {
                fw.append(model.getColumnName(i)).append(",");
            }
            fw.append("\n");

            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    fw.append(model.getValueAt(i, j).toString()).append(",");
                }
                fw.append("\n");
            }
            JOptionPane.showMessageDialog(this, "Exported to transactions.csv");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new TransactionManager();
    }
}
