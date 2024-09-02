/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javataskmanager;

/**
 *
 * @author brdde
 * @param <T>
 */
public class Task<T> {
    private T id;
    private String name;
    private String description;
    private boolean isComplete;
    private String category;

    public Task(T id, String name, String description, boolean completionStatus, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isComplete = completionStatus;
        this.category = category;
    }

    // Getters and Setters
    public T getId() { return id; }
    public void setId(T id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isComplete() { return isComplete; }
    public void setCompletionStatus(boolean completionStatus) { this.isComplete = completionStatus; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", completionStatus=" + isComplete +
                ", category='" + category + '\'' +
                '}';
    }
}

