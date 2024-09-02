/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javataskmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author brdde
 */
public class TaskCategorizer {

    public Map<String, List<Task<Integer>>> categorizeTasks(List<Task<Integer>> tasks) {
        Map<String, List<Task<Integer>>> categorizedTasks = new HashMap<>();
        for (Task<Integer> task : tasks) {
            categorizedTasks.computeIfAbsent(task.getCategory(), k -> new ArrayList<>()).add(task);
        }
        return categorizedTasks;
    }
}
