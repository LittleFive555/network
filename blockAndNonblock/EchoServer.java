import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

public class EchoServer {
	private int port = 8000;
	private Selector selector = null;
	private ServerSocketChannel serverSocketChannel = null;
	private Charset charset = Charset.forName("GBK");
	
	public EchoServer() throws IOException {
		selector = Selector.open();
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.socket().setReuseAddress(true);
		serverSocketChannel.socket().bind(new InetSocketAddress(port));
		System.out.println("服务器启动");
	}
	
	private Object gate = new Object();
	
	public void accept() {
		while (true) {
			try {
				SocketChannel socketChannel = serverSocketChannel.accept();
				System.out.println("接收到客户连接, 来自: " + socketChannel.socket().getInetAddress() + ":"
							+ socketChannel.socket().getPort());
				socketChannel.configureBlocking(false);
				
				ByteBuffer buffer = ByteBuffer.allocate(1024);
				//同步代码块
				synchronized(gate) {			//Accept线程执行这个同步代码块
					selector.wakeup();
					socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, buffer);
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void service() throws IOException {
		while (true) {
			//一个空的同步代码块, 其作用是为了让主线程等待Accept线程执行完同步代码块
			synchronized(gate) {}				//主线程执行
			int n = selector.select();
			
			if (n == 0) continue;
			Set readyKeys = selector.selectedKeys();
			Iterator it = readyKeys.iterator();
			while (it.hasNext()) {
				SelectionKey key = null;
				try {
					key = (SelectionKey)it.next();
					it.remove();
					if (key.isReadable()) {
						receive(key);
					}
					if (key.isWritable()) {
						send(key);
					}
				} catch(IOException e) {
					e.printStackTrace();
					try {
						if (key != null ) {
							key.cancel();
							key.channel().close();
						}
					} catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}
	
	public void send(SelectionKey key) throws IOException {
		//获得附件
		ByteBuffer buffer = (ByteBuffer)key.attachment();
		//获得socketChannel
		SocketChannel socketChannel = (SocketChannel)key.channel();
		buffer.flip();				//把极限设为位置, 把位置设为0
		//按照GBK编码, 把buffer中的字节转换为字符串
		String data = decode(buffer);
		
		//如果还没有一行数据, 就返回
		if (data.indexOf("\r\n") == -1) 
			return;
		
		//截取一行数据
		String outputData = data.substring(0, data.indexOf("\n") + 1);
		System.out.println(outputData);
		//把输出的字符串按照GBK编码, 转换为字节, 放在outputBuffer中
		ByteBuffer outputBuffer = encode("echo: " + outputData);
		//输出outputBuffer中所有字节
		while (outputBuffer.hasRemaining())			//发送一行字符串
			socketChannel.write(outputBuffer);
		
		//把outputData字符串按照GBK编码, 转换为字节, 放在ByteBuffer中
		ByteBuffer temp = encode(outputData);
		//把buffer的位置设为temp的极限
		buffer.position(temp.limit());
		//删除已经处理的字符串
		buffer.compact();							
		
		//如果已经输出了字符串"bye\r\n", 就使SelectionKey失效, 并关闭SocketChannel
		if(outputData.equals("bye\r\n")) {
			key.cancel();
			socketChannel.close();
			System.out.println("关闭与客户的连接");
		}
	}
	
	public void receive(SelectionKey key) throws IOException {
		ByteBuffer buffer = (ByteBuffer)key.attachment();
		
		SocketChannel socketChannel = (SocketChannel)key.channel();
		ByteBuffer readBuff = ByteBuffer.allocate(32);
		socketChannel.read(readBuff);
		readBuff.flip();
		
		//把buffer的极限设为容量
		buffer.limit(buffer.capacity());
		//把读到的数据放到buffer中
		//假定buffer的容量足够大, 不会出现缓冲区溢出异常
		buffer.put(readBuff);
	}
	
	public String decode(ByteBuffer buffer) {				//解码
		CharBuffer charBuffer = charset.decode(buffer);
		return charBuffer.toString();
	}
	
	public ByteBuffer encode(String str) {
		return charset.encode(str);							//编码
	}
	
	public static void main(String[] args) throws IOException{
		final EchoServer server = new EchoServer();
		Thread accept = new Thread() {
			public void run() {
				server.accept();
			}
		};
		accept.start();
		server.service();
	}
}