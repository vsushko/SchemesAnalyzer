package ru.vsushko.analyzer.schema.simpletype;

import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaWhiteSpaceFacet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vsa
 * Date: 23.12.14.
 */
public class SimpleTypeResctrictionFacetImpl<T> implements SimpleTypeResctrictionFacet<T>{

    @Override
    public List<String> getDifferenceBetweenConcreteFacet(List<XmlSchemaFacet> oldFacets, List<XmlSchemaFacet> newFacets, T value, String label) {
        List<String> diffs = new ArrayList<String>();

        T oldFacet = null;
        for (XmlSchemaFacet facet : oldFacets) {
            if (facet instanceof XmlSchemaWhiteSpaceFacet) {
                oldFacet = (T) facet;
            }
        }

        T newFacet = null;
        for (XmlSchemaFacet facet : newFacets) {
            if (facet instanceof XmlSchemaWhiteSpaceFacet) {
                newFacet = (T) facet;
            }
        }

        // добавлено новое ограничение
        if (oldFacet == null && newFacet != null) {
//            diffs.add("Добавлено новое ограничение " + label + " со значением: " + newFacet.getValue());
        }

        // удалено старое органичение
        if (newFacet == null && oldFacet != null) {
//            diffs.add("Удалено ранее объявленное ограничение " + label + " со значением: " + oldFacet.getValue());
        }

        // изменилось значение
//        if (oldFacet != null && newFacet != null
//                && !oldFacet.getValue().equals(newFacet.getValue())) {
//            diffs.add("Изменено значение " + label + " с " + oldFacet.getValue() + " на " + newFacet.getValue());
//        }
        return diffs;
        /*
        XmlGenerator<Integer> intXmlGenerator = new XmlGeneratorImpl<Integer>(Integer.class);
        XmlGenerator<String> stringXmlGenerator = new XmlGeneratorImpl<String>(String.class);

        System.out.println("integer: " + intXmlGenerator.getXml(x));
        System.out.println("string : " + stringXmlGenerator.getXml(y));
        */

    }
}
