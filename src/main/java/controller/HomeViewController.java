package controller;

import com.jfoenix.controls.JFXTextField;
import db.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import model.Task;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class HomeViewController implements Initializable {
    public JFXTextField taskText;
    public ScrollPane taskPane;
    public ScrollPane myDayTasksPane;
    public DatePicker searchTaskDatePicker;
    public JFXTextField searchTaskField;
    private VBox taskContainer;
    private VBox myDayTasksContainer;
    private ArrayList<Task> todoList = new ArrayList<>();
    private ArrayList<Task> myDayList = new ArrayList<>();

    // ------------------ PROJECT INITIALIZING METHOD ------------------

    // First method to run when loading
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        taskContainer = new VBox(10);
        taskContainer.setPadding(new Insets(10));
        taskPane.setContent(taskContainer);

        myDayTasksContainer = new VBox(10);
        myDayTasksContainer.setPadding(new Insets(10));
        myDayTasksPane.setContent(myDayTasksContainer);

        loadAll();
    }

    // ------------------ BUTTON ACTION LISTENERS ------------------

    // Add Button Click
    public void btnAddOnAction(ActionEvent actionEvent) {
        Task task = new Task(taskText.getText());
        if (setDB(task)) {
            todoList.add(task);
            updateToDoList();
            taskText.setText("");
        }
        taskText.setText("");
        loadAll();
    }

    // Move from to-do to my day
    public void moveToMyDayOnAction(ActionEvent actionEvent) {
        for (int i = todoList.size() - 1; i >= 0; i--) {
            Task task = todoList.get(i);
            if (task.isDone()) {
                myDayList.add(task);
                todoList.remove(i);
                updateTaskInDB(task);
            }
        }
        loadAll();
    }

    // Move from my day to to-do
    public void moveToToDoOnAction(ActionEvent actionEvent) {
        for (int i = myDayList.size() - 1; i >= 0; i--) {
            Task task = myDayList.get(i);
            if (!task.isDone()) {
                todoList.add(task);
                myDayList.remove(i);
                updateTaskInDB(task);
            }
        }
        loadAll();
    }

    // ------------------ ADD DATA TO THE DB ------------------

    // Add new tasks to the DB
    public boolean setDB(Task task) {
        try {
            Connection connection = DBConnection.getInstance().getConnection();

            int nextId = getNextTaskId(connection);
            task.setTaskId(nextId);

            String SQL = "INSERT INTO task VALUES(?,?,?,?)";
            PreparedStatement psTm = connection.prepareStatement(SQL);

            psTm.setInt(1, task.getTaskId());
            psTm.setString(2, task.getTaskText());
            psTm.setBoolean(3, task.isDone());
            psTm.setTimestamp(4, task.getCreationTime());

            if (psTm.executeUpdate() > 0) {
                return true;
            } else {
                new Alert(Alert.AlertType.INFORMATION, "Error").show();
                return false;
            }

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to add task: " + e.getMessage()).show();
        }
        return false;
    }

    // Get the task id for the next task
    public int getNextTaskId(Connection connection) throws SQLException {
        String SQL = "SELECT MAX(task_id) FROM task";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            } else {
                return 1;
            }
        }
    }

    // ------------------ GET DATA FROM DB ------------------

    //Fetch data from db based on isDone
    public void getTasksFromDBOnIsDone(boolean isDone) {
        try {
            String SQL = "SELECT * FROM task WHERE is_done = ?";
            Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement psTm = connection.prepareStatement(SQL);
            psTm.setBoolean(1, isDone);
            ResultSet resultSet = psTm.executeQuery();
            while (resultSet.next()) {
                Task task = new Task(
                        resultSet.getInt("task_id"),
                        resultSet.getString("task_text"),
                        resultSet.getBoolean("is_done"),
                        resultSet.getTimestamp("creation_time")
                );
                if (isDone) {
                    myDayList.add(task);
                } else {
                    todoList.add(task);
                }
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to fetch tasks: " + e.getMessage()).show();
        }
    }

    // Load all data
    public void loadAll() {
        try {
            String SQL = "SELECT * FROM task";
            Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement psTm = connection.prepareStatement(SQL);
            ResultSet resultSet = psTm.executeQuery();
            while (resultSet.next()) {
                Task task = new Task(
                        resultSet.getInt("task_id"),
                        resultSet.getString("task_text"),
                        resultSet.getBoolean("is_done"),
                        resultSet.getTimestamp("creation_time")
                );
                if (task.isDone()) {
                    myDayList.add(task);
                } else {
                    todoList.add(task);
                }
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load tasks: " + e.getMessage()).show();
        }
        updateToDoList();
        updateMyDayList();
    }

    // ------------------ SEARCH FROM DB ------------------

    public void searchOnAction(ActionEvent actionEvent) {
        String searchTerm = searchTaskField.getText().trim().toLowerCase();

        if (searchTerm.isEmpty()) {
            loadAll();
            return;
        }

        todoList.clear();
        myDayList.clear();
        taskContainer.getChildren().clear();
        myDayTasksContainer.getChildren().clear();

        try {
            String SQL = "SELECT * FROM task WHERE LOWER(task_text) LIKE ?";
            Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement psTm = connection.prepareStatement(SQL);
            psTm.setString(1, "%" + searchTerm + "%");
            ResultSet resultSet = psTm.executeQuery();
            while (resultSet.next()) {
                Task task = new Task(
                        resultSet.getInt("task_id"),
                        resultSet.getString("task_text"),
                        resultSet.getBoolean("is_done"),
                        resultSet.getTimestamp("creation_time")
                );
                if (task.isDone()) {
                    myDayList.add(task);
                } else {
                    todoList.add(task);
                }
            }

            updateToDoList();
            updateMyDayList();

            if (todoList.isEmpty() && myDayList.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, "No tasks found").show();
            }

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to search tasks: " + e.getMessage()).show();
        }
    }

    // ------------------ UPDATE DATA TO THE DB ------------------

    // Update a task when a move button is clicked
    public void updateTaskInDB(Task task) {
        try {
            String SQL = "UPDATE task SET is_done = ? WHERE task_id = ?";
            Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement psTm = connection.prepareStatement(SQL);
            psTm.setBoolean(1, task.isDone());
            psTm.setInt(2, task.getTaskId());
            psTm.executeUpdate();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to update task: " + e.getMessage()).show();
        }
    }

    // ------------------ DELETE FROM DB ------------------

    public void deleteOnAction(ActionEvent actionEvent) {
        try {
            String SQL = "DELETE FROM task WHERE is_done = TRUE";
            Connection connection = DBConnection.getInstance().getConnection();
            PreparedStatement psTm = connection.prepareStatement(SQL);
            int rowsAffected = psTm.executeUpdate();

            if (rowsAffected > 0) {
                myDayList.clear();
                updateMyDayList();
                new Alert(Alert.AlertType.INFORMATION, "Successfully deleted completed tasks!").show();
            } else {
                new Alert(Alert.AlertType.INFORMATION, "No completed tasks to delete!").show();
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to delete tasks: " + e.getMessage()).show();
        }
    }

    // ------------------ UPDATE ARRAYLIST FROM DB ------------------

    //Update to-do column by arraylist
    public void updateToDoList() {
        //clear existing data
        todoList.clear();
        taskContainer.getChildren().clear();
        //fetch data from db to arraylist by isDone condition
        getTasksFromDBOnIsDone(false);
        for (Task task : todoList) {
            taskContainer.getChildren().add(task.getTaskCard());
        }
    }

    //Update my day column by arraylist
    public void updateMyDayList() {
        //clear existing data
        myDayList.clear();
        myDayTasksContainer.getChildren().clear();
        //fetch data from db to arraylist by isDone condition
        getTasksFromDBOnIsDone(true);
        for (Task task : myDayList) {
            myDayTasksContainer.getChildren().add(task.getTaskCard());
        }
    }

    public void loadOnAction(ActionEvent actionEvent) {
        loadAll();
    }
}