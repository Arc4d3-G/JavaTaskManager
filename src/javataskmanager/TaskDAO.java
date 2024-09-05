/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javataskmanager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) class for managing tasks in the SQLite database.
 */
public class TaskDAO {

    private String url = "jdbc:sqlite:task_manager_db.db";

    /**
     * Creates a new task in the database.
     *
     * @param task the task to be created
     */
    public String createTask(Task<Integer> task) {
        String sql = "INSERT INTO tasks(name, description, completion_status, category) VALUES(?, ?, ?, ?)";
        int id = 0;
        try (Connection conn = DriverManager.getConnection(url); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getName());
            pstmt.setString(2, task.getDescription());
            pstmt.setInt(3, task.isComplete() ? 1 : 0);
            pstmt.setString(4, task.getCategory());
            pstmt.executeUpdate();
            id = pstmt.getGeneratedKeys().getInt(1);
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }

        return ("Success | " + id);
    }

    /**
     * Retrieves all tasks from the database.
     *
     * @return a list of all tasks
     */
    public List<Task<Integer>> getAllTasks() {
        List<Task<Integer>> tasks = new ArrayList<>();
        String sql = "SELECT id, name, description, completion_status, category FROM tasks";

        try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Task<Integer> task = new Task<>(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("completion_status") == 1,
                        rs.getString("category")
                );
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return tasks;
    }

    /**
     * Updates an existing task in the database.
     *
     * @param task the task with updated information
     * @return String indicating success or error.
     */
    public String updateTask(Task<Integer> task) {
        String sql = "UPDATE tasks SET name = ?, description = ?, completion_status = ?, category = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getName());
            pstmt.setString(2, task.getDescription());
            pstmt.setInt(3, task.isComplete() ? 1 : 0);
            pstmt.setString(4, task.getCategory());
            pstmt.setInt(5, task.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }

        return "Success";
    }

    /**
     * Deletes a task from the database by its ID.
     *
     * @param id the ID of the task to be deleted
     * @return String indicating success or error.
     */
    public String deleteTask(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }

        return "Success";
    }
}
