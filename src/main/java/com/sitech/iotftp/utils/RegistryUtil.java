package com.sitech.iotftp.utils;

import com.baidu.aip.face.AipFace;
import com.sitech.iotftp.Entry.UserImage;
import com.sitech.iotftp.handler.FaceManage;
import com.sitech.iotftp.repositories.UserImageRepository;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.impl.piccolo.io.FileFormatException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.persistence.criteria.CriteriaBuilder;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.util.*;


@SuppressWarnings("all")
@Component
public class RegistryUtil {

    private static final String EXTENSION_XLS = "xls";
    private static final String EXTENSION_XLSX = "xlsx";

    @Autowired
    private UserImageRepository userImageRepository;
    @Autowired
    private FaceManage faceManage;

    @Value("${FTP_HOST}")
    private String ftpHost;
    @Value("${FTP_PORT}")
    private int ftpPort;
    @Value("${FTP_USERNAME}")
    private String ftpUserName;
    @Value("${FTP_PASSWORD}")
    private String ftpPassword;

    @Value("${BASE_URL}")
    private String baseUrl;

    private static AipFace client = new AipFace("16751581", "F4GOyU8RvAmOSG0EgxcTCIK2", "GOQaC8myPTGG0cpMa9yL65MvWUtkEmcf");

    @PostConstruct
    public void init() throws IOException {

        //TODO 1 解析Excel
        Map<String, String> noteIds = new HashMap<>();
        List<UserImage> users = userImageRepository.findAll();
        for (UserImage u : users) {
            noteIds.put(u.getNotesId(), "1");
        }
        List<Map<String, String>> result = readExcel("registry.xlsx", noteIds);
        System.out.println(result.size());

        FTPClient ftpClient = FTPUtil.getFTPClient(ftpHost, ftpPort, ftpPassword, ftpUserName);
        // 设置PassiveMode传输
        ftpClient.enterLocalPassiveMode();
        // 设置以二进制流的方式传输
        ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

        ftpClient.changeWorkingDirectory("faceImages");

        ByteArrayInputStream bis = null;
        HttpURLConnection conn = null;
        InputStream inStream = null;
        int flag = 1;
        //TODO 2 循环插入数据库
        try {
            for (Map<String, String> map : result) {
                try {
                    String base64Str = imageToBase64(map.get("4"));
                    if (base64Str.equals(""))
                        continue;
                    //TODO 1 进行百度接口注册
                    JSONObject contrastResult = faceManage.faceRegistry(client, base64Str, "10000", map.get("1"));
                    if ((int) contrastResult.get("error_code") == 0) {  //注册成功
                        String path = "10000/" + map.get("1") + ".png";
                        //TODO 2 将文件存储到ftp中，生成http地址
                        URL url = new URL(map.get("4"));
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setConnectTimeout(5 * 1000);
                        inStream = conn.getInputStream();// 通过输入流获取图片数据

                        ftpClient.mkd("10000");  //以企业id为准创建目录
                        ftpClient.storeFile(path, inStream);

                        //TODO 3 将图片的网络地址存储到mysql中
                        String imageUrl = baseUrl + path;
                        UserImage userImage = new UserImage(null, map.get("1"), "yx_appid20190718100255", map.get("3"), "10000", "", imageUrl, map.get("2"));
                        userImageRepository.save(userImage);
                        System.out.println("已完成個數："+flag++);
                    }
                } finally {
                    try {
                        if (bis != null) {
                            bis.close();
                        }
                        if (conn != null) {
                            conn.disconnect();
                        }
                        if (inStream != null) {
                            inStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            } catch(IOException e){
                e.printStackTrace();
            } finally{
                try {
                    if (bis != null) {
                        bis.close();
                    }
                    if (ftpClient != null) {
                        ftpClient.disconnect();
                    }
                    if (conn != null) {
                        conn.disconnect();
                    }
                    if (inStream != null) {
                        inStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }


    public static String imageToBase64(String strUrl) {
        ByteArrayOutputStream baos = null;
        HttpURLConnection conn = null;
        InputStream inStream = null;
        BufferedImage image = null;
        BufferedImage resized = null;
        try {
//            String strUrl = "Http://hrs.si-tech.com.cn/orgsFiles/10000/oldPhoto/grzp-cheli-163.gif";
            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5 * 1000);
            inStream = conn.getInputStream();// 通过输入流获取图片数据
            image = ImageIO.read(inStream);
            resized = ImageUtil.resizebyaspect(image, 300, 400);
            baos = new ByteArrayOutputStream();
            ImageIO.write(resized, "png", baos);
        } catch (Exception e) {
            return "";
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
                if (inStream != null) {
                    inStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }


    /***
     * <pre>
     * 取得Workbook对象(xls和xlsx对象不同,不过都是Workbook的实现类)
     *   xls:HSSFWorkbook
     *   xlsx：XSSFWorkbook
     * @param filePath
     * @return
     * @throws IOException
     * </pre>
     */
    private Workbook getWorkbook(String filePath) throws IOException {
        Workbook workbook = null;
        InputStream is = new FileInputStream(filePath);
        if (filePath.endsWith(EXTENSION_XLS)) {
            workbook = new HSSFWorkbook(is);
        } else if (filePath.endsWith(EXTENSION_XLSX)) {
            workbook = new XSSFWorkbook(is);
        }
        return workbook;
    }

    /**
     * 文件检查
     *
     * @param filePath
     * @throws FileNotFoundException
     * @throws FileFormatException
     */
    private void preReadCheck(String filePath) throws FileNotFoundException, FileFormatException {
        // 常规检查
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("传入的文件不存在：" + filePath);
        }

        if (!(filePath.endsWith(EXTENSION_XLS) || filePath.endsWith(EXTENSION_XLSX))) {
            throw new FileFormatException("传入的文件不是excel");
        }
    }

    /**
     * 读取excel文件内容
     *
     * @param filePath
     * @throws FileNotFoundException
     * @throws FileFormatException
     */
    public List<Map<String, String>> readExcel(String filePath, Map<String, String> map) throws FileNotFoundException, FileFormatException {
        // 检查
        this.preReadCheck(filePath);
        // 获取workbook对象
        Workbook workbook = null;
        List<Map<String, String>> list = new ArrayList<>();
        try {
            workbook = this.getWorkbook(filePath);
            // 读文件 一个sheet一个sheet地读取
            for (int numSheet = 0; numSheet < workbook.getNumberOfSheets(); numSheet++) {
                Sheet sheet = workbook.getSheetAt(numSheet);
                if (sheet == null) {
                    continue;
                }
                //输出shell名称
                System.out.println("=======================开始读取" + sheet.getSheetName() + "表头信息=========================");

                int firstRowIndex = sheet.getFirstRowNum();
                int lastRowIndex = sheet.getLastRowNum();

                // 读取首行 即,表头
                Row firstRow = sheet.getRow(firstRowIndex);
                for (int i = firstRow.getFirstCellNum(); i <= firstRow.getLastCellNum(); i++) {
                    Cell cell = firstRow.getCell(i);
                    String cellValue = this.getCellValue(cell, true);
                    System.out.print(" " + cellValue + "\t");
                }
                System.out.println();
                System.out.println("=========================" + sheet.getSheetName() + "表头信息读取完毕=========================");
                System.out.println();

                // 读取数据行
                System.out.println("=======================开始读取" + sheet.getSheetName() + "行信息=========================");
                for (int rowIndex = firstRowIndex + 1; rowIndex <= lastRowIndex; rowIndex++) {
                    Map<String, String> params = null;
                    Row currentRow = sheet.getRow(rowIndex);// 当前行
                    String note_id = this.getCellValue(currentRow.getCell(0), true);
                    if (map.containsKey(note_id)) {
                        continue;
                    } else {
                        params = new LinkedHashMap<>();
                        int firstColumnIndex = currentRow.getFirstCellNum(); // 首列
                        int lastColumnIndex = currentRow.getLastCellNum();// 最后一列
                        int flag = 1;
                        for (int columnIndex = firstColumnIndex; columnIndex <= lastColumnIndex; columnIndex++) {
                            Cell currentCell = currentRow.getCell(columnIndex);// 当前单元格
                            String currentCellValue = this.getCellValue(currentCell, true);// 当前单元格的值
                            if (flag == 3) {
                                currentCellValue = currentCellValue.substring(0, 11);
                            }
                            params.put(String.valueOf(flag), currentCellValue);
                            flag++;
                        }
                        list.add(params);
                    }
                }


                System.out.println("=========================" + sheet.getSheetName() + "行信息读取完毕=========================");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
            }
        }
        return list;
    }

    /**
     * 取单元格的值
     *
     * @param cell       单元格对象
     * @param treatAsStr 为true时，当做文本来取值 (取到的是文本，不会把“1”取成“1.0”)
     * @return
     */
    private String getCellValue(Cell cell, boolean treatAsStr) {
        if (cell == null) {
            return "";
        }

        if (treatAsStr) {
            // 虽然excel中设置的都是文本，但是数字文本还被读错，如“1”取成“1.0”
            // 加上下面这句，临时把它当做文本来读取
            cell.setCellType(Cell.CELL_TYPE_STRING);
        }

        if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        } else {
            return String.valueOf(cell.getStringCellValue());
        }
    }
}
