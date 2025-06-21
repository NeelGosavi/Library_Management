package Project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class BookManager extends JFrame {
    JTable table;
    DefaultTableModel model;
    JTextField idField, titleField, authorField, genreField;

    public BookManager() {
        setTitle("Book Manager");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(null);

        JLabel idLabel = new JLabel("Book ID:");
        JLabel titleLabel = new JLabel("Title:");
        JLabel authorLabel = new JLabel("Author:");
        JLabel genreLabel = new JLabel("Genre:");

        idField = new JTextField();
        titleField = new JTextField();
        authorField = new JTextField();
        genreField = new JTextField();

        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");
        JButton backBtn = new JButton("Back");

        idLabel.setBounds(20, 20, 100, 30);
        titleLabel.setBounds(20, 60, 100, 30);
        authorLabel.setBounds(20, 100, 100, 30);
        genreLabel.setBounds(20, 140, 100, 30);

        idField.setBounds(130, 20, 200, 30);
        titleField.setBounds(130, 60, 200, 30);
        authorField.setBounds(130, 100, 200, 30);
        genreField.setBounds(130, 140, 200, 30);

        addBtn.setBounds(20, 200, 100, 40);
        updateBtn.setBounds(130, 200, 100, 40);
        deleteBtn.setBounds(240, 200, 100, 40);
        refreshBtn.setBounds(350, 200, 100, 40);
        backBtn.setBounds(460, 200, 100, 40);

        add(idLabel);
        add(titleLabel);
        add(authorLabel);
        add(genreLabel);
        add(idField);
        add(titleField);
        add(authorField);
        add(genreField);
        add(addBtn);
        add(updateBtn);
        add(deleteBtn);
        add(refreshBtn);
        add(backBtn);

        // Table setup
        model = new DefaultTableModel(new String[]{"ID", "Title", "Author", "Genre"}, 0);
        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(360, 20, 400, 160);
        add(sp);

        // Load initial data
        loadBooks();

        // Add Book
        addBtn.addActionListener(e -> {
            String id = idField.getText();
            String title = titleField.getText();
            String author = authorField.getText();
            String genre = genreField.getText();

            if (id.isEmpty() || title.isEmpty() || author.isEmpty() || genre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!");
                return;
            }

            try (Connection con = DBUtil.getConnection()) {
                String sql = "INSERT INTO books (book_id, title, author, genre) VALUES (?, ?, ?, ?)";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, id);
                pst.setString(2, title);
                pst.setString(3, author);
                pst.setString(4, genre);
                pst.executeUpdate();

                JOptionPane.showMessageDialog(this, "Book Added Successfully!");
                clearFields();
                loadBooks();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        // Update Book
        updateBtn.addActionListener(e -> {
            String id = idField.getText();
            String title = titleField.getText();
            String author = authorField.getText();
            String genre = genreField.getText();

            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter Book ID to update.");
                return;
            }

            try (Connection con = DBUtil.getConnection()) {
                String sql = "UPDATE books SET title=?, author=?, genre=? WHERE book_id=?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, title);
                pst.setString(2, author);
                pst.setString(3, genre);
                pst.setString(4, id);
                int rows = pst.executeUpdate();

                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Book Updated Successfully!");
                    clearFields();
                    loadBooks();
                } else {
                    JOptionPane.showMessageDialog(this, "Book ID not found.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        // Delete Book
        deleteBtn.addActionListener(e -> {
            String id = idField.getText();

            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter Book ID to delete.");
                return;
            }

            try (Connection con = DBUtil.getConnection()) {
                String sql = "DELETE FROM books WHERE book_id=?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setString(1, id);
                int rows = pst.executeUpdate();

                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Book Deleted Successfully!");
                    clearFields();
                    loadBooks();
                } else {
                    JOptionPane.showMessageDialog(this, "Book ID not found.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        // Refresh Table
        refreshBtn.addActionListener(e -> loadBooks());

        backBtn.addActionListener(a ->{
            new Home();
            dispose();
        });

        setVisible(true);
    }

    private void loadBooks() {
        model.setRowCount(0);
        try (Connection con = DBUtil.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM books")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genre")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void clearFields() {
        idField.setText("");
        titleField.setText("");
        authorField.setText("");
        genreField.setText("");
    }

    public static void main(String[] args) {
        new BookManager();
    }
}
