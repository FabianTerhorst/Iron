package io.fabianterhorst.iron;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.fabianterhorst.iron.testdata.TestDataGenerator;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class IronTest {

    @Before
    public void setUp() throws Exception {
        Iron.init(getTargetContext());
        Iron.chest().destroy();
    }

    @Test
    public void testExist() throws Exception {
        assertFalse(Iron.chest().exist("persons"));
        Iron.chest().write("persons", TestDataGenerator.genPersonList(10));
        assertTrue(Iron.chest().exist("persons"));
    }

    @Test
    public void testDelete() throws Exception {
        Iron.chest().write("persons", TestDataGenerator.genPersonList(10));
        assertTrue(Iron.chest().exist("persons"));
        Iron.chest().delete("persons");
        assertFalse(Iron.chest().exist("persons"));
    }

    @Test
    public void testDeleteNotExisted() throws Exception {
        assertFalse(Iron.chest().exist("persons"));
        Iron.chest().delete("persons");
    }

    @Test
    public void testClear() throws Exception {
        Iron.chest().write("persons", TestDataGenerator.genPersonList(10));
        Iron.chest().write("persons2", TestDataGenerator.genPersonList(20));
        assertTrue(Iron.chest().exist("persons"));
        assertTrue(Iron.chest().exist("persons2"));

        Iron.chest().destroy();
        // init() call is not required after clear()
        assertFalse(Iron.chest().exist("persons"));
        assertFalse(Iron.chest().exist("persons2"));

        // Should be possible to continue to use Iron after clear()
        Iron.chest().write("persons3", TestDataGenerator.genPersonList(30));
        assertTrue(Iron.chest().exist("persons3"));
        assertThat(Iron.chest().<List>read("persons3")).hasSize(30);
    }

    @Test
    public void testWriteReadNormal() {
        Iron.chest().write("city", "Lund");
        String val = Iron.chest().read("city", "default");
        assertThat(val).isEqualTo("Lund");
    }

    @Test
    public void testWriteReadNormalAfterReinit() {
        Iron.chest().write("city", "Lund");
        String val = Iron.chest().read("city", "default");
        Iron.init(getTargetContext());// Reinit Iron instance
        assertThat(val).isEqualTo("Lund");
    }

    @Test
    public void testReadNotExisted() {
        String val = Iron.chest().read("non-existed");
        assertThat(val).isNull();
    }

    @Test
    public void testReadDefault() {
        String val = Iron.chest().read("non-existed", "default");
        assertThat(val).isEqualTo("default");
    }

    @Test(expected = IronException.class)
    public void testWriteNull() {
        Iron.chest().write("city", null);
    }

    @Test
    public void testReplace() {
        Iron.chest().write("city", "Lund");
        assertThat(Iron.chest().read("city")).isEqualTo("Lund");
        Iron.chest().write("city", "Kyiv");
        assertThat(Iron.chest().read("city")).isEqualTo("Kyiv");
    }

    @Test
    public void testValidKeyNames() {
        Iron.chest().write("city", "Lund");
        assertThat(Iron.chest().read("city")).isEqualTo("Lund");

        Iron.chest().write("city.dasd&%", "Lund");
        assertThat(Iron.chest().read("city.dasd&%")).isEqualTo("Lund");

        Iron.chest().write("city-ads", "Lund");
        assertThat(Iron.chest().read("city-ads")).isEqualTo("Lund");
    }

    @Test(expected=IronException.class)
    public void testInvalidKeyNameBackslash() {
        Iron.chest().write("city/ads", "Lund");
        assertThat(Iron.chest().read("city/ads")).isEqualTo("Lund");
    }

    @Test(expected=IronException.class)
    public void testGetChestWithDefaultChestName() {
        Iron.chest(Iron.DEFAULT_DB_NAME);
    }

    @Test
    public void testCustomChestReadWrite() {
        final String NATIVE = "native";
        assertThat(Iron.chest()).isNotSameAs(Iron.chest(NATIVE));
        Iron.chest(NATIVE).destroy();

        Iron.chest().write("city", "Lund");
        Iron.chest(NATIVE).write("city", "Kyiv");

        assertThat(Iron.chest().read("city")).isEqualTo("Lund");
        assertThat(Iron.chest(NATIVE).read("city")).isEqualTo("Kyiv");
    }

    @Test
    public void testCustomChestDestroy() {
        final String NATIVE = "native";
        Iron.chest(NATIVE).destroy();

        Iron.chest().write("city", "Lund");
        Iron.chest(NATIVE).write("city", "Kyiv");

        Iron.chest(NATIVE).destroy();

        assertThat(Iron.chest().read("city")).isEqualTo("Lund");
        assertThat(Iron.chest(NATIVE).read("city")).isNull();
    }

    @Test
    public void testGetAllKeys() {
        Iron.chest().destroy();

        Iron.chest().write("city", "Lund");
        Iron.chest().write("city1", "Lund1");
        Iron.chest().write("city2", "Lund2");
        List<String> allKeys = Iron.chest().getAllKeys();

        assertThat(allKeys.size()).isEqualTo(3);
        assertThat(allKeys.contains("city")).isTrue();
        assertThat(allKeys.contains("city1")).isTrue();
        assertThat(allKeys.contains("city2")).isTrue();
    }

}