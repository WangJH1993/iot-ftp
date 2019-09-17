/*******************************************************************************
 * Copyright 2017 Dell Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @microservice:  export-distro
 * @author: Jim White, Dell
 * @version: 1.0.0
 *******************************************************************************/
package com.sitech.iotftp.messaging.impl;

import com.baidu.aip.face.AipFace;
import com.sitech.iotftp.data.UserImageCountListData;
import com.sitech.iotftp.handler.ProcessMessage;
import com.sitech.iotftp.handler.ProcessMessageHandler;
import com.sitech.iotftp.messaging.EventSubscriber;
import com.sitech.iotftp.repositories.DeviceInfoRepository;
import com.sitech.iotftp.utils.DataUtil;
import com.sitech.iotftp.webSocket.WebSocketServer;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zeromq.ZMQ;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
@Component
public class ZeroMQEventSubscriber implements EventSubscriber {

	private final static Logger logger = Logger.getLogger(ZeroMQEventSubscriber.class);

	@Autowired
	private ProcessMessage processMessage;
	@Autowired
	private WebSocketServer webSocketServer;
	@Autowired
	private DeviceInfoRepository deviceInfoRepository;
	@Autowired
	private ProcessMessageHandler processMessageHandler;

	private static int allSendMessage;
	private static int successSendMessage;

	@Value("${zeromq.port}")
	private String zeromqAddressPort;
	@Value("${zeromq.host}")
	private String zeromqAddress;
	@Value("${BASE_URL}")
	private String baseUrl;
	@Value("${engineFactory}")
	private String engineFactory;

	// 初始化一个AipFace
	private static AipFace client = new AipFace("16751581", "F4GOyU8RvAmOSG0EgxcTCIK2", "GOQaC8myPTGG0cpMa9yL65MvWUtkEmcf");

	private ZMQ.Socket subscriber;
	private ZMQ.Context context;

	{
		context = ZMQ.context(1);
	}

	@Override
	public void receive() {
		getSubscriber();
		byte[] raw;
		try {
			while (!Thread.currentThread().isInterrupted()) {
				raw = subscriber.recv();
				Map params = DataUtil.jsonToMap(new String(raw));

				//根据设备ID查询人员底库名称（企业ID）
				String checkFlag = String.valueOf(params.get("deviceId")).split("_")[0];  //标识签到或者签退
				String deviceId = String.valueOf(params.get("deviceId")).split("_")[1];  //设备唯一标识
				Map<String,String> officeMap = deviceInfoRepository.findCropIDById(deviceId);
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				Date date = new Date();
				//下班之前 出门时 不用调用扫脸 只需要调用开门接口
				Map doorDeviceMap = deviceInfoRepository.findProductKeyAndDeviceNameById(deviceId);
				if(sdf.parse(officeMap.get("end_time")).after(sdf.parse(sdf.format(date))) && checkFlag.equals("out")){
					processMessageHandler.openDoorThroughIot(doorDeviceMap.get("product_key")+"",doorDeviceMap.get("device_name")+"");
				} else {
					System.out.println("调用百度消息量： "+ ++allSendMessage);
					System.out.println("start BAIDU API "+ System.currentTimeMillis());
					if (engineFactory.equals("BD")){
						//TODO 5.1 调用人脸识别接口
						// 可选：设置网络连接参数
						client.setConnectionTimeoutInMillis(2000);
						client.setSocketTimeoutInMillis(60000);

						// 传入可选参数调用接口
						HashMap<String, String> options = new HashMap<String, String>();
						options.put("max_face_num", "1");  //最大检测照片中几张人脸   最大可以设置为10
						options.put("match_threshold", "60");  //匹配阈值（设置阈值后，score低于此阈值的用户信息将不会返回） 最大100 最小0 默认80 此阈值设置得越高，检索速度将会越快，推荐使用默认阈值80
						options.put("quality_control", "NORMAL");  //图片质量控制 NONE: 不进行控制 LOW:较低的质量要求 NORMAL: 一般的质量要求 HIGH: 较高的质量要求 默认 NONE
						options.put("liveness_control", "LOW");  //活体检测控制 NONE: 不进行控制 LOW:较低的活体要求(高通过率 低攻击拒绝率) NORMAL: 一般的活体要求(平衡的攻击拒绝率, 通过率) HIGH: 较高的活体要求(高攻击拒绝率 低通过率) 默认NONE
						options.put("max_user_num", "1");  //查找后返回的用户数量。返回相似度最高的几个用户，默认为1，最多返回50个。

						String groupIdList = String.valueOf(officeMap.get("corp_id"));  //corp_id即企groupId

						// 调用接口
						String image = params.get("image")+"";
						String imageType = "BASE64";

						// 人脸搜索
						JSONObject res = client.search(image, imageType, groupIdList, options);
						res.put("currentImage",image);  //抓拍照片
						res.put("checkFlag",checkFlag);
						res.put("deviceId",deviceId);
						res.put("officeMap",officeMap);
						res.put("doorDeviceMap",doorDeviceMap);
						System.out.println("end BAIDU API "+ System.currentTimeMillis());
						//TODO 5.2  //如果返回值为0证明有匹配结果，并且相似度大于80
						System.out.println(res.toString());
						if ((int)res.get("error_code") == 0){
							System.out.println("成功调用百度消息量： "+ ++successSendMessage);
							processMessage.process(res);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("ZMQ的消息处理失败： " + e.getMessage());
		}
		if (subscriber != null)
			subscriber.close();
		subscriber = null;
		// 重启
		receive();
	}

	public String getZeromqAddress() {
		return zeromqAddress;
	}

	public void setZeromqAddress(String zeromqAddress) {
		this.zeromqAddress = zeromqAddress;
	}

	public String getZeromqAddressPort() {
		return zeromqAddressPort;
	}

	public void setZeromqAddressPort(String zeromqAddressPort) {
		this.zeromqAddressPort = zeromqAddressPort;
	}

	private ZMQ.Socket getSubscriber() {
		if (subscriber == null) {
			try {
				subscriber = context.socket(ZMQ.SUB);
				subscriber.connect(zeromqAddress + ":" + zeromqAddressPort);
				subscriber.subscribe("".getBytes());
			} catch (Exception e) {
				logger.error("Unable to get a ZMQ subscriber.  Error:  " + e);
				subscriber = null;
			}
		}
		return subscriber;
	}

}
