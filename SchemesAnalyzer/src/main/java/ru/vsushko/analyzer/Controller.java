package ru.vsushko.analyzer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import ru.vsushko.analyzer.schema.XsdAnalyzer;
import ru.vsushko.analyzer.tasks.FileWalkerTask;
import ru.vsushko.analyzer.tasks.FindNewSchemasTask;
import ru.vsushko.analyzer.tasks.FindRemovedSchemasTask;

import javax.swing.*;
import java.io.File;

public class Controller {
    @FXML
    private Button openOldSchemasButton;
    @FXML
    private Button openNewSchemasButton;
    @FXML
    private Button analyzeButton;
    @FXML
    private Button clearButton;
    @FXML
    private TextField textFieldToOldSchemas;
    @FXML
    private TextField textFieldToNewSchemas;
    @FXML
    private Button exitButton;
    @FXML
    private TextArea textArea;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ProgressBar progressBar;

    private String pathToOldSchemas;
    private String pathToNewSchemas;
    private String oldTextFieldText;
    private String newTextFieldText;

    public Controller() {

        textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setPrefColumnCount(3);
        textArea.setPrefRowCount(3);

        scrollPane = new ScrollPane();
        scrollPane.setContent(textArea);

        textFieldToOldSchemas = new TextField();
    }

    @FXML
    public void openPreviousSchemasPathFileChooser() {
        openOldSchemasButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Указать папку с AF");

            File defaultDirectory = new File(".");
            directoryChooser.setInitialDirectory(defaultDirectory);

            File selectedDirectory = directoryChooser.showDialog(null);

            if (selectedDirectory != null) {
                textFieldToOldSchemas.setText(selectedDirectory.getAbsolutePath());
                setOldTextFieldText(selectedDirectory.getAbsolutePath());
            }
            setPathToOldSchemas(getOldTextFieldText());
            setOldTextFieldText(null);
        });
    }

    @FXML
    public void openRecentSchemasPathFileChooser() {
        openNewSchemasButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Указать папку с AF");

            File defaultDirectory = getPathToOldSchemas() != null && !getPathToOldSchemas().isEmpty() ?
                    new File(getPathToOldSchemas()) : new File(".");
            directoryChooser.setInitialDirectory(defaultDirectory);

            File selectedDirectory = directoryChooser.showDialog(null);

            if (selectedDirectory != null) {
                textFieldToNewSchemas.setText(selectedDirectory.getAbsolutePath());
                setNewTextFieldText(selectedDirectory.getAbsolutePath());
            }
            setPathToNewSchemas(getNewTextFieldText());
            setNewTextFieldText(null);
        });
    }

    @FXML
    public void startAnalyzeSchemas() {
        // если путь не проинициализирован, но заполнено поле
        if (getPathToOldSchemas() == null && !textFieldToOldSchemas.getText().isEmpty()) {
            setPathToOldSchemas(textFieldToOldSchemas.getText());
        }

        // если путь не проинициализирован, но заполнено поле
        if (getPathToNewSchemas() == null && !textFieldToNewSchemas.getText().isEmpty()) {
            setPathToNewSchemas(textFieldToNewSchemas.getText());
        }

        if (getPathToOldSchemas() != null && !getPathToOldSchemas().isEmpty()
                && getPathToNewSchemas() != null && !getPathToNewSchemas().isEmpty()) {
            XsdAnalyzer xsdAnalyzer = new XsdAnalyzer();
            try {
                File[] oldSchemas = xsdAnalyzer.getSchemaFilesFromFolder(getPathToOldSchemas());
                File[] newSchemas = xsdAnalyzer.getSchemaFilesFromFolder(getPathToNewSchemas());

                if (oldSchemas != null && newSchemas != null) {
                    // сравниваем схемы, находим отличия
                    FileWalkerTask walkerTask = new FileWalkerTask(oldSchemas, getPathToOldSchemas(), getPathToNewSchemas(), textArea);
                    progressBar.progressProperty().bind(walkerTask.progressProperty());
                    new Thread(walkerTask).start();

                    // найдем схемы, которые были добавлены
                    FindNewSchemasTask findRecentSchemasTask = new FindNewSchemasTask(newSchemas, getPathToOldSchemas(), getPathToNewSchemas(), textArea);
                    progressBar.progressProperty().bind(findRecentSchemasTask.progressProperty());
                    new Thread(findRecentSchemasTask).start();

                    // найдем схемы, которые были удалены
                    FindRemovedSchemasTask findRemovedSchemasTask = new FindRemovedSchemasTask(oldSchemas, getPathToOldSchemas(), getPathToNewSchemas(), textArea);
                    progressBar.progressProperty().bind(findRemovedSchemasTask.progressProperty());
                    new Thread(findRemovedSchemasTask).start();
                }
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(null, e1.getLocalizedMessage());
            }
            setPathToOldSchemas(getOldTextFieldText());
            setPathToNewSchemas(getNewTextFieldText());
        } else {
            JOptionPane.showMessageDialog(null, "Не удалось прочитать путь к папкам со схемами");
        }
    }

    @FXML
    public void cleaAllControls() {
        textArea.clear();
    }

    @FXML
    public void exit() {
        Platform.exit();
    }

    public String getPathToOldSchemas() {
        return pathToOldSchemas;
    }

    public void setPathToOldSchemas(String pathToOldSchemas) {
        this.pathToOldSchemas = pathToOldSchemas;
    }

    public String getPathToNewSchemas() {
        return pathToNewSchemas;
    }

    public void setPathToNewSchemas(String pathToNewSchemas) {
        this.pathToNewSchemas = pathToNewSchemas;
    }

    public String getOldTextFieldText() {
        return oldTextFieldText;
    }

    public void setOldTextFieldText(String oldTextFieldText) {
        this.oldTextFieldText = oldTextFieldText;
    }

    public String getNewTextFieldText() {
        return newTextFieldText;
    }

    public void setNewTextFieldText(String newTextFieldText) {
        this.newTextFieldText = newTextFieldText;
    }
}
