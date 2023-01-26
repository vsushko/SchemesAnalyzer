package ru.vsushko.analyzer.schema;

import ru.vsushko.analyzer.schema.tree.TreeHelper;
import ru.vsushko.analyzer.schema.tree.TreeNode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vsa
 * Date: 21.12.14.
 */
public class DifferenceResolverImpl implements DifferenceResolver {

    private final List<String> differences;

    public DifferenceResolverImpl() {
        differences = new ArrayList<>();
    }

    /**
     * Schema description difference check.
     */
    public void checkDifferenceBetweenSchemaDescription(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree) {
        String oldSchemaDescription = TreeHelper.getSchemaDescription(actualSchemaTree);
        String newSchemaDescription = TreeHelper.getSchemaDescription(schemaToCompareTree);

        if (!oldSchemaDescription.equals(newSchemaDescription)) {
            differences.add(encodeString("Changed schema description: " + newSchemaDescription));
        }
    }

    /**
     * Schema imports changes check.
     */
    public void checkDifferenceBetweenSchemaImports(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree) {
        List<String> addedImports = SchemaHelper.getAddedImports(actualSchemaTree, schemaToCompareTree);
        List<String> removedImports = SchemaHelper.getRemovedImports(actualSchemaTree, schemaToCompareTree);

        if (addedImports.size() != 0) {
            for (String importString : addedImports) {
                differences.add(encodeString("Added document dependency: " + getSchemaImportName(importString) + "\n"));
            }
        }

        if (removedImports.size() != 0) {
            for (String importString : removedImports) {
                differences.add(encodeString("Removed document dependency: " + getSchemaImportName(importString) + "\n"));
            }
        }
    }

    /**
     * Extracts dependency from schema import and creates string "schema name + file format".
     */
    private String getSchemaImportName(String importString) {
        String[] bits = importString.split(":");
        // schema name position are always fixed
        return bits[bits.length - 2] + ".xsd";
    }

    /**
     * Schema SimpleType elements changes check.
     */
    public void checkDifferenceBetweenSimpleTypes(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree) {
        // find all SimpleType names
        List<String> oldAfSimpleTypeNames = TreeHelper.getSimpleTypesNamesFromTree(actualSchemaTree);
        List<String> newAfSimpleTypeNames = TreeHelper.getSimpleTypesNamesFromTree(schemaToCompareTree);

        // check for new SimpleType types existence
        List<String> newSimpleTypes = SchemaHelper.getNewSimpleTypes(oldAfSimpleTypeNames, newAfSimpleTypeNames);

        if (newSimpleTypes.size() != 0) {
            for (String element : newSimpleTypes) {
                differences.add(encodeString("Added new local type element: " + element + "\n"));
            }
        }

        // old SimpleTypes deletions check
        List<String> preExistingSimpleTypes = SchemaHelper.getPreExistingSimpleTypes(oldAfSimpleTypeNames, newAfSimpleTypeNames);

        if (preExistingSimpleTypes.size() != 0) {
            for (String element : preExistingSimpleTypes) {
                differences.add(encodeString("Removed local type element: " + element + "\n"));
            }
        }

        // retrieves SimpleTypes list with the same names
        List<String> sameSimpleTypeNames = SchemaHelper.getSameTypeNames(oldAfSimpleTypeNames, newAfSimpleTypeNames);

        // the list may be empty because there are no SimpleType types in the schema
        if (sameSimpleTypeNames.size() != 0) {
            // check for changes in SimpleType
            List<String> simpleTypeChanges = SchemaHelper.getDifferenceBetweenSimpleTypes(TreeHelper.getSimpleTypesFromSchemaBySpecificNames(actualSchemaTree, sameSimpleTypeNames), TreeHelper.getSimpleTypesFromSchemaBySpecificNames(schemaToCompareTree, sameSimpleTypeNames));

            if (simpleTypeChanges.size() != 0) {
                for (String diffString : simpleTypeChanges) {
                    differences.add(encodeString(diffString));
                }
            }
        }
    }

    /**
     * Check for new ComplexType types existence.
     */
    public void checkDifferenceBetweenComplexTypes(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree) {
        // find all ComplexType names
        List<String> oldAfComplexTypeNames = TreeHelper.getComplexTypesNamesFromTree(actualSchemaTree);
        List<String> newAfComplexTypeNames = TreeHelper.getComplexTypesNamesFromTree(schemaToCompareTree);

        // there is no ComplexType elements in schema
        if (oldAfComplexTypeNames.size() != 0 && newAfComplexTypeNames.size() != 0) {
            //  retrieves new ComplexType types
            List<String> newComplexTypes = SchemaHelper.getNewComplexTypes(oldAfComplexTypeNames, newAfComplexTypeNames);

            if (newComplexTypes.size() != 0) {
                for (String element : newComplexTypes) {
                    differences.add(encodeString("Added new ComplexType: " + element + "\n"));
                }
            }

            // check for ComplexType deletion which were specified earlier
            List<String> preExistingComplexTypes = SchemaHelper.getPreExistingComplexTypes(oldAfComplexTypeNames, newAfComplexTypeNames);

            if (preExistingComplexTypes.size() != 0) {
                for (String element : preExistingComplexTypes) {
                    differences.add(encodeString("ComplexType was removed: " + element + "\n"));
                }
            }

            // retrieve ComplexTypes list with the same names
            List<String> sameComplexTypeNames = SchemaHelper.getSameTypeNames(oldAfComplexTypeNames, newAfComplexTypeNames);

            // the list may be empty because there are no ComplexType types in the schema
            if (sameComplexTypeNames.size() != 0) {
                // ComplexType differences checks
                List<String> complexTypeChanges = SchemaHelper.getDifferenceBetweenComplexTypes(TreeHelper.getComplexTypesFromSchemaBySpecificNames(actualSchemaTree, sameComplexTypeNames), TreeHelper.getComplexTypesFromSchemaBySpecificNames(schemaToCompareTree, sameComplexTypeNames));

                if (complexTypeChanges.size() != 0) {
                    for (String diffString : complexTypeChanges) {
                        differences.add(encodeString(diffString));
                    }
                }
            }
        }
    }

    /**
     * All schemas checks.
     */
    public void findAllDifference(TreeNode<Object> actualSchemaTree, TreeNode<Object> schemaToCompareTree) {
        checkDifferenceBetweenSchemaDescription(actualSchemaTree, schemaToCompareTree);
        checkDifferenceBetweenSchemaImports(actualSchemaTree, schemaToCompareTree);
        checkDifferenceBetweenSimpleTypes(actualSchemaTree, schemaToCompareTree);
        checkDifferenceBetweenComplexTypes(actualSchemaTree, schemaToCompareTree);
    }

    /**
     * Returns list of changes which were identified during schemas difference comparison.
     */
    @Override
    public List<String> getDifferences() {
        return differences;
    }

    /**
     * Encodes s to ISO-8859-1 for JTextArea.
     */
    public String encodeString(String s) {
        String result;
        result = new String(s.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        return result;
    }
}
