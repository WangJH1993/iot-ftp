package com.sitech.iotftp.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class EncryptionUtil {
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    /**
    * @Method:         genHMAC
    * @Author:         WJH
    * @CreateDate:     2019/7/31 15:26
    * @UpdateUser:     WJH
    * @UpdateDate:     2019/7/31 15:26
    * @UpdateRemark:   HmacSHA1加密
    * @Version:        1.0
    */
    public static String genHMAC(String data, String key) {
        byte[] result = null;
        try {
            //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
            SecretKeySpec signinKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
            //生成一个指定 Mac 算法 的 Mac 对象
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            //用给定密钥初始化 Mac 对象
            mac.init(signinKey);
            //完成 Mac 操作
            result = mac.doFinal(data.getBytes());


        } catch (NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
        } catch (InvalidKeyException e) {
            System.err.println(e.getMessage());
        }
        if (null != result) {
            return Hex.encodeHexString(result);
        } else {
            return null;
        }
    }

}
