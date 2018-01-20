package ru.vsushko.analyzer.tasks;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import org.apache.ws.commons.schema.XmlSchema;
import ru.vsushko.analyzer.schema.SchemaHelper;

import java.io.File;

/**
 * Created by vsa
 * Date: 14.01.15.
 */
public class FindRemovedSchemasTask extends Task<Void> {
    private static final String LINE_SEPARATOR = "\n==================================================================================\n";

    private File[] schemas;
    private String pathToOldSchemas;
    private String pathToNewSchemas;
    private TextArea textArea;

    public FindRemovedSchemasTask(File[] schemas, String pathToOldSchemas, String pathToNewSchemas, TextArea textArea) {
        this.schemas = schemas;
        this.pathToOldSchemas = pathToOldSchemas;
        this.pathToNewSchemas = pathToNewSchemas;
        this.textArea = textArea;
    }

    @Override
    protected Void call() throws Exception {
        for (int i = 0; i < schemas.length; i++) {
            File schemaFile = schemas[i];
            final String commonSchemaName = schemaFile.getName();

            final String pathToRecentSchema = pathToNewSchemas + "\\" + commonSchemaName;
            final String pathToPreviousSchema = pathToOldSchemas + "\\" + commonSchemaName;

            final XmlSchema newAfSchema = SchemaHelper.getSchemaFromPath(pathToRecentSchema);

            final int finalI = i;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (newAfSchema == null) {
                        setTextAreaText("Удалена схема: " + commonSchemaName + "\n");
                        setTextAreaText("Описание наименования XSD-схемы: " + SchemaHelper.getSchemaDescription(pathToPreviousSchema));
                        setTextAreaText(LINE_SEPARATOR);
                    }
                    updateProgress(finalI, schemas.length);
                }
            });
        }
        return null;
    }

    public void setTextAreaText(String text) {
        textArea.appendText(text);
    }
}
