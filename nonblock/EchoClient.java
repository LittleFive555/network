import java.net.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.nio.*;
import java.io.*;
import java.util.*;

public class EchoClient {
	private SocketChannel socketChannel = null;
	private ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
	private ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
	private Charset charset = Charset.forName("GBK");
	private Selector selector;
	
	private int port = 8000;
	
	public EchoClient() throws IOException {
		socketChannel = SocketChannel.open();
		InetAddress ia = InetAddress.getLocalHost();
		InetSocketAddress isa = new InetSocketAddress(ia, port);
		socketChannel.connect(isa);						//采用阻塞模式连接服务器
		socketChannel.configureBlocking(false);			//设置为非阻塞模式
		System.out.println("与服务器的连接建立成功");
		selector = Selector.open();
	}
	
	public static void main(String[] args) throws IOException {
		final EchoClient client = new EchoClient();
		Thread receiver = new Thread() {			//创建Receiver线程
			public void run() {
				client.receiveFromUser();			//接收用户向控制台输入的数据
			}
		};
		
		receiver.start();
		client.talk();
	}
	
	public void receiveFromUser() {					//接收用户向控制台输入的数据, 把它放到sendBuffer中
		try {
			BufferedReader localReader = new BufferedReader(new InputStreamReader(System.in));
			String msg = null;
			while((msg = localReader.readLine()) != null) {
				synchronized(sendBuffer) {
					sendBuffer.put(encode(msg + "\r\n"));
				}
				if (msg.equals("bye")) {
					break;
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void talk() throws IOException{
		socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		while (selector.select() > 0) {
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
						if (key != null) {
							key.cancel();
							key.channel().close();
						}
					} catch(Exception ex) {
						ex.printStackTrace();
					}
				}		//#catch
			}		//#while
		}		//#while
	}
	
	public void send(SelectionKey key) throws IOException {
		//发送sendBuffer中的数据
		SocketChannel socketChannel = (SocketChannel)key.channel();
		synchronized(sendBuffer) {
			sendBuffer.flip();			//把极限设为位置, 把位置设为零
			socketChannel.write(sendBuffer);
			sendBuffer.compact();		//删除已发送的数据
		}
	}
	
	public void receive(SelectionKey key) throws IOException {
		//接收EchoServer发送的数据, 把它放到receiveBuffer中
		//如果receiveBuffer中有一行数据, 就打印这行数据, 然后把它从receiveBuffer中删除
		SocketChannel receiveChannel = (SocketChannel)key.channel();
		socketChannel.read(receiveBuffer);
		receiveBuffer.flip();
		String receiveData = decode(receiveBuffer);
		
		if (receiveData.indexOf("\n") == -1)
			return;
		
		String outputData = receiveData.substring(0, receiveData.indexOf("\n") + 1);
		System.out.println(outputData);
		if (outputData.equals("echo:bye\r\n") || outputData.equals("echo: bye\r\n")) {
			key.cancel();
			socketChannel.close();
			System.out.println("关闭与服务器的连接");
			selector.close();
			System.exit(0);			//结束程序
		}
		
		ByteBuffer temp = encode(outputData);
		receiveBuffer.position(temp.limit());
		receiveBuffer.compact();				//删除已经打印的数据
	}
	
	public String decode(ByteBuffer buffer) {
		CharBuffer charBuffer = charset.decode(buffer);
		return charBuffer.toString();
	}
	public ByteBuffer encode(String str) {
		return charset.encode(str);
	}
	
}