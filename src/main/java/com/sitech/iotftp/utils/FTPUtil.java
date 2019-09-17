package com.sitech.iotftp.utils;


import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.SocketException;

public class FTPUtil {
    private static Logger logger = Logger.getLogger(FTPUtil.class);



    /**
     * 获取FTPClient对象
     * @param ftpHost FTP主机服务器
     * @param ftpPassword FTP 登录密码
     * @param ftpUserName FTP登录用户名
     * @param ftpPort FTP端口 默认为21
     * @return
     */
    public static FTPClient getFTPClient(String ftpHost, int ftpPort, String ftpPassword, String ftpUserName) {
        FTPClient ftpClient = null;
        try {
            ftpClient = new FTPClient();
            ftpClient.connect(ftpHost, ftpPort);// 连接FTP服务器
            ftpClient.login(ftpUserName, ftpPassword);// 登陆FTP服务器
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                logger.info("can't connect FTP，user or password was wrong Please check it。");
                ftpClient.disconnect();
            } else {
//                logger.info("FTP connect succeed。");
            }
        } catch (SocketException e) {
            e.printStackTrace();
            logger.info("Maybe FTP's IP was wrong，Please check it。");
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("Maybe FTP's port was wrong,Please check it。");
        }
        return ftpClient;
    }
}

