package io.fabianterhorst.iron.multithread;

import android.test.AndroidTestCase;

import org.junit.Before;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.fabianterhorst.iron.Iron;
import io.fabianterhorst.iron.testdata.Person;
import io.fabianterhorst.iron.testdata.TestDataGenerator;

import static android.support.test.InstrumentationRegistry.getTargetContext;

/**
 * Tests read/write into iron data from multiple threads
 */
public class MultiThreadTest extends AndroidTestCase {

    @Before
    public void setUp() throws Exception {
        Iron.init(getTargetContext());
        Iron.chest().destroy();
    }

    public void testMultiThreadAccess() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Callable<Object>> todo = new LinkedList<>();

        for (int i = 0; i <= 1000; i++) {
            Runnable task;
            if (i % 2 == 0) {
                task = getInsertRunnable();
            } else {
                task = getSelectRunnable();
            }
            todo.add(Executors.callable(task));
        }
        List<Future<Object>> futures = executor.invokeAll(todo);
        for (Future<Object> future : futures) {
            future.get();
        }
    }

    private Runnable getInsertRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                int size = new Random().nextInt(200);
                final List<Person> inserted100 = TestDataGenerator.genPersonList(size);
                Iron.chest().write("persons", inserted100);
            }
        };
    }

    private Runnable getSelectRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                Iron.chest().read("persons");
            }
        };
    }
}
