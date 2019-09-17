package com.sitech.iotftp.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Base64;
import java.util.UUID;

/**
 * @描述:base64和图片互转功能
 * @作者:wangjn_bj@si-tech.com.cn
 * @创建时间：2019年3月18日 下午12:26:55 
 */
public class ImageBase64Handler {

	Logger logger = LoggerFactory.getLogger(ImageBase64Handler.class);


	/** 
	 * @描述:图片转base64字符串
	 * @参数: @param imgFile 图片路径
	 * @参数： @return base64码
	 * @返回类型:String   
	 */
	public static String imageToBase64Str(InputStream inputStream) {
		byte[] data = null;
		try {
			data = new byte[inputStream.available()];
			inputStream.read(data);
			inputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream!=null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return Base64.getEncoder().encodeToString(data);
	}
	/** 
	 * @描述: base64编码字符串转换为图片
	 * @参数 @param imgStr base64编码字符串
	 * @参数 @param path 图片路径
	 * @参数 @return  
	 * @返回类型:boolean   
	 */
	public static boolean base64StrToImage(byte[] bb, String path) {
//	    if (imgStr == null)
//	    return false;
	    try {
//	      byte[] b = Base64.getDecoder().decode(imgStr);
	      for (int i = 0; i < bb.length; ++i) {
	        if (bb[i] < 0) {
	          bb[i] += 256;
	        }
	      }
	      //文件夹不存在则自动创建
	      File tempFile = new File(path);
	      if (!tempFile.getParentFile().exists()) {
	        tempFile.getParentFile().mkdirs();
	      }
	      OutputStream out = new FileOutputStream(tempFile);
	      out.write(bb);
	      out.flush();
	      out.close();
	      return true;
	    } catch (Exception e) {
	      e.printStackTrace();
	      return false;
	    }
	  }
	
	
	 public static String base64ToImg(String src) throws IOException {
	        String uuid = UUID.randomUUID().toString();
	        StringBuilder newPath = new StringBuilder("xx");
	        newPath.append("xx").
	                append(uuid).
	                append("xx");
	        if (src == null) {
	            return null;
	        }
	        byte[] data = null;
	        OutputStream out = null;
	        Base64.Decoder decoder = Base64.getDecoder();
	        try {
	            out = new FileOutputStream(newPath.toString());
	            data = decoder.decode(src);
	            out.write(data);
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (out != null) {
	                out.close();
	            }
	        }
	        return newPath.toString();
	    }
	public static void main(String[] args) throws IOException {
//		String imagepath= "D:/workspace/python_workspace/faceai/img/show-meinv.png";
//		String imagepath= "C:\\Users\\christ\\Desktop\\AI产品线\\格灵深瞳\\素材库/高圆圆03.jpg";
		String imagepath= "D:\\images\\1557402383783.png";
//		
//		String coder1 = ImageBase64Handler.imageToBase64Str(imagepath);
//		System.out.println(coder1);
		String imgStr = "";
//		base64StrToImage(imgStr, "C:/install/eclipse-workspace/test.png");
//		base64ToImg(imgStr);
//		byte[] decode = Base64.getDecoder().decode(str);
//		System.out.println(new String(decode));
	}
}
