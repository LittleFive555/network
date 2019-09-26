import java.io.*;
import java.net.*;

public class AdminClient {
	private static String host = "localhost";
	private static int adminPort = 8001;
	
	
	public static void main(String[] args) {
		
		
		Socket socket = null;
		try {
			socket = new Socket(host, adminPort);
			//发出关闭命令
			OutputStream socketOut = socket.getOutputStream();
			socketOut.write("shutdown\r\n".getBytes());
			
			//接收服务器的反馈
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String msg = null;
			while((msg = br.readLine()) != null) {
				System.out.println(msg);
			}
		} catch (IOException e) {
			//这有问题
			System.out.println("这有问题???");
			e.printStackTrace();
		} finally {
			try {
				if (socket != null)
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}