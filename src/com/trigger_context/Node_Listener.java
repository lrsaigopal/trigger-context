package com.trigger_context;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

import android.util.Log;

public class Node_Listener implements Runnable {
	private ArrayList<String> macAddressListActive = new ArrayList<String>();
	public static DatagramSocket datagramSocket = null, replySocket = null;
	private DatagramPacket myPacket;
	private int Port;
	private String mac, name;
	private String data;
	private byte[] byteData;
	private DatagramPacket pkt;

	public Node_Listener(int port, String mac) {
		this.Port = port;
		this.mac = mac;
		String myData = name + ";" + mac;
		try {
			replySocket = new DatagramSocket();
			datagramSocket = new DatagramSocket(Port);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i("Trigger_Log", "Node_Listener--constructor end");
	}

	@Override
	public void run() {
		Log.i("Trigger_Log", "Node_Listener-run--start");
		Main_Service.main_Service.noti("in node listener", "");
		byte[] buf = new byte[256];
		String userData;
		String[] userDataArray;
		DatagramPacket packet = new DatagramPacket(buf, buf.length);

		while (Main_Service.wifi) {
			try {
				datagramSocket.receive(packet);
				userData = new String(packet.getData(), "UTF-8");
				userData = userData.substring(0, packet.getLength());

				Main_Service.main_Service.noti("recvd pkt", userData);

				userDataArray = userData.split(";");// name;MAC;type
				if (!userDataArray[1].equals(Network.getMAC())) {
					String replyType = new String("1".getBytes(), "UTF-8");
					if (!macAddressListActive.contains(userDataArray[1])) {
						macAddressListActive.add(userDataArray[1]);
						// processing - trigger on arrival - any user or saved
						// user
						Main_Service.main_Service.noti("in node lisntr mac",
								"'" + userDataArray[1] + "'");
						Main_Service.main_Service.noti(
								Main_Service.conf_macs.toString(), "ssup!");
						if (Main_Service.conf_macs.contains(userDataArray[1])) {
							Main_Service.main_Service.noti("vj sux",
									"and sux more");
							new Thread(new Process_User(userDataArray[1],
									packet.getAddress())).start();
						} else if (Main_Service.conf_macs
								.contains(Main_Service.ANY_USER)) {
							new Thread(new Process_User(Main_Service.ANY_USER,
									packet.getAddress())).start();
						}
						// ^any user
					}
					if (userDataArray[2].equals(replyType)) {
						Main_Service.main_Service.noti("got a reply 2 msg", "");
						name = (String) Network_Service.ns.getSharedMap(
								Main_Service.MY_DATA).get("name");
						if (name == null) {
							name = Main_Service.DEFAULT_USER_NAME;
						}
						data = name + ";" + mac;
						byteData = data.getBytes();
						pkt = new DatagramPacket(byteData, byteData.length,
								packet.getAddress(), Device_Activity.PORT);

						replySocket.send(pkt);
					}
				}
			} catch (IOException e) {
				if (!datagramSocket.isClosed()) {
					Log.i("Trigger_Log", "Node_Listener-run--Error in receive");
				}
			}
		}
		if (!datagramSocket.isClosed()) {
			datagramSocket.close();
		}
		datagramSocket = null;
		replySocket.close();
		Log.i("Trigger_Log", "Node_Listener-run--ending");
	}
}
