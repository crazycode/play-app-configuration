package unit;

import org.junit.Ignore;
import org.junit.Test;
import play.Play;
import play.modules.appconfiguration.AppConfigurationPlugin;
import play.test.UnitTest;

public class ReconfigConfigTest extends UnitTest {

    @Test
    public void testReadConfig() {
        Play.mode = Play.Mode.PROD;
        System.setProperty(AppConfigurationPlugin.RECONFIG_DIR, Play.applicationPath.getPath() + "/test/etc/conf");
        Play.readConfiguration();
        
        assertEquals("Face", Play.configuration.getProperty("app.test1"));
        assertEquals("ProjectValue", Play.configuration.getProperty("global.replace"));
    }
   
    @Ignore
    @Test(expected=Error.class)
    public void testReadBadConfigThenError() {
        Play.mode = Play.Mode.PROD;
        System.setProperty(AppConfigurationPlugin.RECONFIG_DIR, Play.applicationPath.getPath() + "/test/etc/conf_lite");
        Play.readConfiguration();
        
        assertEquals("Face", Play.configuration.getProperty("app.test1"));
        assertEquals("ProjectValue", Play.configuration.getProperty("global.replace"));
    }
       
}
