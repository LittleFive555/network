import java.io.*;
import java.net.*;
import java.util.concurrent.*;

class ShutdownableServer {
	private int port = 8000;
	private ServerSocket serverSocket;
	private ExecutorService executorService;			//线程池
	private int POOL_SIZE = 4;							//单个CPU时线程池中工作线程的数目
	
	private int portForShutdown = 8001;					//用于监听关闭服务器命令的端口
	private ServerSocket serverSocketForShutdown;		
	private boolean isShutdown = false;					//服务器是否已经关闭
	
	private Thread shutdownThread = new Thread() {		//负责关闭服务器的线程
		public void start() {
			this.setDaemon(true);						//设置为守护线程(也称为后台线程)
			super.start();
		}
		public void run() {
			while (!isShutdown) {
				Socket socketForShutdown = null;
				try {
					socketForShutdown = serverSocketForShutdown.accept();
					BufferedReader br = new BufferedReader(new InputStreamReader(socketForShutdown.getInputStream()));
					String command = br.readLine();
					if (command.equals("shutdown")) {
						long beginTime = System.currentTimeMillis();
						socketForShutdown.getOutputStream().write("服务器正在关闭\r\n".getBytes());
						isShutdown = true;
						//请求关闭线程池
						//线程池不再接收新的任务, 但是会继续完成队列中现有的任务
						executorService.shutdown();
						
						//等待关闭线程池, 每次等待的超时时间为30秒
						while(!executorService.isTerminated())
							executorService.awaitTermination(30, TimeUnit.SECONDS);
						
						//如果此处关闭serverSocket, 会导致socketForShutdown报错:
						//		java.net.SocketException: Connection reset
						//管理员客户接收不到服务器关闭的回应
						//所以将关闭serverSocket放到发送关闭信息后  即49行
						// serverSocket.close();
						
						long endTime = System.currentTimeMillis();
						socketForShutdown.getOutputStream().write(("服务器已经关闭, " + 
								"关闭服务器用了 " + (endTime - beginTime) + "毫秒\r\n").getBytes());
								
						serverSocket.close();			//关闭与Client客户通信的ServerSocket
						socketForShutdown.close();
						serverSocketForShutdown.close();
					}
					else {
						socketForShutdown.getOutputStream().write("错误的命令\r\n".getBytes());
						socketForShutdown.close();
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	public ShutdownableServer() throws IOException{
		serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(60000);			//设定等待客户连接的超时时间为60秒
		
		serverSocketForShutdown = new ServerSocket(portForShutdown);
		
		//创建线程池
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);
		
		shutdownThread.start();						//启动负责关闭服务器的线程
		System.out.println("服务器启动");
	}
	
	public void service() {
		while (!isShutdown) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				socket.setSoTimeout(60000);					//把等待客户发送数据的超时时间设为60秒
				executorService.execute(new Handler(socket));
			} catch (SocketTimeoutException e) {
				//不必处理等待客户连接时出现的超时异常
			} catch(RejectedExecutionException e) {
				try {
					if (socket != null) 
						socket.close();
				} catch (IOException x) {}
				return;
			} catch(SocketException e) {
				//如果是由于在执行serverSocket.accept()方法时,
				//ServerSocket被ShutdownThread异常关闭而导致的异常, 就退出service()方法
				if(e.getMessage().indexOf("socket closed") != -1)
					return;
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws IOException{
		new ShutdownableServer().service();
	}
}

class Handler implements Runnable {
	private Socket socket;
	
	public Handler(Socket socket) {
		this.socket = socket;
	}
	private BufferedReader getReader(Socket socket) throws IOException {
		InputStream socketIn = socket.getInputStream();
		return new BufferedReader(new InputStreamReader(socketIn));
	}
	private PrintWriter getWriter(Socket socket) throws IOException {
		OutputStream socketOut = socket.getOutputStream();
		return new PrintWriter(socketOut, true);
	}
	public String echo(String msg) {
		return "Echo: " + msg;
	}
	public void run() {
		try {
			System.out.println("新连接 " + socket.getInetAddress() + ":" + socket.getPort() + " 建立");
			BufferedReader br = getReader(socket);
			PrintWriter pw = getWriter(socket);
			
			String msg = null;
			while ((msg = br.readLine()) != null) {
				System.out.println(msg);
				pw.println(echo(msg));
				if (msg.equals("bye"))
					break;
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (socket != null)
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	} 
}