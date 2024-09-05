/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javataskmanager;

/**
 *
 * @author brdde
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages a list of tasks, providing methods to add, update, remove, filter, and export
 * tasks.
 */
public class TaskManager {

    private List<Task<Integer>> tasks = new ArrayList<>();

    // Add a task
    public void addTask(Task<Integer> task) {
        tasks.add(task);
    }

    // Get all tasks
    public List<Task<Integer>> getTasks() {
        return tasks;
    }

    // Update a task by ID
    public void updateTask(Task<Integer> updatedTask) {
        for (int i = 0; i < tasks.size(); i++) {
            Task<Integer> task = tasks.get(i);
            if (task.getId().equals(updatedTask.getId())) {
                tasks.set(i, updatedTask);
                return;
            }
        }
    }

    // Remove a task by ID
    public void removeTask(Integer taskId) {
        tasks.removeIf(task -> task.getId().equals(taskId));
    }

    /**
     * Saves tasks to a text file and returns file properties.
     *
     * @param fileName the name of the file to save tasks to
     * @return String containing file properties if file creation was successful, or the
     * error message if not.
     */
    public String saveTasksToFile(String fileName) {
        try (OutputStream os = new FileOutputStream(fileName)) {
            for (Task<Integer> task : tasks) {
                String taskString = task.getId() + "|"
                        + task.getName() + "|"
                        + task.getDescription() + "|"
                        + String.valueOf(task.isComplete()) + "|"
                        + task.getCategory() + System.lineSeparator();
                os.write(taskString.getBytes());
            }

            // Get file properties
            Path filePath = Paths.get(fileName);
            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);

            return "Tasks saved to file: " + fileName
                    + "\nFile size: " + attrs.size() + " bytes"
                    + "\nCreation date: " + attrs.creationTime()
                    + "\nLast modified date: " + attrs.lastModifiedTime();

        } catch (IOException e) {
            return "An error occurred while saving tasks to file: " + e.getMessage();
        }
    }

    /**
     * Imports tasks from a text file.
     *
     * @param file the uploaded file to import tasks from
     * @return String containing a success message if import was successful, or the error
     * message if not.
     */
    public String importTasksFromFile(File file) {
        try (InputStream is = new FileInputStream(file); BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Task<Integer> task = parseTask(line);
                if (task != null) {
                    addTask(task);
                }
            }
            return "Tasks imported from file: " + file.getName();
        } catch (IOException e) {
            return "An error occurred while importing tasks from file: " + e.getMessage();
        }
    }

    /**
     * Parses a task from a string.
     *
     * @param line the string representation of a task
     * @return the parsed task, or null if the format is invalid
     */
    private Task<Integer> parseTask(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 5) {
            return null; // Invalid task format
        }
        try {
            int id = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();
            String description = parts[2].trim();
            boolean completionStatus = Boolean.parseBoolean(parts[3].trim());
            String category = parts[4].trim();
            return new Task<>(id, name, description, completionStatus, category);
        } catch (NumberFormatException e) {
            return null; // Invalid number format
        }
    }

    /**
     * Exports tasks to a CSV file and returns the file properties.
     *
     * @param fileName the name of the CSV file to export tasks to
     * @return String containing file properties if file creation was successful, or the
     * error message if not.
     */
    public String exportTasksToCSV(String fileName) {
        Path filePath = Paths.get(fileName);
        List<String> lines = new ArrayList<>();
        lines.add("ID,Name,Description,CompletionStatus,Category"); // CSV header
        for (Task<Integer> task : tasks) {
            lines.add(task.getId() + "," 
                    + escapeCSV(task.getName()) + "," 
                    + escapeCSV(task.getDescription()) + "," 
                    + task.isComplete() + "," 
                    + escapeCSV(task.getCategory()));
        }
        try {
            Files.write(filePath, lines);
            System.out.println("Tasks exported to CSV file: " + fileName);

            // Display file properties
            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);

            return "exported to CSV file: " + fileName
                    + "\nFile size: " + attrs.size() + " bytes"
                    + "\nCreation date: " + attrs.creationTime()
                    + "\nLast modified date: " + attrs.lastModifiedTime();

        } catch (IOException e) {
            return "An error occurred while exporting tasks to CSV file: " + e.getMessage();
        }
    }

    /**
     * Escapes a string for CSV format.
     *
     * @param value the string to be escaped
     * @return the escaped string
     */
    private String escapeCSV(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

}
