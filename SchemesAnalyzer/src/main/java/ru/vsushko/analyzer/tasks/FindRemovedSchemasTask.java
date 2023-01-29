/**
 * Copyright (C) SchemesAnalyzer.2014
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Vasiliy Sushko (vasiliy.sushko@gmail.com)
 */
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

    private final File[] schemas;
    private final String pathToOldSchemas;
    private final String pathToNewSchemas;
    private final TextArea textArea;

    public FindRemovedSchemasTask(File[] schemas, String pathToOldSchemas, String pathToNewSchemas, TextArea textArea) {
        this.schemas = schemas;
        this.pathToOldSchemas = pathToOldSchemas;
        this.pathToNewSchemas = pathToNewSchemas;
        this.textArea = textArea;
    }

    @Override
    protected Void call() {
        for (int i = 0; i < schemas.length; i++) {
            File schemaFile = schemas[i];
            final String commonSchemaName = schemaFile.getName();

            final String pathToRecentSchema = pathToNewSchemas + "/" + commonSchemaName;
            final String pathToPreviousSchema = pathToOldSchemas + "/" + commonSchemaName;

            final XmlSchema newAfSchema = SchemaHelper.getSchemaFromPath(pathToRecentSchema);

            final int finalI = i;
            Platform.runLater(() -> {
                if (newAfSchema == null) {
                    setTextAreaText("This schema was deleted: " + commonSchemaName + "\n");
                    setTextAreaText("XSD-schema description: " + SchemaHelper.getSchemaDescription(pathToPreviousSchema));
                    setTextAreaText(LINE_SEPARATOR);
                }
                updateProgress(finalI, schemas.length);
            });
        }
        return null;
    }

    public void setTextAreaText(String text) {
        textArea.appendText(text);
    }
}
