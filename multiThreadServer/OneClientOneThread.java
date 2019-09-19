import java.io.*;
import java.net.*;

public class OneClientOneThread {
	private int port = 8000;
	private ServerSocket serverSocket;
	
	public OneClientOneThread() throws IOException {
		serverSocket = new ServerSocket(port);
		System.out.println("服务器启动");
	}
	
	public void service() {
		while(true) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				Thread workThread = new Thread(new Handler(socket));
				workThread.start();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		new OneClientOneThread().service();
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