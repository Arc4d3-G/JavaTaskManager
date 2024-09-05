package javataskmanager;

/**
 * Represents a task with a generic ID type.
 *
 * @param <T> the type of the task ID
 */
public class Task<T> {

    private T id;
    private String name;
    private String description;
    private boolean isComplete;
    private String category;

    /**
     * Constructs a new Task with the specified details.
     *
     * @param id the unique identifier for the task
     * @param name the name of the task
     * @param description the description of the task
     * @param completionStatus the completion status of the task (true if completed, false otherwise)
     * @param category the category of the task
     */
    public Task(T id, String name, String description, boolean completionStatus, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isComplete = completionStatus;
        this.category = category;
    }

    // Getters and Setters
    public T getId() {
        return id;
    }

    public void setId(T id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setCompletionStatus(boolean completionStatus) {
        this.isComplete = completionStatus;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
