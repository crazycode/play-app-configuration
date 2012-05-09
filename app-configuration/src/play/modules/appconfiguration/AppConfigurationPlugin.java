package play.modules.appconfiguration;

import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.modules.appconfiguration.reconfig.RecofigReader;
import play.vfs.VirtualFile;

public class AppConfigurationPlugin extends PlayPlugin {

    public static final String RECONFIG_DIR = "reconfig.dir";

    @Override
    public void detectChange() {
        super.detectChange();
    }

    @Override
    public void afterApplicationStart() {
        super.afterApplicationStart();
    }

    @Override
    public void onConfigurationRead() {
        if (Play.mode != Play.mode.PROD) {
            Logger.info("Does NOT USE reconfig.");
            return;
        }
        Logger.info("Use reconfig");
        
        String reconfigPath = Play.configuration.getProperty(RECONFIG_DIR);
        
        if (System.getProperty(RECONFIG_DIR) != null) {
            reconfigPath = System.getProperty(RECONFIG_DIR);
        }
        
        if (StringUtils.isEmpty(reconfigPath)) {
            System.err.println("You must config reconfig.dir on application.conf or System properties!");
            
            Play.fatalServerErrorOccurred();
        }
        
        Logger.info("reconfig.dir=" + reconfigPath);
        VirtualFile vfReconfigPath = VirtualFile.open(reconfigPath);
        
        if (vfReconfigPath.exists()) {
            Properties globalProps = RecofigReader.readOneConfigurationFile(vfReconfigPath, "global.conf");
            if (globalProps != null) {
                for (Object oKey : globalProps.keySet()) {
                    String key = oKey + "";
                    Play.configuration.setProperty(key, globalProps.getProperty(key));
                }
            }
            Properties projectProps = RecofigReader.readOneConfigurationFile(vfReconfigPath, Play.configuration.getProperty("application.name") + ".conf");
            if (projectProps != null) {
                for (Object oKey : projectProps.keySet()) {
                    String key = oKey + "";
                    Play.configuration.setProperty(key, projectProps.getProperty(key));
                }
            }            
        } else {
            Logger.warn("The reconfig dir " + reconfigPath + " NOT Exists!");
        }
        
    }

}
