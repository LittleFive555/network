package echoServer;

import java.net.*;
import java.io.*;

public class ConnectTester {
	public static void main(String[] args) {
		String host = "localhost";
		int port = 25;
		if (args.length > 1) {
			host = args[0];
			port = Integer.parseInt(args[1]);
		}
		new ConnectTester().connect(host, port);
	}
	
	public void connect(String host, int port) {
		SocketAddress remoteAddr = new InetSocketAddress(host, port);
		Socket socket = null;
		String result = "";
		try {
			long begin = System.currentTimeMillis();
			socket = new Socket();
			socket.connect(remoteAddr, 1000);
			long end = System.currentTimeMillis();
			result = (end - begin) + "ms";
		} catch(BindException e) {			//����޷���Socket������ָ���ı���IP��ַ��˿ڰ�,����ʹ�ñ��ز����ڵ�IP��ַ�����߱�ռ�õĶ˿�
			result = "Local address and port can't be binded";
		} catch(UnknownHostException e) {	//�޷�ʶ�����������ֻ�IP��ַ
			result = "Unknown host";		
		} catch(ConnectException e) {		//û�з��������̼���ָ���Ķ˿�    ��    ���������ܾ̾����ӣ������������Ӷ��еĳ��ȣ�
			result = "Connection refused";	
		} catch(SocketTimeoutException e) {	//����ȴ����ӳ�ʱ���ͻ��׳������쳣
			result = "Timeout";				
		} catch(IOException e) {
			result = "failure";
		} finally {
			try {
				if (socket != null)
					socket.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(result);
	}
}
