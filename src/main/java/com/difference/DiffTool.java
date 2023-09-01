package com.difference;

import java.lang.reflect.Field;
import java.util.*;

public class DiffTool<T> {

    private static final String DOT = ".";
    private static final String ID = "id";


    /**
     * Main method for the audit system that is capable of determining the difference
     * between two objects of the same type
     * */
    public List<ChangeType> diff(T previous, T current) throws IllegalAccessException {

        if( previous == null || current== null ){
            throw new AuditException("Provided data is not valid. One of both object is null");
        }

        List<ChangeType> list = new ArrayList<>();
        propertyDifference(previous, current, list, null);
        return list;
    }

    /**
     * Recursive method to iterate over a complex object that has nested objects to find the modified property/items
     */
    private void propertyDifference(T previous, T current, List<ChangeType> changedProperties, String parent) throws IllegalAccessException {
        for (Field field : previous.getClass().getDeclaredFields()) {
            //Identify the parent when is a nested object
            if (parent == null) {
                parent = previous.getClass().getSimpleName();
            }
            field.setAccessible(true);
            Object value1 = field.get(previous);
            Object value2 = field.get(current);

            if (value1 == null && value2 == null) {
                continue;
            }

            if (value1 == null || value2 == null) {
                PropertyUpdate<T> propertyUpdate = new PropertyUpdate<>();
                propertyUpdate.setProperty(parent + DOT + field.getName());
                balancePath(propertyUpdate);
                changedProperties.add(propertyUpdate);
            } else if (isJavaObject(value1)) { // To identify a custom object

                if (value1 instanceof List<?> && value2 instanceof List<?>) {
                    //List items validation
                    listValidation(value1, value2, field, changedProperties);
                } else if (!Objects.equals(value1, value2)) { // Property validation
                    PropertyUpdate<T> propertyUpdate = new PropertyUpdate<>();
                    propertyUpdate.setProperty(parent + DOT + field.getName());
                    propertyUpdate.setPrevious(previous);
                    propertyUpdate.setCurrent(current);

                    balancePath(propertyUpdate);
                    changedProperties.add(propertyUpdate);
                }
            } else {
                propertyDifference((T) value1, (T) value2, changedProperties, parent + DOT + field.getName());
            }
        }
    }

    /**
     * Validates the id, removed/added/modified items in a list and builds the response
     * */
    private void listValidation(Object value1, Object value2, Field field, List<ChangeType> changedProperties) throws IllegalAccessException {
        ListUpdate<T> listUpdate = new ListUpdate<>();
        PropertyUpdate<T> propertyUpdate = new PropertyUpdate<>();

        // Validate if the objects inside the list have id or @AuditKey
        // Only one object is enough for this as both are from the same type
        String idName = validateId(value1);

        //Validate if an object inside a list was added or removed
        removedOrAdded(value1, value2, listUpdate, idName);
        listUpdate.setProperty(field.getName());
        changedProperties.add(listUpdate);

        // Validate if an object inside a list was modified
        modifiedListItem(propertyUpdate, value1, value2, idName, field.getName());
        changedProperties.add(propertyUpdate);
    }

    /**
     * Checks if an object is a java object
     * More restrictions may be added
     * */
    private boolean isJavaObject(Object object) {
        String name = object.getClass().getName();
        return name.startsWith("java.lang") || name.startsWith("java.util"); //Add more names
    }

    /**
     * Erases the leading parent where the updated property is on the root
     */
    private void balancePath(PropertyUpdate<T> propertyUpdate) {
        String[] path = propertyUpdate.getProperty().split("\\.");
        if (path.length <= 2) {
            propertyUpdate.setProperty(path[1]);
        }
    }

    /**
     * Validates if the objects inside the list have 'id' or @AuditKey
     */
    private String validateId(Object value) {
        List<T> res = null;
        for (T innerList : (List<T>) value) {
            res = (List<T>) Arrays.stream(innerList.getClass().getDeclaredFields()).filter(f -> ID.equals(f.getName()) ||
                    f.isAnnotationPresent(AuditKey.class)).toList();
        }

        if (res == null || res.isEmpty()) {
            throw new AuditException("The audit system lacks the information it needs to determine what has changed");
        }
        return ((Field) res.get(0)).getName();
    }

    /**
     * Validates if an object inside a list was modified
     */
    private void modifiedListItem(PropertyUpdate<T> propertyUpdate, Object value1, Object value2, String idName, String fieldName) throws IllegalAccessException {

        Map<T, Object> value1Map = new HashMap<>();
        Map<T, Object> value2Map = new HashMap<>();

        buildValueMap(value1Map, idName, value1);
        buildValueMap(value2Map, idName, value2);

        // To take away the deleted ones
        Map<T, Object> removedMap = new HashMap<>(value1Map);
        for (Map.Entry<T, Object> entry : value2Map.entrySet()) {
            removedMap.entrySet().removeIf(ent -> ent.getKey().equals(entry.getKey()) && ent.getValue().equals(entry.getValue()));
        }

        // To take away the added ones
        Map<T, Object> addedMap = new HashMap<>(value2Map);
        for (Map.Entry<T, Object> entry : value1Map.entrySet()) {
            addedMap.entrySet().removeIf(ent -> ent.getKey().equals(entry.getKey()) && ent.getValue().equals(entry.getValue()));
        }

        //Builds the propertyUpdate
        buildPropertyUpdate(removedMap, addedMap, propertyUpdate, idName, fieldName);
    }

    /**
     * Builds the propertyUpdate object with the changes found in the items in the list
     *
     * */
    private void buildPropertyUpdate(Map<T, Object> removedMap, Map<T, Object> addedMap, PropertyUpdate<T> propertyUpdate,
                                     String idName, String fieldName) throws IllegalAccessException {
        for (Map.Entry<T, Object> entry : removedMap.entrySet()) {
            Object current = addedMap.get(entry.getKey());
            Object previous = entry.getValue();

            //To get the modified object
            if (current != null && !current.equals(previous)) {

                for (Field currentField : current.getClass().getDeclaredFields()) {
                    currentField.setAccessible(true);
                    for (Field previousField : previous.getClass().getDeclaredFields()) {
                        previousField.setAccessible(true);
                        if (currentField.getName().equals(previousField.getName()) &&
                                !((T) previousField.get(previous)).equals((T) currentField.get(current))) {
                            propertyUpdate.setProperty(fieldName + "[" + idName + "]." + currentField.getName());
                        }
                    }
                }
                propertyUpdate.setPrevious((T) previous);
                propertyUpdate.setCurrent((T) current);
                break;
            }
        }
    }

    /**
     * Validates if an item of a list was added or removed
     */
    private void removedOrAdded(Object value1, Object value2, ListUpdate<T> listUpdate, String idName) throws IllegalAccessException {
        Map<T, Object> value1Map = new HashMap<>();
        Map<T, Object> value2Map = new HashMap<>();

        buildValueMap(value1Map, idName, value1);
        buildValueMap(value2Map, idName, value2);

        Map<T, Object> removedMap = new HashMap<>(value1Map);
        removedMap.keySet().removeAll(value2Map.keySet());

        Map<T, Object> addedMap = new HashMap<>(value2Map);
        addedMap.keySet().removeAll(value1Map.keySet());

        listUpdate.setRemoved(new ArrayList<T>((Collection<? extends T>) removedMap.values()));
        listUpdate.setAdded(new ArrayList<T>((Collection<? extends T>) addedMap.values()));
    }

    /**
     * Builds a map from each list object
     */
    private void buildValueMap(Map<T, Object> valueMap, String idName, Object value) throws IllegalAccessException {
        for (T innerListField : (List<T>) value) {
            for (Field f : innerListField.getClass().getDeclaredFields()) {
                if (idName.equals(f.getName())) {
                    f.setAccessible(true);
                    valueMap.put((T) f.get(innerListField), innerListField);
                }
            }
        }
    }
}
