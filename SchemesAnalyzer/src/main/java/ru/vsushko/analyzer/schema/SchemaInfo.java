package ru.vsushko.analyzer.schema;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vsa
 * Date: 21.12.14.
 */
public class SchemaInfo {
    private String schemaName;
    private String schemaDescription;
    private String schemaVersion;

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public void setSchemaDescription(String schemaDescription) {
        this.schemaDescription = schemaDescription;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public List<String> getSchemaInfo() {
        List<String> schemaInfo = new ArrayList<>();
        schemaInfo.add(encodeString("Schema name: " + schemaName) + "\n");
        schemaInfo.add(encodeString("XSD-schema description: " + schemaDescription + "\n"));
        schemaInfo.add(encodeString("Version: " + schemaVersion + "\n"));
        return schemaInfo;
    }

    public String encodeString(String s) {
        try {
            return new String(s.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}