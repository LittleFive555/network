import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/*	当ServerSocketChannel 与 SocketChannel 采用默认的阻塞模式时, 为了同时处理多个客户的连接, 必须使用多线程.
 */


public class EchoServer {
	private int port = 8000;
	private ServerSocketChannel serverSocketChannel = null;
	private ExecutorService executorService;
	private static final int POOL_MULTIPLE = 4;
	
	public EchoServer() throws IOException {
		//创建一个线程池
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_MULTIPLE);
		//创建一个ServerSocketChannel对象
		serverSocketChannel = ServerSocketChannel.open();
		//使得在同一个主机上关闭了服务器程序, 紧接着再启动该服务器程序时, 可以顺利绑定相同的端口
		serverSocketChannel.socket().setReuseAddress(true);
		//把服务器进程和一个本地端口绑定
		serverSocketChannel.socket().bind(new InetSocketAddress(port));
		System.out.println("服务器启动");
	}
	
	public void service() {
		while(true) {
			SocketChannel socketChannel = null;
			try {
				socketChannel = serverSocketChannel.accept();
				executorService.execute(new Handler(socketChannel));
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws IOException{
		new EchoServer().service();
	}
}

class Handler implements Runnable{
	private SocketChannel socketChannel;
	
	public Handler(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}
	
	public void run() {
		handle(socketChannel);
	}
	
	public void handle(SocketChannel socketChannel) {
		try {
			Socket socket = socketChannel.socket();
			System.out.println("接收到客户连接, 来自: " + socket.getInetAddress() + ":" + socket.getPort());
			
			BufferedReader br = getReader(socket);
			PrintWriter pw = getWriter(socket);
			
			String msg = null;
			while((msg = br.readLine()) != null) {
				System.out.println(msg);
				pw.println(echo(msg));
				if (msg.equals("bye"))
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (socketChannel != null)
					socketChannel.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private BufferedReader getReader(Socket socket) throws IOException{
		InputStream socketIn = socket.getInputStream();
		return new BufferedReader(new InputStreamReader(socketIn));
	}
	private PrintWriter getWriter(Socket socket) throws IOException{
		OutputStream socketOut = socket.getOutputStream();
		return new PrintWriter(socketOut, true);
	}
	private String echo(String msg) {
		return "Echo:" + msg;
	}
}