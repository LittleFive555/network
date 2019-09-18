import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
/*
Socket有以下几个选项
TCP_NODELAY: 表示立即发送数据
SO_RESUSEADDR: 表示是否允许重用Socket所绑定的本地地址
SO_TIMEOUT: 表示接收数据时的等待超时时间
SO_LINGER: 表示当执行Socket的close()方法时，是否立即关闭底层的Socket
SO_SNFBUF: 表示发送数据的缓冲区的大小
SO_KEEPALIVE: 表示对于长时间处于空闲状态的Socket，是否要自动把它关闭
OOBINLINE: 表示是否支持发送一个字节的TCP紧急数据
*/
public class EchoClient {
	
	private String host = "localhost";
	private int port = 8000;
	private Socket socket;
	
	public EchoClient() throws IOException {
		/*
		 * 参数host表示EchoServer进程所在的主机的名字，参数port表示EchoServer进程监听的端口。
		 * 当参数host的取值为“localhost”时，表示EchoClient与EchoServer进程运行在同一个主机上。
		 */
		socket = new Socket(host, port);
		
		
		/*
		 * Socket类还有两个构造方法，允许显式地设置客户端的IP地址和端口
		 *  如果一个主机同时属于两个以上的网络，他就可能拥有两个以上的IP地址（Internet中和局域网中）
		 */	
//		socket = new Socket(InetAddress address, int port, InetAddress localAddr,int localPort) throws IOException;
//		socket = new Socket(String host, int port, InetAddress localAddr, int localPort) throws IOException;
		
		
		/*
		 * 以下方式可以设定客户端等待建立连接的超时时间，
		 * 若限定时间内，连接成功，则connect()方法顺利返回
		 * 限定时间内，出现某种异常，则抛出该异常
		 * 超过限定时间，既没有连接成功，也没有出现其他异常，那么会抛出SocketTimeoutException
		 * 如果timeout设为0，表示永远不会超时
		 */
//		socket = new Socket();
//		SocketAddress remoteAddr = new InetSocketAddress(host, port);
//		socket.connect(remoteAddr, 600);
		
		/*
		 * Socket的构造方法请求连接服务器时，可能会抛出下面的异常（均为IOException的直接或间接子类）：
		 * ···UnknownHostException：如果无法识别主机的名字或IP地址，就会抛出这种异常
		 * ···ConnectException：若果没有服务器进程监听制定的端口，或者服务器进程拒绝连接，就会抛出这种异常
		 * ···SocketTimeoutException：如果等待连接超时，就会抛出这种异常
		 * ···BindException：如果无法把Socket对象与指定的本地IP地址或端口绑定，就会抛出这种异常
		 */
	}
	
	public EchoClient(int somePort) throws IOException {
		socket = new Socket(host, somePort);
	}
	
	private PrintWriter getWriter(Socket socket) throws IOException {
		OutputStream socketOut = socket.getOutputStream();
		return new PrintWriter(socketOut, true);
	}
	
	private BufferedReader getReader(Socket socket) throws IOException {
		InputStream socketIn = socket.getInputStream();
		return new BufferedReader(new InputStreamReader(socketIn));
	}
	
	public void talk() throws IOException {
		try {
			BufferedReader br = getReader(socket);
			PrintWriter pw = getWriter(socket);
			BufferedReader localReader = new BufferedReader(new InputStreamReader(System.in));
			String msg = null;
			while ((msg = localReader.readLine()) != null) {
				pw.println(msg);
				System.out.println(br.readLine());
				if (msg.equals("bye")) {
					break;
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		new EchoClient().talk();
	}
}
