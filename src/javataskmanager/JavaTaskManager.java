/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package javataskmanager;

/**
 *
 * @author brdde
 */
public class JavaTaskManager {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // Create Class Instances
        TaskManager taskManager = new TaskManager();
        TaskDAO taskDao = new TaskDAO();
        
        // Get all tasks from db and add to taskManager instance
        taskDao.getAllTasks().forEach((task) -> {
            taskManager.addTask(task);
        });
        
        // Instaniate GUI frame and pass classes as parameters
        TaskManagerFrame tmf = new TaskManagerFrame(taskManager, taskDao);
        tmf.setVisible(true);
        
    }

}
