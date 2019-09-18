import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {
	private int port = 8000;
	private ServerSocket serverSocket;
	private int maxConnectNum = 2;
	
	public EchoServer() throws IOException {
		/*
		 * ServerSocket的构造方法负责在操作系统中把当前进程注册为服务器进程。
		 */
		serverSocket = new ServerSocket(port, maxConnectNum);	//监听8000端口，第二个参数为连接请求队列的长度
		System.out.println("服务器启动");
	}
	
	public EchoServer(int somePort) throws IOException {
		serverSocket = new ServerSocket(somePort, maxConnectNum);
		System.out.println("读取服务器启动");
	}
	
	public String echo(String msg) {
		return "Echo: " + msg;
	}
	
	private PrintWriter getWriter(Socket socket) throws IOException {
		OutputStream socketOut = socket.getOutputStream();
		return new PrintWriter(socketOut, true);
	}
	
	private BufferedReader getReader(Socket socket) throws IOException {
		InputStream socketIn = socket.getInputStream();
		return new BufferedReader(new InputStreamReader(socketIn));
	}
	
	public void service() {
		while(true) {
			Socket socket = null;
			try {
				/*
				 * 服务器程序接下来调用ServerSocket对象的accept方法，该方法一直监听端口，等待客户的连接请求，
				 * 如果接收到一个连接请求，accept()方法就会返回一个Socket对象，这个Socket对象与客户端的Socket对象形成了一条通信线路
				 */
				socket = serverSocket.accept();		//等待客户的连接请求
				System.out.println("New connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
				BufferedReader br = getReader(socket);
				PrintWriter pw = getWriter(socket);
				
				String msg = null;
				while ((msg = br.readLine()) != null) {
					System.out.println(msg);
					pw.println(echo(msg));
					if (msg.equals("bye")) {
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (socket != null)	socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static void main(String[] args) throws IOException {
		new EchoServer().service();
	}
}
