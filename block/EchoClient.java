import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.io.*;
import java.nio.charset.*;

public class EchoClient {
	private String host = "localhost";
	private int port = 8000;
	private SocketChannel socketChannel = null;
	
	public EchoClient() throws IOException {
		socketChannel = SocketChannel.open();
		//方法1
		//InetAddress用来表示 IP地址, InetSocketAddress用来表示 IP地址 + 端口号
		InetAddress addr = InetAddress.getLocalHost();
		InetSocketAddress isaddr = new InetSocketAddress(addr, port);
		//方法2
		// InetSocketAddress isaddr = new InetSocketAddress(host, port);
		
		//使用connect()方法连接远程服务器, 该方法在阻塞模式下运行时, 等到与远程服务器的连接建立成功才返回
		socketChannel.connect(isaddr);
		System.out.println("与服务器连接成功");
	}
	
	private PrintWriter getWriter(Socket socket) throws IOException{
		OutputStream socketOut = socket.getOutputStream();
		return new PrintWriter(socketOut, true);
	}
	private BufferedReader getReader(Socket socket) throws IOException{
		InputStream socketIn = socket.getInputStream();
		return new BufferedReader(new InputStreamReader(socketIn));
	}
	
	public void talk() throws IOException {
		try {
			BufferedReader br = getReader(socketChannel.socket());
			PrintWriter pw = getWriter(socketChannel.socket());
			BufferedReader localReader = new BufferedReader(new InputStreamReader(System.in));
			
			String msg = null;
			while ((msg = localReader.readLine()) != null) {
				pw.println(msg);
				System.out.println(br.readLine());
				
				if(msg.equals("bye"))
					break;
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socketChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		new EchoClient().talk();
	}
	
	
}