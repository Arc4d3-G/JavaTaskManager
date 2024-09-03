/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javataskmanager;

/**
 *
 * @author brdde
 */
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TaskManager {

    private List<Task<Integer>> tasks = new ArrayList<>();

    public void addTask(Task<Integer> task) {
        tasks.add(task);
    }

    public List<Task<Integer>> filterTasks(Predicate<Task<Integer>> criteria) {
        return tasks.stream().filter(criteria).collect(Collectors.toList());
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

}
