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
import java.util.concurrent.TimeUnit;

import io.fabianterhorst.iron.Cache;
import io.fabianterhorst.iron.DataChangeCallback;
import io.fabianterhorst.iron.Iron;
import io.fabianterhorst.iron.testdata.Person;
import io.fabianterhorst.iron.testdata.TestDataGenerator;

import static android.support.test.InstrumentationRegistry.getTargetContext;

/**
 * Tests read/write listeners into iron data from multiple threads
 */
public class MultiThreadListenerTest extends AndroidTestCase {

    @Before
    public void setUp() throws Exception {
        Iron.init(getTargetContext());
        Iron.setCache(Cache.MEMORY);
        Iron.chest().destroy();
    }

    public void testMultiThreadAccess() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Callable<Object>> todo = new LinkedList<>();

        // Start with several dummy listeners waiting for data change
        for (int i = 0; i < 10; i++) {
            DataChangeCallback<List<Person>> callback = createChangeCallback("persons");
            Iron.chest().addOnDataChangeListener(callback);
        }

        // create a thread to asynchronously modify callbacks list with delays
        Runnable changeListenersRunnable = new Runnable() {
            @Override
            public void run() {
                DataChangeCallback<List<Person>> callback = createChangeCallback("persons");
                Iron.chest().addOnDataChangeListener(callback);

                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                    fail("Got error: " + e.getMessage());
                }

                Iron.chest().removeListener(callback);
            }
        };
        todo.add(Executors.callable(changeListenersRunnable));

        // create a thread to asynchronously invoke callbacks list
        Runnable invokeListenersRunnable = new Runnable() {
            @Override
            public void run() {
                int size = new Random().nextInt(200);
                List<Person> data = TestDataGenerator.genPersonList(size);
                Iron.chest().callCallbacks("persons", data);
            }
        };
        todo.add(Executors.callable(invokeListenersRunnable));

        // run async threads
        List<Future<Object>> futures = executor.invokeAll(todo);
        for (Future<Object> future : futures) {
            future.get();
        }

        // wait for async threads to finish; should not get exception nor deadlock
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        assertTrue(executor.isShutdown());
    }

    private DataChangeCallback<List<Person>> createChangeCallback(final String key) {
        return new DataChangeCallback<List<Person>>(key) {
            @Override
            public void onDataChange(final List<Person> value) {
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                    fail("Got error: " + e.getMessage());
                }
            }
        };
    }
}
