package com.DeviceTest;

import android.view.KeyEvent;

import com.mediatek.xlog.Xlog;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Client socket, used to connect with GPS server and communicate with PMTK
 * command
 */
public class ClientSocket {

	private static final int MASK_8_BIT = 0xFF;
	private static final int SOCKET_TIME_OUT_TIME = 2000;
	private static final int LINE_OUT_SIZE = 10;
	private static final int BUFFER_SIZE = 1024;
	private static final String TAG = "YGPS/ClientSocket";
	private static final int SERVER_PORT = 7000;
	private Socket mClientSocket = null;
	private DataInputStream mDataInput = null;
	private DataOutputStream mDataOutput = null;
	private String mCommand = null;
	private String mResponse = null;
	private BlockingQueue<String> mCommandQueue = null;
	private YgpsActivity mCallBack = null;
	private Thread mSendThread = null;
	private byte[] mDataBuffer = null;

	/**
	 * Constructor, initial parameters and start thread to send/receive
	 * 
	 * @param callBack
	 *            Callback when message received
	 */
	public ClientSocket(YgpsActivity callBack) {
		Xlog.v(TAG, "ClientSocket constructor");
		this.mCallBack = callBack;
		mCommandQueue = new LinkedBlockingQueue<String>();
		mDataBuffer = new byte[BUFFER_SIZE];
		mSendThread = new Thread(new Runnable() {

			public void run() {
				while (true) {
					try {
						mCommand = mCommandQueue.take();
						Xlog.v(TAG, "Queue take command:" + mCommand);
					} catch (InterruptedException ie) {
						Xlog.w(TAG,
								"Take command interrupted:" + ie.getMessage());
						return;
					}
					openClient();
					if (null != mDataOutput && null != mDataInput) {
						try {
							// in = new
							// DataInputStream(localSocket.getInputStream());
							mDataOutput.writeBytes(mCommand);
							mDataOutput.write('\r');
							mDataOutput.write('\n');
							mDataOutput.flush();
							String result = null;
							int line = 0;
							int count = -1;
							while ((count = mDataInput.read(mDataBuffer)) != -1) {
								line++;
								result = new String(mDataBuffer, 0, count);
								Xlog.v(TAG, "line: " + line + " sentence: "
										+ result);
								if (result.contains("PMTK")) {
									mResponse = result;
									Xlog.v(TAG, "Get response from MNL: "
											+ result);
									break;
								}
								if (line > LINE_OUT_SIZE) {
									mResponse = "TIMEOUT";
									break;
								}
							}
						} catch (IOException e) {
							Xlog.w(TAG,
									"sendCommand IOException: "
											+ e.getMessage());
							mResponse = "ERROR";
						}
					} else {
						Xlog.d(TAG, "out is null");
						mResponse = "ERROR";
					}
					if (null != ClientSocket.this.mCallBack) {
						ClientSocket.this.mCallBack.onResponse(mResponse);
					}
					mCommand = null;
					closeClient();
				}
			}
		});
		mSendThread.start();
	}

	/**
	 * Start client socket and connect with server
	 */
	private void openClient() {
		Xlog.v(TAG, "enter startClient");
		if (null != mClientSocket && mClientSocket.isConnected()) {
			Xlog.d(TAG, "localSocket has started, return");
			return;
		}
		try {
			mClientSocket = new Socket("127.0.0.1", SERVER_PORT);
			mClientSocket.setSoTimeout(SOCKET_TIME_OUT_TIME);
			mDataOutput = new DataOutputStream(mClientSocket.getOutputStream());
			mDataInput = new DataInputStream(mClientSocket.getInputStream());
		} catch (UnknownHostException e) {
			Xlog.w(TAG, e.getMessage());
		} catch (IOException e) {
			Xlog.w(TAG, e.getMessage());
		}
	}

	/**
	 * Stop client socket and disconnect with server
	 */
	private void closeClient() {
		Xlog.v(TAG, "enter closeClient");
		try {
			if (null != mDataInput) {
				mDataInput.close();
			}
			if (null != mDataOutput) {
				mDataOutput.close();
			}
			if (null != mClientSocket) {
				mClientSocket.close();
			}
		} catch (IOException e) {
			Xlog.w(TAG, "closeClient IOException: " + e.getMessage());
		} finally {
			mClientSocket = null;
			mDataInput = null;
			mDataOutput = null;
		}
	}

	/**
	 * Finalise communicate with socket server
	 */
	public void endClient() {
		Xlog.v(TAG, "enter endClient");
		mSendThread.interrupt();
		Xlog.v(TAG, "Queue remaining:" + mCommandQueue.size());
		closeClient();
		mCallBack = null;
	}

	/**
	 * Send command to socket server
	 * 
	 * @param command
	 *            Command need to send
	 */
	public void sendCommand(String command) {
		Xlog.v(TAG, "enter sendCommand");
		String sendComm = "$" + command + "*" + calcCS(command);
		Xlog.v(TAG, "Send command: " + sendComm);
		if (!mSendThread.isAlive()) {
			Xlog.v(TAG, "sendThread is not alive");
			mSendThread.start();
		}
		if (command.equals(sendComm) || mCommandQueue.contains(sendComm)) {
			Xlog.v(TAG, "send command return because of hasn't handle the same");
			return;
		}
		try {
			mCommandQueue.put(sendComm);
		} catch (InterruptedException ie) {
			Xlog.w(TAG, "send command interrupted:" + ie.getMessage());
		}
	}

	/**
	 * Calculate check sum for PMTK command
	 * 
	 * @param command
	 *            The command need to send
	 * @return The check sum string
	 */
	private String calcCS(String command) {
		if (null == command || "".equals(command)) {
			return "";
		}
		byte[] ba = command.toUpperCase().getBytes();
		int temp = 0;
		for (byte b : ba) {
			temp ^= b;
		}
		return String.format("%1$02x", temp & MASK_8_BIT).toUpperCase();
	}

}
