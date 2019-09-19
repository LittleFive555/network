import java.net.*;
import java.io.*;

class Client {
	
	public static void main(String[] args) throws Exception {
		final int length = 100;
		String host = "localhost";
		int port = 8000;
		
		Socket[] sockets = new Socket[length];
		for (int i = 0; i < length; i++) {							//尝试建立100次连接
			sockets[i] = new Socket(host, port);
			System.out.println("第" + (i+1) + "次连接建立成功");
		}
		Thread.sleep(30000);
		for (int i = 0; i < length; i++) {
			sockets[i].close();
		}
	}
}