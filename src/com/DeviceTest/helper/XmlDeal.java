package com.DeviceTest.helper;

import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.res.Resources;

public class XmlDeal {

	private static final String TAG = "XmlDeal";
	/**/
	private static final String XML_ROOT_TAG = "TestCaseList";
	private static final String XML_NODE_TAG = "TestCase";

	private static final String CLASS_NAME_TAG = "class_name";
	private static final String TEST_NAME_TAG = "test_name";
	private static final String RESULT_TAG = "result";
	private static final String TEST_GROUP_TAG = "test_group";
	private static final String TEST_FIRST = "first_test";

	public List<TestCase> mTestCases = null;
	public Map<String, List<TestCase>> mCaseGroups = null;

	public XmlDeal(InputStream is) {
		mTestCases = new ArrayList<TestCase>();
		mCaseGroups = new HashMap<String, List<TestCase>>();
		if (!ParseXml(is)) {
			throw new RuntimeException();
		}
	}

	private boolean ParseXml(InputStream is) {

		DocumentBuilderFactory docBuilderFactory = null;
		DocumentBuilder docBuilder = null;
		Document doc = null;
		try {
			docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docBuilderFactory.newDocumentBuilder();

			doc = docBuilder.parse(is);
			NodeList nodeList = doc.getElementsByTagName(XML_ROOT_TAG);

			int length = nodeList.getLength();
			List<TestCase> caseGroup = null;
			for (int i = 0; i < length; i++) {
				Node item = nodeList.item(i);

				int testNo = 0;
				caseGroup = null;
				for (Node node = item.getFirstChild(); node != null; node = node
						.getNextSibling()) {
					if (node.getNodeType() == Node.ELEMENT_NODE) {

						String testName = null;
						String className = null;
						boolean isfirsttest = false;
						for (int j = 0; j < node.getAttributes().getLength(); j++) {
							String attrValue = node.getAttributes().item(j)
									.getNodeValue();
							String attrName = node.getAttributes().item(j)
									.getNodeName();
							if (attrName.equals(CLASS_NAME_TAG)) {
								className = attrValue;
							} else if (attrName.equals(TEST_GROUP_TAG)) {
								caseGroup = mCaseGroups.get(attrValue);
								if (caseGroup == null) {
									caseGroup = new ArrayList<TestCase>();
									mCaseGroups.put(attrValue, caseGroup);
								}
							} else if (attrName.equals(TEST_FIRST)) {
								isfirsttest = true;
							}
						}
						testName = node.getFirstChild().getNodeValue();
						Log.i(TAG, "-----getTestItemName:" + testName
								+ "    isfirsttest = " + isfirsttest);

						if (Resources.getSystem().getConfiguration().locale
								.getCountry().equals("CN")) {
							if (testName.equals("Version")) {
								testName = "版本";
							} else if (testName.equals("LCD")) {
								testName = "屏幕";
							} else if (testName.equals("Touch")) {
								testName = "触屏";
							} else if (testName.equals("Camera")) {
								testName = "摄像头";
							} else if (testName.equals("Speaker")) {
								testName = "喇叭";
							} else if (testName.equals("Gsensor")) {
								testName = "重力感应";
							}

							else if (testName.equals("Bluetooth")) {
								testName = "蓝牙";
							} else if (testName.equals("Wifi")) {
								testName = "无线";
							} else if (testName.equals("MIC")) {
								testName = "录音";
							} else if (testName.equals("Battery")) {
								testName = "电池";
							} else if (testName.equals("FMTransmit")) {
								testName = "FM发射";
							} else if (testName.equals("SD Card")) {
								testName = "SD 卡";
							} else if (testName.equals("Keyboard")) {
								testName = "键盘";
							} else if (testName.equals("Brightness")) {
								testName = "亮度";
							} else if (testName.equals("UsbHost")) {
								testName = "USB 主机";
							} else if (testName.equals("HDMI")) {
								// testName = "°æ±¾"
							} else if (testName.equals("Storage")) {
								testName = "存储";
							} else if (testName.equals("Msensor")) {
								testName = "磁场感应";
							} else if (testName.equals("Lightsensor")) {
								testName = "光感";
							} else if (testName.equals("Gyroscope")) {
								testName = "陀螺仪";
							} else if (testName.equals("VideoPlayer")) {
								testName = "视频播放";
							}
							// add by zzp
							else if (testName.equals("Vibration")) {
								testName = "震动";
							} else if (testName.equals("GPS Location")) {
								testName = "GPS 定位";
							} else if (testName.equals("Headset MIC")) {
								testName = "耳机录音";
							} else if (testName.equals("Sim Card")) {
								testName = "SIM 卡";
							} else if (testName.equals("FM Radio")) {
								testName = "收音机";
							} else if (testName.equals("Signal Status")) {
								testName = "信号状态";
							} else if (testName.equals("Proximity Sensor")) {
								testName = "距离传感器";
							}

						}

						TestCase testCase = new TestCase(testNo, testName,
								className);
						// testCase.setneedtest(isfirsttes);
						mTestCases.add(testCase);
						if (caseGroup != null) {
							caseGroup.add(testCase);
						}
						testNo++;
					}
				}
			}

		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		} finally {
			doc = null;
			docBuilder = null;
			docBuilderFactory = null;
		}

		if (mTestCases.size() == 0) {
			return false;
		}

		Log.i(TAG, "The cases count is :" + mTestCases.size());
		return true;
	}

}
