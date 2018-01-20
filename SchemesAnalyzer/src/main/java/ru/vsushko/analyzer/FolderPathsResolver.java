package ru.vsushko.analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vsa
 * Date: 19.11.14.
 */
public class FolderPathsResolver {
    public Object[] getPaths(String rootSchemaFolderPath) {
        File file = new File(rootSchemaFolderPath);

        return transformFullPath(file.list(), rootSchemaFolderPath);
    }

    private Object[] transformFullPath(String[] paths, String rootSchemaFolderPath) {
        List<String> folders = new ArrayList<String>();
        for (String path : paths) {
            StringBuilder builder = new StringBuilder();
            folders.add(String.valueOf(builder.append(rootSchemaFolderPath).append("\\").append(path).append("\\")));
        }
        return folders.toArray();
    }
}
