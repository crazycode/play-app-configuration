package play.modules.appconfiguration.reconfig;

import play.Logger;
import play.Play;
import play.libs.IO;
import play.vfs.VirtualFile;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class KeyConfig {

    public final static String AES_ENCRYPT_PREFIX = "AESENC:";
    public final static String AES_ENCRYPT_KEY = "A31C272410964DF293952429C6CB3B28";

    public Set<String> requiredKeys = new HashSet<>();

    /**
     * 从keyConfig配置文件中读取必须要的key值
     * @param fileName
     */
    public static KeyConfig read(String fileName) {
        KeyConfig keyConfig = new KeyConfig();
        VirtualFile appRoot = VirtualFile.open(Play.applicationPath);
        VirtualFile conf = appRoot.child("conf/" + fileName);
        
        Properties propsFromFile=null;
        try {
            propsFromFile = IO.readUtf8Properties(conf.inputstream());
        } catch (RuntimeException e) {
            Logger.info("Not Found conf/" + fileName + ", use empty KeyConfig.");
            return keyConfig;
        }
        for (Object oKey : propsFromFile.keySet()) {
            String key = oKey + "";
            String value = propsFromFile.getProperty(key);
            if (value != null && ("Required".equalsIgnoreCase(value) || value.startsWith("R"))) {
                keyConfig.requiredKeys.add(key);
            } else {
                Logger.info("Can't process keyConfig " + key + ":" + value);
            }
        }
        return keyConfig;
    }
}
