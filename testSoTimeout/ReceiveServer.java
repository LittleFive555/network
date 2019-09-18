import java.io.*;
import java.net.*;

public class ReceiveServer {
	public static void main(String[] args) throws IOException{
		ServerSocket serverSocket = new ServerSocket(8000);
		Socket s = serverSocket.accept();
		/*
		设置了等待超时时间，超过这一时间后就抛出SocketTimeoutException
		但不会终止Socket或终止进程，由此达到的效果是：
			每个间隔时间后，如果该间隔时间内没有等待到发送方发送数据，就抛出一次SocketTimeoutException异常，
			直到<接收到数据>或<通过异常处理跳出>
		*/
		s.setSoTimeout(20000);
		
		InputStream in = s.getInputStream();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		int len = -1;
		//测试git如何管理子文件夹中的文件
		do {
			try {
				len = in.read(buff);
				if (len != -1)
					buffer.write(buff, 0, len);
			} catch (SocketTimeoutException e) {
				System.out.println("Timeout!");
				len = 0;
			}
		} while(len != -1);
		System.out.println(new String(buffer.toByteArray()));
	}
}