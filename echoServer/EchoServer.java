import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {
	private int port = 8000;
	private ServerSocket serverSocket;
	private int maxConnectNum = 2;
	
	public EchoServer() throws IOException {
		/*
		 * ServerSocket�Ĺ��췽�������ڲ���ϵͳ�аѵ�ǰ����ע��Ϊ���������̡�
		 */
		serverSocket = new ServerSocket(port, maxConnectNum);	//����8000�˿ڣ��ڶ�������Ϊ����������еĳ���
		System.out.println("����������");
	}
	
	public EchoServer(int somePort) throws IOException {
		serverSocket = new ServerSocket(somePort, maxConnectNum);
		System.out.println("��ȡ����������");
	}
	
	public String echo(String msg) {
		return "Echo: " + msg;
	}
	
	private PrintWriter getWriter(Socket socket) throws IOException {
		OutputStream socketOut = socket.getOutputStream();
		return new PrintWriter(socketOut, true);
	}
	
	private BufferedReader getReader(Socket socket) throws IOException {
		InputStream socketIn = socket.getInputStream();
		return new BufferedReader(new InputStreamReader(socketIn));
	}
	
	public void service() {
		while(true) {
			Socket socket = null;
			try {
				/*
				 * �������������������ServerSocket�����accept�������÷���һֱ�����˿ڣ��ȴ��ͻ�����������
				 * ������յ�һ����������accept()�����ͻ᷵��һ��Socket�������Socket������ͻ��˵�Socket�����γ���һ��ͨ����·
				 */
				socket = serverSocket.accept();		//�ȴ��ͻ�����������
				System.out.println("New connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
				BufferedReader br = getReader(socket);
				PrintWriter pw = getWriter(socket);
				
				String msg = null;
				while ((msg = br.readLine()) != null) {
					System.out.println(msg);
					pw.println(echo(msg));
					if (msg.equals("bye")) {
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (socket != null)	socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static void main(String[] args) throws IOException {
		new EchoServer().service();
	}
}
