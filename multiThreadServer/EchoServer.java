package multiThreadServer;

import java.io.*;
import java.net.*;

public class EchoServer {
	private int port = 8000;
	private ServerSocket serverSocket;
	private ThreadPool threadPool;
	private final int POOL_SIZE = 4;
	
	public EchoServer() throws IOException {
		serverSocket = new ServerSocket(port);
		//Runtime 的 availableProcessors() 方法返回当前系统的CPU的数目
		threadPool = new ThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);
		
		System.out.println("服务器启动成功");
	}
	
	public void service() {
		while (true) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				threadPool.execute(new Handler(socket));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
	}
	
	public static void main(String[] args) throws IOException{
		new EchoServer().service();
	}
}

class Handler implements Runnable {
	private Socket socket;
	public Handler(Socket socket) {
		this.socket = socket;
	}
	private PrintWriter getWriter(Socket socket) throws IOException {
		OutputStream out = socket.getOutputStream();
		return new PrintWriter(out, true);
	}
	private BufferedReader getReader(Socket socket) throws IOException {
		InputStream in = socket.getInputStream();
		return new BufferedReader(new InputStreamReader(in));
	}
	public String echo(String msg) {
		return "Echo: " + msg;
	}
	public void run() {
		try {
			System.out.println("新连接" + socket.getInetAddress() + ":" + socket.getPort() +"建立");
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
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
}