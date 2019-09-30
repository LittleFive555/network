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
			//SocketChannel实际上也提供了read(ByteBuffer buffer), 但用来读取一行字符串比较麻烦, 此为自定义的readLine()方法
			// while((msg = readLine(socketChannel)) != null) {
			//此为BufferedReader的readLine()方法
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
	
	//此为根据socketChannel的read()方法构建的readLine()方法
	public String readLine(SocketChannel socketChannel) throws IOException {
		//存放所有读到的数据, 假定一行字符串对应的字节序列的长度小于1024
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		//存放一次读到的数据, 一次只读一个字节
		ByteBuffer tempBuffer = ByteBuffer.allocate(1);
		boolean isLine = false;						//表示是否读到了一行字符串
		boolean isEnd = false;						//表示是否到达了输入流的末尾
		String data = null;
		while(!isLine && !isEnd) {
			tempBuffer.clear();						//清空缓冲区
			//在阻塞模式下, 只有等读到了1个字节或者读到输入流末尾才返回
			//在非阻塞模式下, 有可能返回零
			int n = socketChannel.read(tempBuffer);
			if (n == -1) {
				isEnd = true;
				break;
			}
			if (n == 0)
				continue;
			tempBuffer.flip();					//把极限设为位置, 把位置设为0
			buffer.put(tempBuffer);				//把tempBuffer中的数据复制到buffer中
			buffer.flip();
			Charset charset = Charset.forName("GBK");
			CharBuffer charBuffer = charset.decode(buffer);		//解码
			data = charBuffer.toString();
			if (data.indexOf("\r\n") != -1) {
				isLine = true;
				data = data.substring(0, data.indexOf("\r\n"));
				break;
			}
			buffer.position(buffer.limit());		//把位置设为极限, 为下次读数据做准备
			buffer.limit(buffer.capacity());		//把极限设为容量, 为下次读数据做准备
		}
		//如果读入了一行字符串, 就返回这行字符串, 不包括行结束符 "\r\n"
		//如果读入了输入流的末尾, 就返回null
		return data;
	}
}