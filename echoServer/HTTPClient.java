import java.net.*;
import java.io.*;

public class HTTPClient {
	String host = "www.javathinker.org";
	int port = 80;
	Socket socket;
	
	public void createSocket() throws Exception {
		socket = new Socket(host, port);
	}
	
	public void communicate() throws Exception {
		StringBuffer sb = new StringBuffer("GET" + "/index.jsp" + " HTTP/1.1\r\n");
		sb.append("Host:www.javathinker.org\r\n");
		sb.append("Accept:*/*\r\n");
		sb.append("Accept-Language:zh-cn\r\n");
		sb.append("Accept-Encoding:gzip, defalte\r\n");
		sb.append("User-Agent:Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)\r\n");
		sb.append("Connection: Keep-Alive\r\n\r\n");
		
		//发出HTTP请求
		OutputStream socketOut = socket.getOutputStream();
		//在发送数据时，先把字符串形式的请求信息转换为字节数组（即字符串的编码），然后再发送
		socketOut.write(sb.toString().getBytes());
		socket.shutdownOutput();			//关闭输出流
		
		//接受响应结果
		InputStream socketIn = socket.getInputStream();
		//在接收数据时，把接收到的字节写到一个ByteArrayOutputStream中，它具有一个容量能够自动增长的缓冲区。
		//如果socketIn.read(buff)方法返回-1，则表示读到了输入流的末尾
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		int len = -1;
		while((len=socketIn.read(buff)) != -1) {
			buffer.write(buff, 0, len);
		}
		
		System.out.println(new String(buffer.toByteArray()));			//把字节数组转换为字符串
		
		/*如果接受到的网页数据量很大，先把这些数据全部保存在ByteArrayOutputStream的缓存中不是很明智的做法。
		因为这些数据会占用大量内存，更有效的做法是，利用BufferedReader来逐行读取网页数据*/
		// InputStream socketIn = socket.getInputStream();
		// BufferedReader br = new BufferedReader(new InputStreamReader(socketIn));
		// String data;
		// while ((data = br.readLine()) != null) {
			// System.out.println(data);
		// }
		
		socket.close();
	}
	
	public static void main(String[] args) throws Exception {
		HTTPClient client = new HTTPClient();
		client.createSocket();
		client.communicate();
	}
}