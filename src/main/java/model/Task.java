package model;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class Task {
    private int taskId;
    private HBox taskCard;
    private Label taskText;
    private boolean isDone;
    private Timestamp creationTime;
    private CheckBox isDoneCheckBox;

    public Task(String text) {
        this.taskId = -1;
        this.creationTime = Timestamp.valueOf(LocalDateTime.now());
        taskCard = new HBox(10);
        taskCard.setPadding(new Insets(10));
        taskCard.setMinWidth(100);
        taskCard.setStyle("-fx-background-color: blue; -fx-border-radius: 5;");

        isDoneCheckBox = new CheckBox();
        isDoneCheckBox.setOnAction(event -> this.isDone = isDoneCheckBox.isSelected());

        this.taskText = new Label(text);
        this.taskText.setTextFill(Color.WHITE);
        this.taskText.setFont(new Font("Comic Sans MS", 16));
        Label dateAndTime = new Label(formatDateTime(this.creationTime));
        dateAndTime.setTextFill(Color.AZURE);
        taskCard.getChildren().addAll(isDoneCheckBox, this.taskText, dateAndTime);
    }

    public Task(int taskId, String taskText, boolean isDone, Timestamp creationTime) {
        this(taskText);
        this.taskId = taskId;
        this.isDone = isDone;
        this.creationTime = creationTime;
        this.isDoneCheckBox.setSelected(isDone);

        // Update the date and time label
        Label dateAndTime = (Label) taskCard.getChildren().get(2);
        dateAndTime.setText(formatDateTime(this.creationTime));
    }

    private String formatDateTime(Timestamp timestamp) {
        LocalDateTime dateTime = timestamp.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    public String getTaskText() {
        return taskText.getText();
    }

    public boolean isDone() {
        return isDone;
    }

    public void setIsDone(boolean isDone) {
        this.isDone = isDone;
        this.isDoneCheckBox.setSelected(isDone);
    }
}