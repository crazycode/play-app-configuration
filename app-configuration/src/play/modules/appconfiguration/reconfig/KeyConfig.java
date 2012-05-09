package play.modules.appconfiguration.reconfig;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import play.Logger;
import play.Play;
import play.libs.IO;
import play.vfs.VirtualFile;

public class KeyConfig {
    
    public Set<String> requiredKeys = new HashSet<>();

    /**
     * 从keyConfig配置文件中读取必须要的key值
     * @param fileName
     */
    public void read(String fileName) {
        VirtualFile appRoot = VirtualFile.open(Play.applicationPath);
        VirtualFile conf = appRoot.child("conf/" + fileName);
        
        Properties propsFromFile=null;
        try {
            propsFromFile = IO.readUtf8Properties(conf.inputstream());
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException) {
                Logger.fatal("Cannot read "+fileName);
            }
            return;
        }
        for (Object oKey : propsFromFile.keySet()) {
            String key = oKey + "";
            String value = propsFromFile.getProperty(key);
            if ("Required".equalsIgnoreCase(value)) {
                requiredKeys.add(key);
            }
        }
    }
}
