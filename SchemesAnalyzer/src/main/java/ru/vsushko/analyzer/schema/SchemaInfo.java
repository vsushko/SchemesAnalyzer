package ru.vsushko.analyzer.schema;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vsa
 * Date: 21.12.14.
 */
public class SchemaInfo {
    private static Logger log = Logger.getLogger(SchemaInfo.class);

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
        List<String> schemaInfo = new ArrayList<String>();
        schemaInfo.add(encodeString("Наименование схемы: " + schemaName) + "\n");
        schemaInfo.add(encodeString("Описание наименования XSD-схемы: " + schemaDescription + "\n"));
        schemaInfo.add(encodeString("Версия схемы: " + schemaVersion + "\n"));
        return schemaInfo;
    }

    public String encodeString(String s) {
        try {
            return new String(s.getBytes("UTF-8"), "ISO-8859-1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}