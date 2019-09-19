import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
/* ServerSocket有以下几个选项: 
   SO_TIMEOUT: 表示等待客户连接的超时时间
			serverSocket.setSoTimeout(int mills), 默认为0, 表示永远不会超时
   SO_REUSEADDR: 表示是否允许重用服务器所绑定的地址, 与Socket的相同, 必须在还未绑定到本地端口之前就设置
			serverSocket.setReuseAddress(boolean on)
   SO_RCVBUF: 表示接收数据的缓冲区的大小
			serverSocket.setReceiveBufferSize(int size)
			绑定端口前后设置都有效, 例外情况下是如果要设置大于64KB的缓冲区, 则必须要在绑定端口钱设置才有效.
   PerformancePreferences: 与Socket的相同
 */
public class EchoServer {
	/* 如果port = 0, 表示由操作系统来分配一个任意可用的端口, 也称为匿名端口
	   FTP就使用的是匿名端口.FTP使用两个并行的TCP连接: 一个是控制连接, 一个是数据连接.
			控制连接: 用于在客户和服务器之间发送控制信息, 如用户名和口令, 改变远程目录的命令或上传和下载文件的命令.
			数据连接: 用于传送文件
	   TCP服务器在21端口上监听控制连接, 如果有客户要求上传或下载文件, 就另外建立一个数据连接, 通过它来传送文件.
	 */
	private int port = 8000;
	private ServerSocket serverSocket;
	/*该项一般由操作系统限定为50, 以下三种情况会使用操作系统限定的最大长度:
		backlog > 操作系统限定的队列的最大长度
		backlog <= 0
		在ServerSocket的构造方法中没有设置backlog参数 
	 */
	private int maxConnectNum = 2;
	
	public EchoServer() throws IOException {
		/*
		 * ServerSocket的构造方法负责在操作系统中把当前进程注册为服务器进程。
		 */
		serverSocket = new ServerSocket(port, maxConnectNum);	//监听8000端口，第二个参数 backlog 为连接请求队列的长度
		
		//设定绑定的IP地址(当有两个网卡,一个internnet一个局域网, 如果想让服务器只运行在局域网上, 那么就使用局域网的IP地址)
		// serverSocket = new ServerSocket(port, maxConnectNum, InetAddress.getByName("xxx.xxx.xxx.xxx"));
		
		//默认的构造函数  当想要在绑定前设置ServerSocket一些选项时, 使用默认的构造函数, 例如:
		// serverSocket = new ServerSocket();
		// serverSocket.setReuseAddress(true);
		// serverSocket.bind(new InetSocketAddress(8000));
		
		//如果要确定一个ServerSocket已经与特定端口绑定,并且还没有被关闭,则可以采用以下方式:
		// boolean isOpen = serverSocket.isBound() && !serverSocket.isClosed();
		
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
