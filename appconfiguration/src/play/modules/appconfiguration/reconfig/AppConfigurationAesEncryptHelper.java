/*
 * 上海毕冉信息技术有限公司 版权所有
 */
package play.modules.appconfiguration.reconfig;

import play.Logger;
import play.libs.Codec;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * AES加密.
 */
public class AppConfigurationAesEncryptHelper {
    private static final String CHAR_ENCODING = "UTF-8";
    private static final String AES_ALGORITHM = "AES/ECB/PKCS5Padding";

    private AppConfigurationAesEncryptHelper(){
        super();
    }

    /**
     * 加密
     *
     * @param data
     *            需要加密的内容
     * @param key
     *            加密密码
     * @return
     */
    private static byte[] encrypt(byte[] data, byte[] key) {
        notEmpty(data, "data");
        notEmpty(key, "key");
        if(key.length!=16){
            throw new RuntimeException("Invalid AES key length (must be 16 bytes)");
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec seckey = new SecretKeySpec(enCodeFormat,"AES");
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);// 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, seckey);// 初始化
            return cipher.doFinal(data); // 加密
        } catch (Exception e){
            throw new RuntimeException("encrypt  fail!", e);
        }
    }

    /**
     * 解密
     *
     * @param data
     *            待解密内容
     * @param key
     *            解密密钥
     * @return
     */
    public static byte[] decrypt(byte[] data, byte[] key) {
        notEmpty(data, "data");
        notEmpty(key, "key");
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec seckey = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, seckey);// 初始化
            return cipher.doFinal(data); // 加密
        } catch (Exception e){
            Logger.warn(e, "LOG14184: 出错");
            return null;
        }
    }

    public static String encryptToBase64(String data, String key){
        try {
            byte[] valueByte = encrypt(data.getBytes(CHAR_ENCODING), Codec.hexStringToByte(key));
            return Codec.encodeBASE64(valueByte);
        } catch (UnsupportedEncodingException e) {
            Logger.warn(e, "LOG14185: encryptToBase64出错, data=" + data);
            return null;
        }
    }

    public static String decryptFromBase64(String data, String key){
        try {
            byte[] originalData = Codec.decodeBASE64(data);
            byte[] valueByte = decrypt(originalData, Codec.hexStringToByte(key));
            return new String(valueByte, CHAR_ENCODING);
        } catch (UnsupportedEncodingException e) {
            Logger.warn(e, "LOG14185: decryptFromBase64出错, data=" + data);
            return null;
        }
    }

    public static void notEmpty(Object obj, String message) {
        if (obj == null){
            throw new IllegalArgumentException(message + " must be specified");
        }
        if (obj instanceof String && obj.toString().trim().length()==0){
            throw new IllegalArgumentException(message + " must be specified");
        }
        if (obj.getClass().isArray() && Array.getLength(obj)==0){
            throw new IllegalArgumentException(message + " must be specified");
        }
        if (obj instanceof Collection && ((Collection)obj).isEmpty()){
            throw new IllegalArgumentException(message + " must be specified");
        }
        if (obj instanceof Map && ((Map)obj).isEmpty()){
            throw new IllegalArgumentException(message + " must be specified");
        }
    }

}
