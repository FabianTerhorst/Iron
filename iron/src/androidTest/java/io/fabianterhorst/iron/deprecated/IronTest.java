package io.fabianterhorst.iron.deprecated;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.fabianterhorst.iron.Iron;
import io.fabianterhorst.iron.IronException;
import io.fabianterhorst.iron.testdata.TestDataGenerator;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;

/**
 * Tests deprecated API
 */
@RunWith(AndroidJUnit4.class)
public class IronTest {

    @Before
    public void setUp() throws Exception {
        Iron.clear(getTargetContext());
        Iron.init(getTargetContext());
    }

    @Test
    public void testExist() throws Exception {
        assertFalse(Iron.exist("persons"));
        Iron.put("persons", TestDataGenerator.genPersonList(10));
        assertTrue(Iron.exist("persons"));
    }

    @Test
    public void testDelete() throws Exception {
        Iron.put("persons", TestDataGenerator.genPersonList(10));
        assertTrue(Iron.exist("persons"));
        Iron.delete("persons");
        assertFalse(Iron.exist("persons"));
    }

    @Test
    public void testDeleteNotExisted() throws Exception {
        assertFalse(Iron.exist("persons"));
        Iron.delete("persons");
    }

    @Test
    public void testClear() throws Exception {
        Iron.put("persons", TestDataGenerator.genPersonList(10));
        Iron.put("persons2", TestDataGenerator.genPersonList(20));
        assertTrue(Iron.exist("persons"));
        assertTrue(Iron.exist("persons2"));

        Iron.clear(getTargetContext());
        // init() call is not required after clear()
        assertFalse(Iron.exist("persons"));
        assertFalse(Iron.exist("persons2"));

        // Should be possible to continue to use Iron after clear()
        Iron.put("persons3", TestDataGenerator.genPersonList(30));
        assertTrue(Iron.exist("persons3"));
        assertThat(Iron.<List>get("persons3")).hasSize(30);
    }

    @Test
    public void testPutGetNormal() {
        Iron.put("city", "Lund");
        String val = Iron.get("city", "default");
        assertThat(val).isEqualTo("Lund");
    }

    @Test
    public void testPutGetNormalAfterReinit() {
        Iron.put("city", "Lund");
        String val = Iron.get("city", "default");
        Iron.init(getTargetContext());// Reinit Iron instance
        assertThat(val).isEqualTo("Lund");
    }

    @Test
    public void testGetNotExisted() {
        String val = Iron.get("non-existed");
        assertThat(val).isNull();
    }

    @Test
    public void testGetDefault() {
        String val = Iron.get("non-existed", "default");
        assertThat(val).isEqualTo("default");
    }

    @Test
    public void testReplace() {
        Iron.put("city", "Lund");
        Iron.put("city", "Kyiv");
        assertThat(Iron.get("city")).isEqualTo("Kyiv");
    }

    @Test
    public void testValidKeyNames() {
        Iron.put("city", "Lund");
        assertThat(Iron.get("city")).isEqualTo("Lund");

        Iron.put("city.dasd&%", "Lund");
        assertThat(Iron.get("city.dasd&%")).isEqualTo("Lund");

        Iron.put("city-ads", "Lund");
        assertThat(Iron.get("city-ads")).isEqualTo("Lund");
    }

    @Test(expected = IronException.class)
    public void testInvalidKeyNameBackslash() {
        Iron.put("city/ads", "Lund");
        assertThat(Iron.get("city/ads")).isEqualTo("Lund");
    }

}