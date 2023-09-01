package com.difference;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DiffToolTest {

    @Test
    @DisplayName("Test for update on property when one object is null")
    public void diffPropertyUpdateNullCurrent() {
        Book previousBook = new Book(1, "book1", new Category(1, "cat1"));
        DiffTool<Book> diffTool = new DiffTool<>();
        assertThrows(AuditException.class, () -> diffTool.diff(previousBook, null));
    }

    @Test
    @DisplayName("Test for update on property when one object is null")
    public void diffPropertyUpdateNullPrevious() {
        Book currentBook = new Book(1, "book1", new Category(1, "cat1"));
        DiffTool<Book> diffTool = new DiffTool<>();
        assertThrows(AuditException.class, () -> diffTool.diff(null, currentBook));
    }

    @Test
    @DisplayName("Test for update on property")
    public void diffPropertyUpdate() throws IllegalAccessException, NoSuchFieldException {

        Book previousBook = new Book(1, "book1", new Category(1, "cat1"));
        Book currentBook = new Book(1, "book2", new Category(1, "cat1"));

        DiffTool<Book> diffTool = new DiffTool<>();
        List<ChangeType> list = diffTool.diff(previousBook, currentBook);

        PropertyUpdate<Book> expected = new PropertyUpdate<>();
        expected.setPrevious(previousBook);
        expected.setCurrent(currentBook);
        expected.setProperty("bookName");

        assertEquals(expected.getProperty(), list.get(0).getProperty());
        assertEquals(expected.getPrevious().bookName, ((Book) ((PropertyUpdate<?>) list.get(0)).getPrevious()).bookName);
        assertEquals(expected.getCurrent().bookName, ((Book) ((PropertyUpdate<?>) list.get(0)).getCurrent()).bookName);

    }

    @Test
    @DisplayName("Test for update on nested property")
    public void diffNestedPropertyUpdate() throws IllegalAccessException, NoSuchFieldException {

        Book previousBook = new Book(1, "book1", new Category(1, "cat1"));
        Book currentBook = new Book(1, "book1", new Category(1, "cat1-2"));

        DiffTool<Book> diffTool = new DiffTool<>();
        List<ChangeType> list = diffTool.diff(previousBook, currentBook);

        PropertyUpdate<Book> expected = new PropertyUpdate<>();
        expected.setPrevious(previousBook);
        expected.setCurrent(currentBook);
        expected.setProperty("Book.category.categoryName");

        assertEquals(expected.getProperty(), list.get(0).getProperty());
        assertEquals(expected.getPrevious().category.categoryName, ((Category) ((PropertyUpdate<?>) list.get(0)).getPrevious()).categoryName);
        assertEquals(expected.getCurrent().category.categoryName, ((Category) ((PropertyUpdate<?>) list.get(0)).getCurrent()).categoryName);

    }

    @Test
    @DisplayName("Test for update on property and nestedProperty")
    public void diffPropertyAndNestedPropertyUpdate() throws IllegalAccessException, NoSuchFieldException {

        Book previousBook = new Book(1, "book1", new Category(1, "cat1"));
        Book currentBook = new Book(1, "book2", new Category(1, "cat1-2"));

        DiffTool<Book> diffTool = new DiffTool<>();
        List<ChangeType> list = diffTool.diff(previousBook, currentBook);

        PropertyUpdate<Book> expected1 = new PropertyUpdate<>();
        expected1.setPrevious(previousBook);
        expected1.setCurrent(currentBook);
        expected1.setProperty("bookName");

        PropertyUpdate<Book> expected2 = new PropertyUpdate<>();
        expected2.setPrevious(previousBook);
        expected2.setCurrent(currentBook);
        expected2.setProperty("Book.category.categoryName");

        assertEquals(expected1.getProperty(), list.get(0).getProperty());
        assertEquals(expected1.getPrevious().bookName, ((Book) ((PropertyUpdate<?>) list.get(0)).getPrevious()).bookName);
        assertEquals(expected1.getCurrent().bookName, ((Book) ((PropertyUpdate<?>) list.get(0)).getCurrent()).bookName);

        assertEquals(expected2.getProperty(), list.get(1).getProperty());
        assertEquals(expected2.getPrevious().category.categoryName, ((Category) ((PropertyUpdate<?>) list.get(1)).getPrevious()).categoryName);
        assertEquals(expected2.getCurrent().category.categoryName, ((Category) ((PropertyUpdate<?>) list.get(1)).getCurrent()).categoryName);
    }

    @Test
    @DisplayName("Test for no added or removed items with id")
    public void diffNoAddedRemovedItemsWithId() throws IllegalAccessException, NoSuchFieldException {

        Client previousClient = new Client(1, "client1", List.of(new Account(1, "account1"), new Account(2, "account2")));
        Client currentClient = new Client(1,  "client1", List.of(new Account(1, "account1"), new Account(2, "account2")));

        DiffTool<Client> diffTool = new DiffTool<>();
        List<ChangeType> list = diffTool.diff(previousClient, currentClient);

        ListUpdate<Account> expected = new ListUpdate<>();
        expected.setRemoved(List.of());
        expected.setAdded(List.of());
        expected.setProperty("accounts");

        assertEquals(expected.getProperty(), list.get(0).getProperty());
        assertEquals(expected.getAdded(), ((ListUpdate<?>) list.get(0)).getAdded());
        assertEquals(expected.getRemoved(), ((ListUpdate<?>) list.get(0)).getRemoved());
    }

    @Test
    @DisplayName("Test for removed items with id")
    public void diffRemovedItemsWithId() throws IllegalAccessException, NoSuchFieldException {

        Client previousClient = new Client(1, "client1",List.of(new Account(1, "account1"), new Account(2, "account2"), new Account(3, "account3")));
        Client currentClient = new Client(1, "client1", List.of(new Account(1, "account1"), new Account(2, "account2")));

        DiffTool<Client> diffTool = new DiffTool<>();
        List<ChangeType> list = diffTool.diff(previousClient, currentClient);

        ListUpdate<Account> expected = new ListUpdate<>();
        expected.setRemoved(List.of(new Account(3, "account3")));
        expected.setAdded(List.of());
        expected.setProperty("accounts");

        assertEquals(expected.getProperty(), list.get(0).getProperty());
        assertEquals(expected.getAdded(), ((ListUpdate<?>) list.get(0)).getAdded());
        assertEquals(expected.getRemoved(), ((ListUpdate<?>) list.get(0)).getRemoved());
    }

    @Test
    @DisplayName("Test for added items with id")
    public void diffAddedItemsWithId() throws IllegalAccessException, NoSuchFieldException {

        Client previousClient = new Client(1, "client1", List.of(new Account(1, "account1"), new Account(2, "account2")));
        Client currentClient = new Client(1, "client1",  List.of(new Account(1, "account1"), new Account(2, "account2"), new Account(3, "account3")));

        DiffTool<Client> diffTool = new DiffTool<>();
        List<ChangeType> list = diffTool.diff(previousClient, currentClient);

        ListUpdate<Account> expected = new ListUpdate<>();
        expected.setRemoved(List.of());
        expected.setAdded(List.of(new Account(3, "account3")));
        expected.setProperty("accounts");

        assertEquals(expected.getProperty(), list.get(0).getProperty());
        assertEquals(expected.getAdded(), ((ListUpdate<?>) list.get(0)).getAdded());
        assertEquals(expected.getRemoved(), ((ListUpdate<?>) list.get(0)).getRemoved());
    }

    @Test
    @DisplayName("Test for added/removed items with id")
    public void diffAddedRemovedItemsWithId() throws IllegalAccessException, NoSuchFieldException {

        Client previousClient = new Client(1, "client1", List.of(new Account(1, "account1"), new Account(2, "account2"), new Account(3, "account3")));
        Client currentClient = new Client(1, "client1",  List.of(new Account(1, "account1"), new Account(2, "account2"), new Account(4, "account4")));

        DiffTool<Client> diffTool = new DiffTool<>();
        List<ChangeType> list = diffTool.diff(previousClient, currentClient);

        ListUpdate<Account> expected = new ListUpdate<>();
        expected.setRemoved(List.of(new Account(3, "account3")));
        expected.setAdded(List.of(new Account(4, "account4")));
        expected.setProperty("accounts");

        assertEquals(expected.getProperty(), list.get(0).getProperty());
        assertEquals(expected.getAdded(), ((ListUpdate<?>) list.get(0)).getAdded());
        assertEquals(expected.getRemoved(), ((ListUpdate<?>) list.get(0)).getRemoved());
    }

    @Test
    @DisplayName("Test for modified and added/removed items with id")
    public void diffModifiedAddedRemovedItemsWithId() throws IllegalAccessException, NoSuchFieldException {

        Client previousClient = new Client(1, "client1", List.of(new Account(1, "account1"), new Account(2, "account2"), new Account(3, "account3")));
        Client currentClient = new Client(1, "client1", List.of(new Account(1, "account1"), new Account(2, "account5"), new Account(4, "account4")));

        DiffTool<Client> diffTool = new DiffTool<>();
        List<ChangeType> list = diffTool.diff(previousClient, currentClient);

        ListUpdate<Account> expectedList = new ListUpdate<>();
        expectedList.setRemoved(List.of(new Account(3, "account3")));
        expectedList.setAdded(List.of(new Account(4, "account4")));
        expectedList.setProperty("accounts");

        assertEquals(expectedList.getProperty(), list.get(0).getProperty());
        assertEquals(expectedList.getAdded(), ((ListUpdate<?>) list.get(0)).getAdded());
        assertEquals(expectedList.getRemoved(), ((ListUpdate<?>) list.get(0)).getRemoved());

        PropertyUpdate<Account> expectedProperty = new PropertyUpdate<>();
        expectedProperty.setProperty("accounts[id].accountName");
        expectedProperty.setCurrent(new Account(2, "account5"));
        expectedProperty.setPrevious(new Account(2, "account2"));

        assertEquals(expectedProperty.getProperty(), list.get(1).getProperty());
        assertEquals(expectedProperty.getPrevious(), ((PropertyUpdate<?>) list.get(1)).getPrevious());
        assertEquals(expectedProperty.getCurrent(), ((PropertyUpdate<?>) list.get(1)).getCurrent());
    }

    @Test
    @DisplayName("Test for modified root, modified item in list and added/removed items with id")
    public void diffModifiedRootModifiedItemAddedRemovedItemsWithId() throws IllegalAccessException, NoSuchFieldException {

        Client previousClient = new Client(1, "client1", List.of(new Account(1, "account1"), new Account(2, "account2"), new Account(3, "account3")));
        Client currentClient = new Client(1, "client2",  List.of(new Account(1, "account1"), new Account(2, "account5"), new Account(4, "account4")));

        DiffTool<Client> diffTool = new DiffTool<>();
        List<ChangeType> list = diffTool.diff(previousClient, currentClient);

        PropertyUpdate<Client> expectedRootProperty = new PropertyUpdate<>();
        expectedRootProperty.setProperty("clientName");
        expectedRootProperty.setCurrent(currentClient);
        expectedRootProperty.setPrevious(previousClient);

        assertEquals(expectedRootProperty.getProperty(), list.get(0).getProperty());
        assertEquals(expectedRootProperty.getPrevious(), ((PropertyUpdate<?>) list.get(0)).getPrevious());
        assertEquals(expectedRootProperty.getCurrent(), ((PropertyUpdate<?>) list.get(0)).getCurrent());

        ListUpdate<Account> expectedList = new ListUpdate<>();
        expectedList.setRemoved(List.of(new Account(3, "account3")));
        expectedList.setAdded(List.of(new Account(4, "account4")));
        expectedList.setProperty("accounts");

        assertEquals(expectedList.getProperty(), list.get(1).getProperty());
        assertEquals(expectedList.getAdded(), ((ListUpdate<?>) list.get(1)).getAdded());
        assertEquals(expectedList.getRemoved(), ((ListUpdate<?>) list.get(1)).getRemoved());

        PropertyUpdate<Account> expectedProperty = new PropertyUpdate<>();
        expectedProperty.setProperty("accounts[id].accountName");
        expectedProperty.setCurrent(new Account(2, "account5"));
        expectedProperty.setPrevious(new Account(2, "account2"));

        assertEquals(expectedProperty.getProperty(), list.get(2).getProperty());
        assertEquals(expectedProperty.getPrevious(), ((PropertyUpdate<?>) list.get(2)).getPrevious());
        assertEquals(expectedProperty.getCurrent(), ((PropertyUpdate<?>) list.get(2)).getCurrent());
    }

    @Test
    @DisplayName("Test for modified list item when has no id nor @AuditKey")
    public void noIdNoAuditKey() {
        Car previousCar = new Car(1, "Ferrari", List.of(new Wheel(1, "wheel1")));
        Car currentCar = new Car(1, "Ferrari",  List.of(new Wheel(1, "wheel1")));

        DiffTool<Car> diffTool = new DiffTool<>();

        assertThrows(AuditException.class, () -> diffTool.diff(previousCar, currentCar));
    }

    @Test
    @DisplayName("Test for no added or removed items with auditKey")
    public void diffNoAddedRemovedItemsWithAuditKey() throws IllegalAccessException, NoSuchFieldException {

        Student previousStudent = new Student(1, "student1", List.of(new Subject(1, "maths"), new Subject(2, "chemistry")));
        Student currentStudent = new Student(1,  "student1", List.of(new Subject(1, "maths"), new Subject(2, "chemistry")));

        DiffTool<Student> diffTool = new DiffTool<>();
        List<ChangeType> list = diffTool.diff(previousStudent, currentStudent);

        ListUpdate<Subject> expected = new ListUpdate<>();
        expected.setRemoved(List.of());
        expected.setAdded(List.of());
        expected.setProperty("subjects");

        assertEquals(expected.getProperty(), list.get(0).getProperty());
        assertEquals(expected.getAdded(), ((ListUpdate<?>) list.get(0)).getAdded());
        assertEquals(expected.getRemoved(), ((ListUpdate<?>) list.get(0)).getRemoved());
    }

    @Test
    @DisplayName("Test for removed items with auditKey")
    public void diffRemovedItemsWithAuditKey() throws IllegalAccessException, NoSuchFieldException {

        Student previousStudent = new Student(1, "student1", List.of(new Subject(1, "maths"), new Subject(2, "chemistry"), new Subject(3, "art")));
        Student currentStudent = new Student(1,  "student1", List.of(new Subject(1, "maths"), new Subject(2, "chemistry")));

        DiffTool<Student> diffTool = new DiffTool<>();
        List<ChangeType> list = diffTool.diff(previousStudent, currentStudent);

        ListUpdate<Subject> expected = new ListUpdate<>();
        expected.setRemoved(List.of(new Subject(3, "art")));
        expected.setAdded(List.of());
        expected.setProperty("subjects");

        assertEquals(expected.getProperty(), list.get(0).getProperty());
        assertEquals(expected.getAdded(), ((ListUpdate<?>) list.get(0)).getAdded());
        assertEquals(expected.getRemoved(), ((ListUpdate<?>) list.get(0)).getRemoved());
    }

    @Test
    @DisplayName("Test for added items with auditKey")
    public void diffAddedItemsWithAuditKey() throws IllegalAccessException, NoSuchFieldException {

        Student previousStudent = new Student(1, "student1", List.of(new Subject(1, "maths"), new Subject(2, "chemistry")));
        Student currentStudent = new Student(1,  "student1", List.of(new Subject(1, "maths"), new Subject(2, "chemistry"), new Subject(3, "art")));

        DiffTool<Student> diffTool = new DiffTool<>();
        List<ChangeType> list = diffTool.diff(previousStudent, currentStudent);

        ListUpdate<Subject> expected = new ListUpdate<>();
        expected.setRemoved(List.of());
        expected.setAdded(List.of(new Subject(3, "art")));
        expected.setProperty("subjects");

        assertEquals(expected.getProperty(), list.get(0).getProperty());
        assertEquals(expected.getAdded(), ((ListUpdate<?>) list.get(0)).getAdded());
        assertEquals(expected.getRemoved(), ((ListUpdate<?>) list.get(0)).getRemoved());
    }

    @Test
    @DisplayName("Test for added/removed items with auditKey")
    public void diffAddedRemovedItemsWithAuditKey() throws IllegalAccessException, NoSuchFieldException {

        Student previousStudent = new Student(1, "student1", List.of(new Subject(1, "maths"), new Subject(2, "chemistry"), new Subject(3, "science")));
        Student currentStudent = new Student(1,  "student1", List.of(new Subject(1, "maths"), new Subject(2, "chemistry"), new Subject(4, "art")));

        DiffTool<Student> diffTool = new DiffTool<>();
        List<ChangeType> list = diffTool.diff(previousStudent, currentStudent);

        ListUpdate<Subject> expected = new ListUpdate<>();
        expected.setRemoved(List.of(new Subject(3, "science")));
        expected.setAdded(List.of(new Subject(4, "art")));
        expected.setProperty("subjects");

        assertEquals(expected.getProperty(), list.get(0).getProperty());
        assertEquals(expected.getAdded(), ((ListUpdate<?>) list.get(0)).getAdded());
        assertEquals(expected.getRemoved(), ((ListUpdate<?>) list.get(0)).getRemoved());
    }

    @Test
    @DisplayName("Test for modified and added/removed items with auditKey")
    public void diffModifiedAddedRemovedItemsWithAuditKey() throws IllegalAccessException, NoSuchFieldException {

        Student previousStudent = new Student(1, "student1", List.of(new Subject(1, "maths"), new Subject(2, "chemistry"), new Subject(3, "science")));
        Student currentStudent = new Student(1,  "student1", List.of(new Subject(1, "maths"), new Subject(2, "music"), new Subject(4, "art")));

        DiffTool<Student> diffTool = new DiffTool<>();
        List<ChangeType> list = diffTool.diff(previousStudent, currentStudent);

        ListUpdate<Subject> expected = new ListUpdate<>();
        expected.setRemoved(List.of(new Subject(3, "science")));
        expected.setAdded(List.of(new Subject(4, "art")));
        expected.setProperty("subjects");

        assertEquals(expected.getProperty(), list.get(0).getProperty());
        assertEquals(expected.getAdded(), ((ListUpdate<?>) list.get(0)).getAdded());
        assertEquals(expected.getRemoved(), ((ListUpdate<?>) list.get(0)).getRemoved());

        PropertyUpdate<Subject> expectedProperty = new PropertyUpdate<>();
        expectedProperty.setProperty("subjects[subjectId].subjectName");
        expectedProperty.setCurrent(new Subject(2, "music"));
        expectedProperty.setPrevious(new Subject(2, "chemistry"));

        assertEquals(expectedProperty.getProperty(), list.get(1).getProperty());
        assertEquals(expectedProperty.getPrevious(), ((PropertyUpdate<?>) list.get(1)).getPrevious());
        assertEquals(expectedProperty.getCurrent(), ((PropertyUpdate<?>) list.get(1)).getCurrent());
    }

    @Test
    @DisplayName("Test for modified root, modified item in list and added/removed items with auditKey")
    public void diffModifiedRootModifiedItemAddedRemovedItemsWithAuditKey() throws IllegalAccessException, NoSuchFieldException {

        Student previousStudent = new Student(1, "student1", List.of(new Subject(1, "maths"), new Subject(2, "chemistry"), new Subject(3, "science")));
        Student currentStudent = new Student(1,  "student2", List.of(new Subject(1, "maths"), new Subject(2, "music"), new Subject(4, "art")));

        DiffTool<Student> diffTool = new DiffTool<>();
        List<ChangeType> list = diffTool.diff(previousStudent, currentStudent);

        PropertyUpdate<Student> expectedRootProperty = new PropertyUpdate<>();
        expectedRootProperty.setProperty("name");
        expectedRootProperty.setCurrent(currentStudent);
        expectedRootProperty.setPrevious(previousStudent);

        assertEquals(expectedRootProperty.getProperty(), list.get(0).getProperty());
        assertEquals(expectedRootProperty.getPrevious(), ((PropertyUpdate<?>) list.get(0)).getPrevious());
        assertEquals(expectedRootProperty.getCurrent(), ((PropertyUpdate<?>) list.get(0)).getCurrent());

        ListUpdate<Subject> expected = new ListUpdate<>();
        expected.setRemoved(List.of(new Subject(3, "science")));
        expected.setAdded(List.of(new Subject(4, "art")));
        expected.setProperty("subjects");

        assertEquals(expected.getProperty(), list.get(1).getProperty());
        assertEquals(expected.getAdded(), ((ListUpdate<?>) list.get(1)).getAdded());
        assertEquals(expected.getRemoved(), ((ListUpdate<?>) list.get(1)).getRemoved());

        PropertyUpdate<Subject> expectedProperty = new PropertyUpdate<>();
        expectedProperty.setProperty("subjects[subjectId].subjectName");
        expectedProperty.setCurrent(new Subject(2, "music"));
        expectedProperty.setPrevious(new Subject(2, "chemistry"));

        assertEquals(expectedProperty.getProperty(), list.get(2).getProperty());
        assertEquals(expectedProperty.getPrevious(), ((PropertyUpdate<?>) list.get(2)).getPrevious());
        assertEquals(expectedProperty.getCurrent(), ((PropertyUpdate<?>) list.get(2)).getCurrent());
    }

    public record Book(Integer bookId, String bookName, Category category) {
    }

    public record Category(Integer categoryId, String categoryName) {
    }

    public record Client(Integer clientId, String clientName, List<Account> accounts) {
    }

    public record Account(Integer id, String accountName) {
    }

    public record Car(Integer carId, String brand, List<Wheel> wheels) {
    }

    public record Wheel(Integer wheelId, String wheelBrand) {
    }

    public record Student(Integer studentId, String name, List<Subject> subjects) {
    }

    public record Subject(@AuditKey Integer subjectId, String subjectName) {
    }

}
