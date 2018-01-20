package ru.vsushko.analyzer.schema;

import java.io.File;

/**
 * Created by vsa
 * Date: 19.11.14.
 */
public class XsdAnalyzer {

    // TODO: рефакторинг

    public File[] getSchemaFilesFromFolder(String pathToSchema) {
        File folderFile = new File(pathToSchema);
        return folderFile.listFiles();
    }

}