package ru.vsushko.analyzer.schema;

import java.util.List;

/**
 * Created by vsa
 * Date: 11.11.14.
 */
public class InfosHolder implements InfoProvider {
    private List schemasDefinition;

    @Override
    public String getSchemaName(int indx) {
        return schemasDefinition.get(indx).toString();
    }
}
