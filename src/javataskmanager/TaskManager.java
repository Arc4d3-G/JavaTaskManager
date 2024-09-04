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

public class TaskManager {

    private List<Task<Integer>> tasks = new ArrayList<>();

    public void addTask(Task<Integer> task) {
        tasks.add(task);
    }

    public List<Task<Integer>> getTasks() {
        return tasks;
    }

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

    // Save tasks to a text file
    public String saveTasksToFile(String fileName) {
        try (OutputStream os = new FileOutputStream(fileName)) {
            for (Task<Integer> task : tasks) {
                String taskString = task.getId() + ","
                        + task.getName() + ","
                        + task.getDescription() + ","
                        + String.valueOf(task.isComplete()) + ","
                        + task.getCategory() + System.lineSeparator();
                os.write(taskString.getBytes());
            }

            // Display file properties
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

    // Import tasks from a text file
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

    // Parse a task from a string
    private Task<Integer> parseTask(String line) {
        String[] parts = line.split(",");
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

    // Export tasks to a CSV file
    public String exportTasksToCSV(String fileName) {
        Path filePath = Paths.get(fileName);
        List<String> lines = new ArrayList<>();
        lines.add("ID,Name,Description,CompletionStatus,Category"); // CSV header
        for (Task<Integer> task : tasks) {
            lines.add(task.getId() + ","
                    + task.getName() + ","
                    + task.getDescription() + ","
                    + String.valueOf(task.isComplete()) + ","
                    + task.getCategory());
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

}
