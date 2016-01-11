package io.fabianterhorst.iron.deprecated;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.fabianterhorst.iron.Iron;
import io.fabianterhorst.iron.testdata.Person;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static io.fabianterhorst.iron.testdata.TestDataGenerator.genPerson;
import static io.fabianterhorst.iron.testdata.TestDataGenerator.genPersonList;
import static io.fabianterhorst.iron.testdata.TestDataGenerator.genPersonMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests deprecated put/get API
 */
@RunWith(AndroidJUnit4.class)
public class DataTest {

    @Before
    public void setUp() throws Exception {
        Iron.clear(getTargetContext());
        Iron.init(getTargetContext());
    }

    @Test
    public void testPutEmptyList() throws Exception {
        final List<Person> inserted = genPersonList(0);
        Iron.put("persons", inserted);
        assertThat(Iron.<List>get("persons")).isEmpty();
    }

    @Test
    public void testPutGetList() {
        final List<Person> inserted = genPersonList(10000);
        Iron.put("persons", inserted);
        List<Person> persons = Iron.get("persons");
        assertThat(persons).isEqualTo(inserted);
    }

    @Test
    public void testPutMap() {
        final Map<Integer, Person> inserted = genPersonMap(10000);
        Iron.put("persons", inserted);

        final Map<Integer, Person> personMap = Iron.get("persons");
        assertThat(personMap).isEqualTo(inserted);
    }

    @Test
    public void testPutPOJO() {
        final Person person = genPerson(1);
        Iron.put("profile", person);

        final Person savedPerson = Iron.get("profile");
        assertThat(savedPerson).isEqualTo(person);
        assertThat(savedPerson).isNotSameAs(person);
    }

    @Test
    public void testPutSubAbstractListRandomAccess() {
        final List<Person> origin = genPersonList(100);
        List<Person> sublist = origin.subList(10, 30);
        assertThat(testReadWriteWithoutClassCheck(sublist)).isEqualTo(sublist);
    }

    @Test
    public void testPutSubAbstractList() {
        final LinkedList<Person> origin = new LinkedList<>(genPersonList(100));
        List<Person> sublist = origin.subList(10, 30);
        assertThat(testReadWriteWithoutClassCheck(sublist)).isEqualTo(sublist);
    }

    @Test
    public void testPutLinkedList() {
        final LinkedList<Person> origin = new LinkedList<>(genPersonList(100));
        testReadWrite(origin);
    }

    @Test
    public void testPutArraysAsLists() {
        List list = Arrays.asList("123", "345");
        assertThat(testReadWrite(list)).isEqualTo(list.getClass());
    }

    @Test
    public void testPutCollectionsEmptyList() {
        List list = Collections.emptyList();
        assertThat(testReadWrite(list)).isEqualTo(list.getClass());
    }

    @Test
    public void testPutCollectionsEmptyMap() {
        Map map = Collections.emptyMap();
        assertThat(testReadWrite(map)).isEqualTo(map.getClass());
    }

    @Test
    public void testPutCollectionsEmptySet() {
        Set set = Collections.emptySet();
        assertThat(testReadWrite(set)).isEqualTo(set.getClass());
    }

    @Test
    public void testPutSingletonList() {
        List list = Collections.singletonList("item");
        assertThat(testReadWrite(list)).isEqualTo(list.getClass());
    }

    @Test
    public void testPutSingletonSet() {
        Set set = Collections.singleton("item");
        assertThat(testReadWrite(set)).isEqualTo(set.getClass());
    }

    @Test
    public void testPutSingletonMap() {
        Map map = Collections.singletonMap("key", "value");
        assertThat(testReadWrite(map)).isEqualTo(map.getClass());
    }

    @Test
    public void testPutGeorgianCalendar() {
        GregorianCalendar calendar = new GregorianCalendar();
        assertThat(testReadWrite(calendar)).isEqualTo(calendar.getClass());
    }

    @Test
    public void testPutSynchronizedList() {
        List list = Collections.synchronizedList(new ArrayList<>());
        assertThat(testReadWrite(list)).isEqualTo(list.getClass());
    }

    private Object testReadWriteWithoutClassCheck(Object originObj) {
        Iron.put("obj", originObj);
        Object readObj = Iron.get("obj");
        assertThat(readObj).isEqualTo(originObj);
        return readObj;
    }

    private Class testReadWrite(Object originObj) {
        Object readObj = testReadWriteWithoutClassCheck(originObj);
        assertThat(readObj.getClass()).isEqualTo(originObj.getClass());
        return readObj.getClass();
    }

}
