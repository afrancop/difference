# Object difference

## Table of Contents
1. [Description](#description)
2. [Time spent](#time-spent)
3. [Comments](#comments)
4. [Files description](#files-description)
5. [How this works](#how-this-works)
6. [Setup](#setup)

<a name="description"></a>
## Description
Core of an audit system that is capable of determining the difference between two objects of the same type

<a name="time-spent"></a>
## Time spent

Saturday: <green>2 hours</green>
> - Understanding the requirement and tasks breakdown

Sunday: <red>N/A</red>
> - I did not work on the solution on this day

Monday: <green>5 hours</green>
> - Creation for some basic tests for property update & list update
> - Started the development for the main method and the recursion approach
> - Split of the main class into some specific ones (Exception, types, AuditKeyAnnotation)

Tuesday: <green>1.5 hours</green>
> - Started the added/removed part
> - Started the list update approach.
> - Some problems found during coding to identify how to compare values inside the list items
> - This part was the most difficult one

Wednesday: <green>4 hours</green>
> - Continue working on the list update approach
> - Solved the issue by creating some maps instead of lists

Thursday: <green>3 hours</green>
> - Fixed some missing valiations
> - Adding/modifying unit tests

Friday: <green>2.5 hours</green>
> - Running some test to validate all scenarios
> - Organizing code
> - Created readme file.

TOTAL: <blue>**18 hours** </blue>

<a name="comments"></a>
## Comments

The hardest part was the validation to identify if an object inside a list was updated or added/removed

<a name="code"></a>
## Files description

> - DiffToolTest.java -> Test class with all scenarios

> - DiffTool.java -> Main class. Contains the core logic for the solution

> - ChangeType.java -> Main type. Contains the final result as a List of ChangeType

> - PropertyUpdate.java -> Implementation of ChangeType. It is used to store results when a property changes or when an item inside a list changes

> - ListUpdate.java -> Implementation of ChangeType. It is used to store results when an item inside a list was added or removed

> - AuditKey.java -> Custom annotation for list item identification

> - AuditException.java -> Custom exception for the system


<a name="how-this-works"></a>
## How this works

The main logic is inside class DiffTool.java. This is the explanation about how this works:

### Methods breakdown

- Exposed method for the audit system that is capable of determining the difference between two objects of the same type
```java
public List<ChangeType> diff(T previous, T current) throws IllegalAccessException
```

- Recursive method to iterate over a complex object which has nested objects to find the modified or removed/added items
```java
private void propertyDifference(T previous, T current, List<ChangeType> changedProperties, String parent) throws IllegalAccessException
```

- Validates the id, removed/added/modified items in a list and builds the response
```java
private void listValidation(Object value1, Object value2, Field field, List<ChangeType> changedProperties) throws IllegalAccessException
```

- Checks if an object is a java object 
- More restrictions may be added
- The idea is to add the common java packages that might be useful to identify if an object is a java object
```java
private boolean isJavaObject(Object object)
```

- Erases the leading parent where the updated property is on the root
```java
private void balancePath(PropertyUpdate<T> propertyUpdate) 
```

- Validates if the objects inside the list have 'id' or @AuditKey 
- This is a key method for the list validation part.
- With this method was solved the dilema when an item was updated or just added/removed 
```java
    private String validateId(Object value)
```

- Validates if an object inside a list was modified
- In this method, some utility mpas where created to accomplish the goal of identifying the modified item inside a list
```java
    private void modifiedListItem(PropertyUpdate<T> propertyUpdate, Object value1, Object value2, String idName, String fieldName) throws IllegalAccessException
```

- Builds the propertyUpdate object with the changes found in the items in the list
```java
private void buildPropertyUpdate(Map<T, Object> removedMap, Map<T, Object> addedMap, PropertyUpdate<T> propertyUpdate, String idName, String fieldName) throws IllegalAccessException
```

- Validates if an item of a list was added or removed
```java
    private void removedOrAdded(Object value1, Object value2, ListUpdate<T> listUpdate, String idName) throws IllegalAccessException
```

- Builds a map from each list object
```java
    private void buildValueMap(Map<T, Object> valueMap, String idName, Object value) throws IllegalAccessException
```

<a name="setup"></a>
## Setup

To run this locally, make sure you have:

> - Java 17
> - Junit jupiter for the unit tests

<style>
blue {
  color: CornflowerBlue;
}

red {
  color: firebrick;
}

green {
  color: green;
}
</style>