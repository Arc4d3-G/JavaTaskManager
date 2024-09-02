/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package javataskmanager;

import java.util.List;
import java.util.Map;

/**
 *
 * @author brdde
 */
public class JavaTaskManager {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        TaskDAO taskDao = new TaskDAO();
        // Adding some tasks
        taskManager.addTask(new Task<>(null, "Complete project report", "Finish the final report for the project", false, "Work"));
        taskManager.addTask(new Task<>(null, "Buy groceries", "Buy milk, eggs, and bread", false, "Personal"));
        taskManager.addTask(new Task<>(null, "Workout", "Go to the gym for an hour", true, "Health"));
        
//        taskDao.getAllTasks().forEach(( Task<Integer> task) -> {
//            taskDao.deleteTask(task.getId());
//        });

//        TaskCategorizer taskCategorizer = new TaskCategorizer();
//        Map<String, List<Task<Integer>>> categorizedTasks = taskCategorizer.categorizeTasks(taskManager.getTasks());
//
//        List<Task<Integer>> incompleteTasks = taskManager.filterTasks(task -> !task.isComplete());
//        incompleteTasks.forEach(System.out::println);
//
//        // Printing categorized tasks
//        categorizedTasks.forEach((category, tasks) -> {
//            System.out.println("Category: " + category);
////            tasks.forEach(System.out::println);
//        });
    

    }

}
