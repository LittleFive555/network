import java.io.*;
import java.net.*;

class Server {
	private int port = 8000;
	private ServerSocket serverSocket;
	
	public Server() throws IOException {
		serverSocket = new ServerSocket(port, 3);			//连接请求队列的长度为3
		System.out.println("服务器启动成功");
	}
	
	public void service() {
		while (true) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();				//从连接请求队列中取出一个连接
				System.out.println("新连接" + socket.getInetAddress() + ":" + socket.getPort() + "建立成功");
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
	public static void main(String[] args) throws Exception{
		Server server = new Server();
		// Thread.sleep(60000 * 10);				//睡眠10分钟
		server.service();
	}

}