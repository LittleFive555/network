import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class EchoServer {
	/* 
	   一个Selector对象中会有3中类型的SelectionKey集合:
	   all-keys: 当前所有向Selector注册的SelectionKey的集合, Selector的 keys() 方法返回该集合
	   selected-keys: 相关事件已经被Selector捕获的SelectionKey的集合, selectedKeys() 方法返回该集合
	   cancelled-keys: 已经被取消的SelectionKey的集合, 没有提供访问这种集合的方法
	   
	   · 当执行SelectableChannel的 register() 方法时, 该方法新建一个SelectionKey, 并把它加入Selector的all-keys集合中
	   · 如果关闭了与SelectionKey对象关联的Channel对象, 或者调用了SelectionKey对象的cancel()方法, 
	     这个SelectionKey就被加入到Selector的cancelled-keys集合中, 
		 在程序下一次执行Selector的select()方法时, 被取消的SelectionKey对象从所有的集合中删除
	   · 在执行select()方法时, 如果与SelectionKey相关的事件发生了, 这个SelectionKey就被加入到selected-keys集合中, 
	     程序直接调用selected-keys集合的remove()方法, 或者调用它的Iterator的remove()方法, 都可以从selected-keys集合中删除一个SelectionKey对象
	   
	   Selector类的主要方法:
	   Selector open() throws IOException: 静态工厂方法, 创建一个Selector对象
	   boolean isOpen(): 判断Selector是否处于打开状态. Selector对象创建后就处于打开状态, 当调用它的close()方法后, 它就进入关闭状态
	   Set<SelectionKey> keys(): 返回Selector的all-keys集合
	   int selectNow() throws IOException: 返回相关事件已经发生的SelectionKey对象的数目. 该方法采用非阻塞的工作方式, 如果没有, 就立即返回0
	   int select() throws IOException
	   int select(long timeout) throws IOException:该方法采用阻塞的工作方式, 返回相关事件已经发生的SelectionKey对象的数目, 
													如果没有就进入阻塞状态, 直到出现以下情况之一, 才从select()方法中返回
				至少有一个SelectionKey的相关事件已经发生
				其他线程调用了Selector的wakeup()方法, 导致执行select()方法的线程立即从select()方法中返回
				当前执行select()方法的线程被其他线程中断
				超出了等待时间timeout. 如果超时, 就会正常返回, 但不会抛出超时异常
	   Selector wakeup(): 唤醒执行 Selector的select()方法的线程, 执行一次wakeup()只能唤醒一次
	   void close() throws IOException: 关闭Selector. 如果有其他线程正执行这个Selector的select()方法并且处于阻塞状态, 那么这个线程会立即返回.
										使Selector占用的所有资源都被释放, 所有与Selector关联的SelectionKey都被取消
	 */
	private Selector selector = null;
	private ServerSocketChannel serverSocketChannel = null;
	private int port = 8000;
	private Charset charset = Charset.forName("GBK");
	
	public EchoServer() throws IOException {
		//创建一个Selector对象
		selector = Selector.open();
		//创建一个ServerSocketChannel对象
		serverSocketChannel = ServerSocketChannel.open();
		//使得在同一个主机上关闭了服务器程序, 紧接着再启动该服务器程序时, 可以顺利绑定到相同的端口
		serverSocketChannel.socket().setReuseAddress(true);
		//使ServerSocketChannel工作于非阻塞模式
		serverSocketChannel.configureBlocking(false);
		//把服务器进程与一个本地端口绑定
		serverSocketChannel.socket().bind(new InetSocketAddress(port));
		
		System.out.println("服务器启动");
	}
	
	public void service() throws IOException {
		/* 
		   在SelectionKey中定义了4种事件, 分别用4个int类型的常量来表示
		   SelectionKey.OP_ACCEPT:	常量值为16, 接收连接就绪事件, 表示服务器监听到了客户的连接, 服务器可以接收这个连接了 
		   SelectionKey.OP_CONNECT:	常量值为 8, 接收就绪事件, 表示客户与服务器的连接已经建立成功
		   SelectionKey.OP_READ:	常量值为 1, 读就绪事件, 表示通道中已经有了可读数据, 可以执行读操作了
		   SelectionKey.OP_WRITE: 	常量值为 4, 写就绪事件, 表示已经可以向通道写数据了
		   
		   以上常量分别占据不同的二进制, 可以通过二进制的或运算 "|", 来将它们任意组合
		   
		   SelectionKey 包含两种类型的事件:
		   所有感兴趣的事件:
				interestOps() 方法返回SelectionKey所有感兴趣的事件
				interestOps(int Op) 方法用于为SelectionKey对象添加一个感兴趣的事件
		   所有已经发生的事件: 
				readyOps() 方法返回所有已经发生的事件
				
		   SelectionKey的主要方法:
		   SelectableChannel channel()	返回与之关联的SelectableChannel
		   Selector selector()	返回与之关联的Selector
		   boolean isValid()	判断这个SelectionKey是否有效, 对象创建后就一直有效, 
								如果调用了cancel() 或者关闭了与他关联的SelectableChannnel或Selector对象, 他就失效
		   void cancel()	使SelectionKey对象失效
		   int interestOps()	返回感兴趣的事件
		   SelectionKey interestOps(int ops)	添加感兴趣的事件, 返回SelectionKey对象本身的引用, 相当于return this
		   int readyOps()		返回已经就绪的事件
		   boolean isReadable()  等价于 (key.readyOps() & OP_READ != 0)
		   boolean isWritable()
		   boolean isConnectable()
		   boolean isAcceptable()  
		   Object attach(Object ob) 使 SelectionKey 关联一个附件, 一个SelectionKey只能关联一个附件, 最后关联的那个附件
		   Object attachment() 返回与 SelectionKey 对象关联的附件
		 */
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		while (selector.select() > 0) {
			Set readyKeys = selector.selectedKeys();			//获得Selector的selected-keys集合
			Iterator it = readyKeys.iterator();
			while (it.hasNext()) {
				SelectionKey key = null;
				try {							//处理SelectionKey
					key = (SelectionKey)it.next();		//取出一个SelectionKey
					it.remove();						//把SelectionKey从Selector的selected-key集合中删除
					
					if (key.isAcceptable()) {			//处理连接就绪事件
						//获得与SelectionKey关联的ServerSocketChannel
						ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
						//获得与客户连接的SocketChannel
						SocketChannel socketChannel = (SocketChannel)ssc.accept();
						System.out.println("接收到客户连接, 来自: " + socketChannel.socket().getInetAddress() + ":"
							+ socketChannel.socket().getPort());
						//把socketChannel设置为非阻塞模式
						socketChannel.configureBlocking(false);
						//创建一个用于存放用户发送来的数据的缓冲区
						ByteBuffer buffer = ByteBuffer.allocate(1024);
						//SocketChannel向Selector注册读就绪事件和写就绪事件
						socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, buffer);		//关联了一个buffer附件
					}
					if (key.isReadable()) {				//处理读就绪事件
						receive(key);
					}
					if (key.isWritable()) {				//处理写就绪事件
						send(key);
					}
				} catch (IOException e) {
					e.printStackTrace();
					try {
						if (key != null) {
							//使这个SelectionKey失效, 使得Selector不再监控这个SelectionKey感兴趣的事件
							key.cancel();
							key.channel().close();		//关闭与这个SelectionKey关联的SocketChannel
						}
					} catch (Exception ex) {
						e.printStackTrace();
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
	
	public static void main(String[] args) throws Exception{
		EchoServer server = new EchoServer();
		server.service();
	}
}