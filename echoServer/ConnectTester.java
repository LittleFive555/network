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
		} catch(BindException e) {			//如果无法把Socket对象与指定的本地IP地址或端口绑定,例如使用本地不存在的IP地址，或者被占用的端口
			result = "Local address and port can't be binded";
		} catch(UnknownHostException e) {	//无法识别主机的名字或IP地址
			result = "Unknown host";		
		} catch(ConnectException e) {		//没有服务器进程监听指定的端口    或    服务器进程拒绝连接（超出请求连接队列的长度）
			result = "Connection refused";	
		} catch(SocketTimeoutException e) {	//如果等待连接超时，就会抛出这种异常
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
