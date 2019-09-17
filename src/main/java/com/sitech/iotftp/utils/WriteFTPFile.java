package com.sitech.iotftp.utils;


import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;

import java.io.*;


public class WriteFTPFile {

    private static Logger logger = Logger.getLogger(WriteFTPFile.class);

    /**
     * 本地上传文件到FTP服务器
     *
     * @param ftpPath
     *            远程文件路径FTP
     * @throws IOException
     */
    public static void upload(String ftpPath, String ftpUserName, String ftpPassword,
                       String ftpHost, int ftpPort, String fileName,String newName) throws IOException {
        FTPClient ftpClient = null;
        File f = null;
        InputStream in = null;
        logger.info("start upload file to FTP.");
        try {
            f = new File(fileName);
            System.out.println(f.exists());

            ftpClient = FTPUtil.getFTPClient(ftpHost,ftpPort, ftpPassword, ftpUserName);
            // 设置PassiveMode传输
            ftpClient.enterLocalPassiveMode();
            // 设置以二进制流的方式传输
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

            in = new FileInputStream(f);
            ftpClient.mkd(ftpPath);
            logger.info(f.getName()+"==========="+f.getPath()+"============"+f.exists());
            fileName = fileName.replace("./","");
            if (newName.equals("")||newName==null)  //如果设置新名称则使用旧名称
                newName = fileName;
            ftpClient.storeFile(ftpPath+"/"+newName, in);
            logger.info("Upload" + newName + "Succeed!");
        } catch (Exception e) {
            throw e;
        }finally{
            try {
                if (in != null){
                    in.close();
                }
                if (f != null){
                    f.delete();
                }
                if (ftpClient != null){
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 把配置文件先写到本地的一个文件中取
     *
     * @param
     * @param
     * @return
     */
    public static boolean write(String fileName, String fileContext,
                         String writeTempFielPath) {
        try {
            logger.info("开始写配置文件");
            File f = new File(fileName);
            if(!f.exists()){
                if(!f.createNewFile()){
                    logger.info("文件不存在，创建失败!");
                }
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
            bw.write(fileContext.replaceAll("\n", "\r\n"));
            bw.flush();
            bw.close();
            return true;
        } catch (Exception e) {
            logger.error("写文件没有成功");
            e.printStackTrace();
            return false;
        }
    }
}

