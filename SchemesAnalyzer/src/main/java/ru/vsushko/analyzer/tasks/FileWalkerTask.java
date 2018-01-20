package ru.vsushko.analyzer.tasks;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;
import org.apache.ws.commons.schema.XmlSchema;
import ru.vsushko.analyzer.schema.SchemaHelper;
import ru.vsushko.analyzer.schema.DifferenceResolver;
import ru.vsushko.analyzer.schema.DifferenceResolverImpl;
import ru.vsushko.analyzer.schema.SchemaInfo;
import ru.vsushko.analyzer.schema.tree.TreeBuilder;
import ru.vsushko.analyzer.schema.tree.TreeBuilderImpl;
import ru.vsushko.analyzer.schema.tree.TreeNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vsa
 * Date: 13.01.15.
 */
public class FileWalkerTask extends Task<Void> {
    private static final String LINE_SEPARATOR = "\n==================================================================================\n";

    private File[] oldSchemaFiles;
    private String oldSchemesPath;
    private String newSchemesPath;
    private TextArea textArea;

    public FileWalkerTask(File[] oldSchemaFile, String oldSchemesPath, String newSchemesPath, TextArea textArea) {
        this.oldSchemaFiles = oldSchemaFile;
        this.oldSchemesPath = oldSchemesPath;
        this.newSchemesPath = newSchemesPath;
        this.textArea = textArea;
    }

    public String getSchemaAnnotationDocumentation(String pathToSchema) {
        return SchemaHelper.getSchemaDescription(pathToSchema);
    }

    public String identifySchemaVersionInfo(String schemaTargetNamespace) {
        return SchemaHelper.readSchemaVersionInfo(schemaTargetNamespace);
    }

    @Override
    public Void call() throws Exception {
        for (int i = 0; i < oldSchemaFiles.length; i++) {
            File schemaFile = oldSchemaFiles[i];
            String commonSchemaName = schemaFile.getName();

            String pathToPreviousSchemas = oldSchemesPath + "\\" + commonSchemaName;
            String pathToRecentSchemas = newSchemesPath + "\\"  + commonSchemaName;

            XmlSchema oldAfSchema = SchemaHelper.getSchemaFromPath(pathToPreviousSchemas);
            XmlSchema newAfSchema = SchemaHelper.getSchemaFromPath(pathToRecentSchemas);

            if (oldAfSchema != null && newAfSchema != null) {
                // основная информация о схеме
                final SchemaInfo schemaInfo = new SchemaInfo();
                schemaInfo.setSchemaName(schemaFile.getName());
                schemaInfo.setSchemaDescription(getSchemaAnnotationDocumentation(pathToRecentSchemas));
                schemaInfo.setSchemaVersion(identifySchemaVersionInfo(newAfSchema.getTargetNamespace()));

                // построим деревья схем
                TreeBuilder treeBuilder = new TreeBuilderImpl();
                TreeNode<Object> actualSchemaTree = treeBuilder.buildSchemaTree(oldAfSchema, pathToPreviousSchemas);
                TreeNode<Object> schemaToCompareTree = treeBuilder.buildSchemaTree(newAfSchema, pathToRecentSchemas);

                // получим список изменений
                DifferenceResolver resolver = new DifferenceResolverImpl();
                resolver.findAllDifference(actualSchemaTree, schemaToCompareTree);
                final List<String> diffs = new ArrayList<String>();
                diffs.addAll(resolver.getDifferences());

                final int finalI = i;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                         // добавляем в список на вывод
                         if (diffs.size() != 0) {
                             setTextAreaText(schemaInfo.getSchemaInfo());
                             for (String diff : diffs) {
                                 setTextAreaText(diff);
                             }
                             setTextAreaText(LINE_SEPARATOR);
                         }
                         diffs.clear();
                        updateProgress(finalI, oldSchemaFiles.length);
                     }
                 });
            }
        }
        return null;
    }

    public void setTextAreaText(final List<String> textAreaText) {
        if (textAreaText != null) {
            for (final String s : textAreaText) {
                try {
                    textArea.appendText(new String(s.getBytes("ISO-8859-1"), "UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setTextAreaText(String text) {
        if (text != null) {
            try {
                textArea.appendText(new String(text.getBytes("ISO-8859-1"), "UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
