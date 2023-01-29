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
        List<String> folders = new ArrayList<>();
        for (String path : paths) {
            StringBuilder builder = new StringBuilder();
            folders.add(String.valueOf(builder.append(rootSchemaFolderPath).append("\\").append(path).append("\\")));
        }
        return folders.toArray();
    }
}
