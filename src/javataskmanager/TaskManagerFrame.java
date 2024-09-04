/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package javataskmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author brdde
 */
public class TaskManagerFrame extends javax.swing.JFrame {

    TaskManager taskManager;
    TaskDAO taskDao;
    TaskCategorizer taskCategorizer;
    DefaultTableModel tableModel;
    HandleGUI gui;

    /**
     * Creates new form TaskManagerFrame
     *
     * @param taskManager
     * @param taskDao
     */
    public TaskManagerFrame(TaskManager taskManager, TaskDAO taskDao) {
        this.taskManager = taskManager;
        this.taskDao = taskDao;
        this.taskCategorizer = new TaskCategorizer();
        this.gui = new HandleGUI();

        this.tableModel = (DefaultTableModel) new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableModel.addColumn("ID");
        tableModel.addColumn("Name");
        tableModel.addColumn("Description");
        tableModel.addColumn("is_completed");
        tableModel.addColumn("Category");

        initComponents();
        gui.refreshCategoryCombo();
        gui.refreshTaskTable();
    }

    class HandleGUI {

        void populateTaskDetails() {
            int idCol = 0;
            int nameCol = 1;
            int descCol = 2;
            int completionCol = 3;
            int categoryCol = 4;

            int row = TaskTable.getSelectedRow();
            if (row != -1) {
                EditButton.setEnabled(true);
                DeleteButton.setEnabled(true);
            }
            String id = TaskTable.getModel().getValueAt(row, idCol).toString();
            String name = TaskTable.getModel().getValueAt(row, nameCol).toString();
            String description = TaskTable.getModel().getValueAt(row, descCol).toString();
            boolean completionStatus = (boolean) TaskTable.getModel().getValueAt(row, completionCol);
            String category = TaskTable.getModel().getValueAt(row, categoryCol).toString();

            IDField.setText(id);
            NameField.setText(name);
            CategoryField.setText(category);
            CompletedCheck.setSelected(completionStatus);
            DescriptionTextArea.setText(description);
        }

        void handleTaskEdit() {

            int row = TaskTable.getSelectedRow();
            if (row == -1) {
                return;
            }

            if (EditButton.getText() == "Edit") {

                NameField.setEnabled(true);
                NameField.setEditable(true);
                CategoryField.setEnabled(true);
                CategoryField.setEditable(true);
                CompletedCheck.setEnabled(true);
                DescriptionTextArea.setEnabled(true);
                DescriptionTextArea.setEditable(true);

                EditButton.setText("Save");
            } else {

                int id = Integer.parseInt(IDField.getText());
                String name = NameField.getText();
                String description = DescriptionTextArea.getText();
                boolean completionStatus = CompletedCheck.isSelected();
                String category = CategoryField.getText();
                Task<Integer> newTask = new Task<>(id, name, description, completionStatus, category);
                String response = taskDao.updateTask(newTask);

                if (response == "Success") {

                    taskManager.updateTask(newTask);

                    NameField.setEnabled(false);
                    NameField.setEditable(false);
                    CategoryField.setEnabled(false);
                    CategoryField.setEditable(false);
                    CompletedCheck.setEnabled(false);
                    DescriptionTextArea.setEnabled(false);
                    DescriptionTextArea.setEditable(false);
                    EditButton.setText("Edit");
                    refreshCategoryCombo();
                    refreshTaskTable();
                } else {
                    System.out.println(response);
                }
            }
        }

        void handleTaskDelete() {
            int id = Integer.parseInt(IDField.getText());
            String response = taskDao.deleteTask(id);

            if (response == "Success") {
                taskManager.removeTask(id);
                refreshCategoryCombo();
                refreshTaskTable();
            }
        }

        void handleTaskCreate() {
            String name = NewTaskNameField.getText();
            String category = NewTaskCategoryField.getText();
            String description = NewTaskDescriptionTextArea.getText();

            Task<Integer> newTask = new Task<>(null, name, description, false, category);
            String response = taskDao.createTask(newTask);

            if (response.contains("Success")) {
                System.out.println(response);
                int newId = Integer.parseInt(response.split(" \\| ")[1]);
                newTask.setId(newId);
                taskManager.addTask(newTask);
                NewTaskDialog.dispose();
                refreshCategoryCombo();
                refreshTaskTable();
            } else {
                System.out.println(response);
            }
        }

        // Clear and populate CategoryCombo box with all categories (hashmap keys)
        void refreshCategoryCombo() {
            // Get all tasks in taskManager
            List<Task<Integer>> allTasks = taskManager.getTasks();

            // Create hashmap based on task categories
            Map<String, List<Task<Integer>>> categorizedTasks = taskCategorizer.categorizeTasks(allTasks);
            CategoryCombo.removeAllItems();
            CategoryCombo.addItem("-- Show All --");
            categorizedTasks.forEach((category, tasks) -> {
                CategoryCombo.addItem(category);
            });
        }

        List<Task<Integer>> filterBySearch(List<Task<Integer>> tasks) {
            String query = SearchField.getText().trim().toLowerCase();
            String column = (String) SearchCombo.getSelectedItem();

            System.out.println(query);
            if (query.isBlank()) {
                return tasks;
            } else {
                if (column == "Name") {
                    return tasks.stream().filter(task -> task.getName().toLowerCase().contains(query)).collect(Collectors.toList());
                } else {
                    try {
                        int id = Integer.parseInt(query);
                        return tasks.stream().filter(task -> task.getId().equals(id)).collect(Collectors.toList());
                    } catch (NumberFormatException e) {
                        return new ArrayList<>(); // Return empty list if ID is not a valid number
                    }
                }
            }
        }

        void refreshTaskTable() {
            System.out.println("refreshed");
            // Clear table of all rows
            tableModel.setRowCount(0);

            EditButton.setEnabled(false);
            DeleteButton.setEnabled(false);

            IDField.setText(null);
            NameField.setText(null);
            CategoryField.setText(null);
            CompletedCheck.setSelected(false);
            DescriptionTextArea.setText(null);

            // Get all tasks in taskManager
            List<Task<Integer>> searchResults = filterBySearch(taskManager.getTasks());

            // Create hashmap based on task categories
            Map<String, List<Task<Integer>>> categorizedTasks = taskCategorizer.categorizeTasks(searchResults);

            String selectedCategory = (String) CategoryCombo.getSelectedItem();
            List<Task<Integer>> tasksToFilter;
            if (selectedCategory == "-- Show All --") {
                tasksToFilter = searchResults;
            } else {
                tasksToFilter = categorizedTasks.get(selectedCategory);
            }

            // Filter all tasks by user criteria (seach box + show completed tickbox)
            boolean showCompleted = ShowCompCheck.isSelected();
            List<Task<Integer>> filteredTasks;

            if (!showCompleted) {
                filteredTasks = tasksToFilter.stream().filter(task -> !task.isComplete()).collect(Collectors.toList());
            } else {
                filteredTasks = tasksToFilter;
            }

            if (filteredTasks != null) {
                filteredTasks.forEach(task -> {
                    int id = task.getId();
                    String name = task.getName();
                    String desc = task.getDescription();
                    boolean completeStatus = task.isComplete();
                    String category = task.getCategory();

                    tableModel.addRow(new Object[]{id,
                        name,
                        desc,
                        completeStatus,
                        category});
                });
            }

        }
    }

    class TaskCategorizer {

        Map<String, List<Task<Integer>>> categorizeTasks(List<Task<Integer>> tasks) {
            Map<String, List<Task<Integer>>> categorizedTasks = new HashMap<>();
            for (Task<Integer> task : tasks) {
                categorizedTasks.computeIfAbsent(task.getCategory(), k -> new ArrayList<>()).add(task);
            }
            return categorizedTasks;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING:
     * Do NOT modify this code. The content of this method is always regenerated by the
     * Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        NewTaskDialog = new javax.swing.JDialog();
        jPanel9 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        NewTaskCategoryField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        NewTaskDescriptionTextArea = new javax.swing.JTextArea();
        NewTaskNameField = new javax.swing.JTextField();
        jPanel10 = new javax.swing.JPanel();
        CreateButton = new javax.swing.JButton();
        CancelButton = new javax.swing.JButton();
        ExportResultDialog = new javax.swing.JDialog();
        jPanel11 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        ExportTextPanel = new javax.swing.JTextPane();
        jPanel12 = new javax.swing.JPanel();
        CloseButton = new javax.swing.JButton();
        FileChooserDialog = new javax.swing.JDialog();
        ImportFileChooser = new javax.swing.JFileChooser();
        jPanel2 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        EditButton = new javax.swing.JButton();
        DeleteButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        IDField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        NameField = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        DescriptionTextArea = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        CategoryField = new javax.swing.JTextField();
        CompletedCheck = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        AddButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TaskTable = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        SearchCombo = new javax.swing.JComboBox<>();
        SearchField = new javax.swing.JTextField();
        SearchButton = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        CategoryCombo = new javax.swing.JComboBox<>();
        ShowCompCheck = new javax.swing.JCheckBox();
        FilterButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();

        NewTaskDialog.setMinimumSize(new java.awt.Dimension(350, 350));
        NewTaskDialog.setPreferredSize(new java.awt.Dimension(320, 350));

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("New Task Details"));

        jLabel8.setText("Name");

        jLabel9.setText("Category");

        jLabel11.setText("Description");

        NewTaskDescriptionTextArea.setColumns(20);
        NewTaskDescriptionTextArea.setRows(5);
        jScrollPane4.setViewportView(NewTaskDescriptionTextArea);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(NewTaskCategoryField)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)
                            .addComponent(jLabel11))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(NewTaskNameField)
                    .addComponent(jScrollPane4))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(NewTaskNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(NewTaskCategoryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel10.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 50, 5));

        CreateButton.setText("Create");
        CreateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CreateButtonActionPerformed(evt);
            }
        });
        jPanel10.add(CreateButton);

        CancelButton.setText("Cancel");
        CancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelButtonActionPerformed(evt);
            }
        });
        jPanel10.add(CancelButton);

        javax.swing.GroupLayout NewTaskDialogLayout = new javax.swing.GroupLayout(NewTaskDialog.getContentPane());
        NewTaskDialog.getContentPane().setLayout(NewTaskDialogLayout);
        NewTaskDialogLayout.setHorizontalGroup(
            NewTaskDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(NewTaskDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(NewTaskDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE))
                .addContainerGap())
        );
        NewTaskDialogLayout.setVerticalGroup(
            NewTaskDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(NewTaskDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        ExportResultDialog.setMinimumSize(new java.awt.Dimension(320, 230));
        ExportResultDialog.setPreferredSize(new java.awt.Dimension(320, 230));

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Export Result"));

        ExportTextPanel.setEditable(false);
        jScrollPane3.setViewportView(ExportTextPanel);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3)
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel12.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 50, 5));

        CloseButton.setText("Close");
        CloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseButtonActionPerformed(evt);
            }
        });
        jPanel12.add(CloseButton);

        javax.swing.GroupLayout ExportResultDialogLayout = new javax.swing.GroupLayout(ExportResultDialog.getContentPane());
        ExportResultDialog.getContentPane().setLayout(ExportResultDialogLayout);
        ExportResultDialogLayout.setHorizontalGroup(
            ExportResultDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ExportResultDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ExportResultDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE))
                .addContainerGap())
        );
        ExportResultDialogLayout.setVerticalGroup(
            ExportResultDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ExportResultDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        FileChooserDialog.setMinimumSize(new java.awt.Dimension(550, 447));

        ImportFileChooser.setAcceptAllFileFilterUsed(false);
        ImportFileChooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        ImportFileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        ImportFileChooser.setDialogTitle("");
        ImportFileChooser.setFileFilter(new MyCustomFilter());
        ImportFileChooser.setPreferredSize(new java.awt.Dimension(601, 326));
        ImportFileChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ImportFileChooserActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout FileChooserDialogLayout = new javax.swing.GroupLayout(FileChooserDialog.getContentPane());
        FileChooserDialog.getContentPane().setLayout(FileChooserDialogLayout);
        FileChooserDialogLayout.setHorizontalGroup(
            FileChooserDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FileChooserDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ImportFileChooser, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
                .addContainerGap())
        );
        FileChooserDialogLayout.setVerticalGroup(
            FileChooserDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, FileChooserDialogLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(ImportFileChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 341, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(625, 690));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Task Details"));

        jPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 20));

        EditButton.setText("Edit");
        EditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EditButtonActionPerformed(evt);
            }
        });
        jPanel7.add(EditButton);

        DeleteButton.setText("Delete");
        DeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteButtonActionPerformed(evt);
            }
        });
        jPanel7.add(DeleteButton);

        jLabel4.setText("ID");

        IDField.setEditable(false);

        jLabel5.setText("Name");

        jLabel6.setText("Category");

        NameField.setEditable(false);

        DescriptionTextArea.setEditable(false);
        DescriptionTextArea.setColumns(20);
        DescriptionTextArea.setRows(5);
        DescriptionTextArea.setEnabled(false);
        jScrollPane2.setViewportView(DescriptionTextArea);

        jLabel7.setText("Description");

        CategoryField.setEditable(false);

        CompletedCheck.setText("Completed");
        CompletedCheck.setEnabled(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(CategoryField))
                        .addGap(18, 18, 18))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(NameField, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(IDField))
                        .addGap(18, 18, 18)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(CompletedCheck)
                        .addGap(102, 102, 102))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel4)
                    .addComponent(CompletedCheck))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(IDField)
                        .addGap(12, 12, 12)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(NameField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(CategoryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2))
                .addGap(2, 2, 2)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel1.setText("Java Task Manager");
        jLabel1.setMinimumSize(new java.awt.Dimension(417, 32));
        jLabel1.setPreferredSize(new java.awt.Dimension(417, 32));
        jPanel1.add(jLabel1);

        AddButton.setText("Add New Task");
        AddButton.setIconTextGap(0);
        AddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddButtonActionPerformed(evt);
            }
        });
        jPanel1.add(AddButton);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Task List"));

        TaskTable.setAutoCreateRowSorter(true);
        TaskTable.setModel(this.tableModel);
        TaskTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        TaskTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        TaskTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TaskTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(TaskTable);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel2.setText("Search");
        jPanel4.add(jLabel2);

        SearchCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Name", "ID" }));
        SearchCombo.setPreferredSize(new java.awt.Dimension(100, 26));
        jPanel4.add(SearchCombo);

        SearchField.setPreferredSize(new java.awt.Dimension(200, 26));
        jPanel4.add(SearchField);

        SearchButton.setText("Search");
        SearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SearchButtonActionPerformed(evt);
            }
        });
        jPanel4.add(SearchButton);

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLabel3.setText("Filter By Category:");
        jPanel6.add(jLabel3);

        CategoryCombo.setPreferredSize(new java.awt.Dimension(120, 26));
        jPanel6.add(CategoryCombo);

        ShowCompCheck.setSelected(true);
        ShowCompCheck.setText("Show Completed");
        jPanel6.add(ShowCompCheck);

        FilterButton.setText("Filter");
        FilterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FilterButtonActionPerformed(evt);
            }
        });
        jPanel6.add(FilterButton);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                .addContainerGap())
        );

        jMenu1.setText("File");

        jMenu2.setText("Export...");

        jMenuItem2.setText(".txt");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        jMenuItem1.setText(".csv");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        jMenu1.add(jMenu2);

        jMenu3.setText("Import...");

        jMenuItem3.setText(".txt");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem3);

        jMenu1.add(jMenu3);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 609, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void AddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddButtonActionPerformed
        NewTaskDialog.setLocationRelativeTo(this);
        NewTaskDialog.setVisible(true);
    }//GEN-LAST:event_AddButtonActionPerformed

    private void CancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelButtonActionPerformed
        // TODO add your handling code here:
        NewTaskDialog.dispose();
    }//GEN-LAST:event_CancelButtonActionPerformed

    private void TaskTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TaskTableMouseClicked
        // TODO add your handling code here:
        gui.populateTaskDetails();
    }//GEN-LAST:event_TaskTableMouseClicked

    private void EditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EditButtonActionPerformed
        // TODO add your handling code here:
        gui.handleTaskEdit();
    }//GEN-LAST:event_EditButtonActionPerformed

    private void DeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteButtonActionPerformed
        // TODO add your handling code here:
        gui.handleTaskDelete();
    }//GEN-LAST:event_DeleteButtonActionPerformed

    private void CreateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreateButtonActionPerformed
        // TODO add your handling code here:
        gui.handleTaskCreate();
    }//GEN-LAST:event_CreateButtonActionPerformed

    private void FilterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FilterButtonActionPerformed
        // TODO add your handling code here:
        gui.refreshTaskTable();
    }//GEN-LAST:event_FilterButtonActionPerformed

    private void SearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchButtonActionPerformed
        // TODO add your handling code here:
        gui.refreshTaskTable();
    }//GEN-LAST:event_SearchButtonActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // TODO add your handling code here:
        String result = taskManager.saveTasksToFile("tasks.txt");

        ExportResultDialog.setLocationRelativeTo(this);
        ExportResultDialog.setVisible(true);
        ExportTextPanel.setText(result);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void CloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseButtonActionPerformed
        // TODO add your handling code here:
        ExportResultDialog.dispose();
    }//GEN-LAST:event_CloseButtonActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        // TODO add your handling code here:
        FileChooserDialog.setLocationRelativeTo(this);
        FileChooserDialog.setVisible(true);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void ImportFileChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ImportFileChooserActionPerformed
        // TODO add your handling code here:
        if (evt.getActionCommand().equals(javax.swing.JFileChooser.APPROVE_SELECTION)) {
            File file = ImportFileChooser.getSelectedFile();
            String result = taskManager.importTasksFromFile(file);
            FileChooserDialog.dispose();
            ExportResultDialog.setLocationRelativeTo(this);
            ExportResultDialog.setVisible(true);
            ExportTextPanel.setText(result);
            gui.refreshTaskTable();

        } else if (evt.getActionCommand().equals(javax.swing.JFileChooser.CANCEL_SELECTION)) {
            FileChooserDialog.dispose();
        }
    }//GEN-LAST:event_ImportFileChooserActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
        String result = taskManager.saveTasksToFile("tasks.csv");

        ExportResultDialog.setLocationRelativeTo(this);
        ExportResultDialog.setVisible(true);
        ExportTextPanel.setText(result);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TaskManagerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TaskManagerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TaskManagerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TaskManagerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddButton;
    private javax.swing.JButton CancelButton;
    private javax.swing.JComboBox<String> CategoryCombo;
    private javax.swing.JTextField CategoryField;
    private javax.swing.JButton CloseButton;
    private javax.swing.JCheckBox CompletedCheck;
    private javax.swing.JButton CreateButton;
    private javax.swing.JButton DeleteButton;
    private javax.swing.JTextArea DescriptionTextArea;
    private javax.swing.JButton EditButton;
    private javax.swing.JDialog ExportResultDialog;
    private javax.swing.JTextPane ExportTextPanel;
    private javax.swing.JDialog FileChooserDialog;
    private javax.swing.JButton FilterButton;
    private javax.swing.JTextField IDField;
    private javax.swing.JFileChooser ImportFileChooser;
    private javax.swing.JTextField NameField;
    private javax.swing.JTextField NewTaskCategoryField;
    private javax.swing.JTextArea NewTaskDescriptionTextArea;
    private javax.swing.JDialog NewTaskDialog;
    private javax.swing.JTextField NewTaskNameField;
    private javax.swing.JButton SearchButton;
    private javax.swing.JComboBox<String> SearchCombo;
    private javax.swing.JTextField SearchField;
    private javax.swing.JCheckBox ShowCompCheck;
    private javax.swing.JTable TaskTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    // End of variables declaration//GEN-END:variables

}

class MyCustomFilter extends javax.swing.filechooser.FileFilter {

    @Override
    public boolean accept(File file) {
        // Allow only directories, or files with ".txt" extension
        return file.isDirectory() || file.getAbsolutePath().endsWith(".txt");
    }

    @Override
    public String getDescription() {
        // This description will be displayed in the dialog,
        // hard-coded = ugly, should be done via I18N
        return "Text documents (*.txt)";
    }
}
