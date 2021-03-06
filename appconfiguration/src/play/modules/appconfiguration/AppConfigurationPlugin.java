package play.modules.appconfiguration;

import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.modules.appconfiguration.reconfig.AppConfigurationAesEncryptHelper;
import play.modules.appconfiguration.reconfig.KeyConfig;
import play.modules.appconfiguration.reconfig.RecofigReader;
import play.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class AppConfigurationPlugin extends PlayPlugin {

    public static final String RECONFIG_DIR = "reconfig.dir";

    public static final String KEY_CONFIG_FILE = "key-define.conf";

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
        Logger.info("Use reconfig for app:" + Play.configuration.getProperty("application.name"));

        Map<String, String> reconfigMap = readReconfigMap();
        KeyConfig keyConfig = KeyConfig.read(KEY_CONFIG_FILE);
        Set<String> requireKeys = keyConfig.requiredKeys;

        if (reconfigMap.size() > 0) {
            if (Play.mode == Play.Mode.PROD) {
                //找出所有必须在reconfig中定义而没有定义的key
                List<String> undefinedKeys = new ArrayList<>();
                for (String key : requireKeys) {
                    if (!reconfigMap.keySet().contains(key)) {
                        undefinedKeys.add(key);
                    }
                }
                if (undefinedKeys.size() > 0) {
                    Logger.error("Follow key MUST be defined at " + RECONFIG_DIR + ": " + StringUtils.join(undefinedKeys, ", "));
                    Play.fatalServerErrorOccurred();
                }
            } else {
                Logger.info("It will NOT Check key-define.conf on development environment.");
            }
            for (String key : reconfigMap.keySet()) {
                String value = reconfigMap.get(key);
                if (StringUtils.isNotBlank(value)) {
                    if (value.startsWith(KeyConfig.AES_ENCRYPT_PREFIX)) {
                        String encryptValue = value.substring(KeyConfig.AES_ENCRYPT_PREFIX.length());
                        String decryptValue = AppConfigurationAesEncryptHelper.decryptFromBase64(encryptValue, KeyConfig.AES_ENCRYPT_KEY);
                        if (key.contains("test")) {
                            Logger.info("AppCOnfiguration Test Encrypt: key=" + key + ", encryptValue=" + encryptValue + ", decrypt:" + decryptValue);
                        }
                        if (StringUtils.isNotBlank(decryptValue)) {
                            Play.configuration.setProperty(key, decryptValue);
                        } else {
                            Logger.info("AppCOnfiguration Decrypt Failed: key=" + key + ", encryptValue=" + encryptValue);
                        }
                    } else {
                        Play.configuration.setProperty(key, value);
                    }
                }
            }
        } else {
            Logger.error("Does NOT found reconfig value!");
        }
    }

    /**
     * 从配置目录读取出环境相关的配置。
     *
     * @return
     */
    private Map<String, String> readReconfigMap() {
        String reconfigPath = Play.configuration.getProperty(RECONFIG_DIR);

        if (System.getProperty(RECONFIG_DIR) != null) {
            reconfigPath = System.getProperty(RECONFIG_DIR);
        }

        if (StringUtils.isEmpty(reconfigPath)) {
            Logger.error("You must config reconfig.dir on application.conf or System properties!");
            Play.fatalServerErrorOccurred();
        }

        Map<String, String> reconfigMap = new HashMap<>();
        Logger.info("reconfig.dir=" + reconfigPath);
        VirtualFile vfReconfigPath = VirtualFile.open(reconfigPath);

        if (vfReconfigPath.exists()) {
            Properties globalProps = RecofigReader.readOneConfigurationFile(vfReconfigPath, "global.conf");
            if (globalProps != null) {
                for (Object oKey : globalProps.keySet()) {
                    String key = oKey + "";
                    reconfigMap.put(key, globalProps.getProperty(key));
                }
            }
            Properties frontProps = RecofigReader.readOneConfigurationFile(vfReconfigPath, "front.conf");
            if (frontProps != null) {
                for (Object oKey : frontProps.keySet()) {
                    String key = oKey + "";
                    reconfigMap.put(key, frontProps.getProperty(key));
                }
            }
            Properties projectProps = RecofigReader.readOneConfigurationFile(vfReconfigPath, Play.configuration.getProperty("application.name") + ".conf");
            if (projectProps != null) {
                for (Object oKey : projectProps.keySet()) {
                    String key = oKey + "";
                    reconfigMap.put(key, projectProps.getProperty(key));
                }
            }
        } else {
            Logger.warn("The reconfig dir " + reconfigPath + " NOT Exists!");
        }
        return reconfigMap;
    }

}
