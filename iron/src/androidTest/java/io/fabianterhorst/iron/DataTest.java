package io.fabianterhorst.iron;

import android.os.Handler;
import android.os.Looper;
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

import io.fabianterhorst.iron.testdata.ClassWithoutPublicNoArgConstructor;
import io.fabianterhorst.iron.testdata.Person;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static io.fabianterhorst.iron.testdata.TestDataGenerator.genPerson;
import static io.fabianterhorst.iron.testdata.TestDataGenerator.genPersonList;
import static io.fabianterhorst.iron.testdata.TestDataGenerator.genPersonMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests List write/read API
 */
@RunWith(AndroidJUnit4.class)
public class DataTest {

    @Before
    public void setUp() throws Exception {
        Iron.init(getTargetContext());
        Iron.chest("keys").destroy();
        Iron.chest().destroy();
        Iron.setCache(new LruCache());
        //Iron.setEncryptionExtension(new IronEncryption());
    }

    @Test
    public void testPutEmptyList() throws Exception {
        final List<Person> inserted = genPersonList(0);
        Iron.chest().write("persons", inserted);
        Iron.chest().invalidateCache("persons");
        assertThat(Iron.chest().<List>read("persons")).isEmpty();
    }

    @Test
    public void testReadEmptyListInEmptyChest() {
        Iron.init(getTargetContext());
        Iron.setCache(new LruCache());
        Iron.chest("keys").destroy();
        Iron.chest().destroy();
        //Iron.setEncryptionExtension(new IronEncryption());
        assertThat(Iron.chest().<List>read("persons2")).isNull();
        assertThat(Iron.chest().read("persons2", new ArrayList<Person>())).isNotNull();
        Iron.chest().write("persons2", genPersonList(1));
        Iron.chest().invalidateCache("persons2");
        Iron.init(getTargetContext());
        Iron.setCache(new LruCache());
        //Iron.setEncryptionExtension(new IronEncryption());
        assertThat(Iron.chest().read("persons2")).isNotNull();
        assertThat(Iron.chest().<List>read("persons2")).isNotEmpty();
        Iron.chest().invalidateCache("persons2");
        Iron.init(getTargetContext());
        Iron.setCache(new LruCache());
        //Iron.setEncryptionExtension(new IronEncryption());
        assertThat(Iron.chest().read("persons2")).isNotNull();
        assertThat(Iron.chest().<List>read("persons2")).isNotEmpty();
    }

    @Test
    public void testPutGetList() {
        final List<Person> inserted = genPersonList(10000);
        Iron.chest().write("persons", inserted);
        Iron.chest().invalidateCache("persons");
        List<Person> persons = Iron.chest().read("persons");
        assertThat(persons).isEqualTo(inserted);
    }

    @Test
    public void testPutDifferentThreads() {
        List<Person> inserted = genPersonList(10000);
        Iron.chest().put("persons", inserted);
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Person> inserted = genPersonList(10000);
                Iron.chest().put("persons", inserted);
            }
        }).start();
        Runnable runnable = new Runnable(){
            @Override
            public void run() {
                List<Person> inserted = genPersonList(10000);
                Iron.chest().put("persons", inserted);
            }
        };
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(runnable);
        Iron.chest().put("persons", inserted);
    }

    @Test
    public void testPutMap() {
        final Map<Integer, Person> inserted = genPersonMap(10000);
        Iron.chest().write("persons", inserted);
        Iron.chest().invalidateCache("persons");
        final Map<Integer, Person> personMap = Iron.chest().read("persons");
        assertThat(personMap).isEqualTo(inserted);
    }

    @Test
    public void testPutPOJO() {
        final Person person = genPerson(1);
        Iron.chest().write("profile", person);
        Iron.chest().invalidateCache("profile");
        final Person savedPerson = Iron.chest().read("profile");
        assertThat(savedPerson).isEqualTo(person);
        assertThat(savedPerson).isNotSameAs(person);
    }

    @Test
    public void testPutSubAbstractListRandomAccess() {
        final List<Person> origin = genPersonList(100);
        List<Person> sublist = origin.subList(10, 30);
        testReadWriteWithoutClassCheck(sublist);
    }

    @Test
    public void testPutSubAbstractList() {
        final LinkedList<Person> origin = new LinkedList<>(genPersonList(100));
        List<Person> sublist = origin.subList(10, 30);
        testReadWriteWithoutClassCheck(sublist);
    }

    @Test
    public void testPutLinkedList() {
        final LinkedList<Person> origin = new LinkedList<>(genPersonList(100));
        testReadWrite(origin);
    }

    @Test
    public void testPutArraysAsLists() {
        testReadWrite(Arrays.asList("123", "345"));
    }

    @Test
    public void testPutCollectionsEmptyList() {
        testReadWrite(Collections.emptyList());
    }

    @Test
    public void testPutCollectionsEmptyMap() {
        testReadWrite(Collections.emptyMap());
    }

    @Test
    public void testPutCollectionsEmptySet() {
        testReadWrite(Collections.emptySet());
    }

    @Test
    public void testPutSingletonList() {
        testReadWrite(Collections.singletonList("item"));
    }

    @Test
    public void testPutSingletonSet() {
        testReadWrite(Collections.singleton("item"));
    }

    @Test
    public void testPutSingletonMap() {
        testReadWrite(Collections.singletonMap("key", "value"));
    }

    @Test
    public void testPutGeorgianCalendar() {
        testReadWrite(new GregorianCalendar());
    }

    @Test
    public void testPutSynchronizedList() {
        testReadWrite(Collections.synchronizedList(new ArrayList<>()));
    }

    @Test(expected = IronException.class)
    public void testReadWriteClassWithoutNoArgConstructor() {
        testReadWrite(new ClassWithoutPublicNoArgConstructor("constructor argument"));
    }

    private Object testReadWriteWithoutClassCheck(Object originObj) {
        Iron.chest().write("obj", originObj);
        Iron.chest().invalidateCache("obj");
        Object readObj = Iron.chest().read("obj");
        assertThat(readObj).isEqualTo(originObj);
        return readObj;
    }

    private void testReadWrite(Object originObj) {
        Object readObj = testReadWriteWithoutClassCheck(originObj);
        assertThat(readObj.getClass()).isEqualTo(originObj.getClass());
    }

}
