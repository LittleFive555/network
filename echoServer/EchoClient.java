import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
/*
Socket�����¼���ѡ��
TCP_NODELAY: ��ʾ������������
SO_RESUSEADDR: ��ʾ�Ƿ���������Socket���󶨵ı��ص�ַ
SO_TIMEOUT: ��ʾ��������ʱ�ĵȴ���ʱʱ��
SO_LINGER: ��ʾ��ִ��Socket��close()����ʱ���Ƿ������رյײ��Socket
SO_SNFBUF: ��ʾ�������ݵĻ������Ĵ�С
SO_KEEPALIVE: ��ʾ���ڳ�ʱ�䴦�ڿ���״̬��Socket���Ƿ�Ҫ�Զ������ر�
OOBINLINE: ��ʾ�Ƿ�֧�ַ���һ���ֽڵ�TCP��������
*/
public class EchoClient {
	
	private String host = "localhost";
	private int port = 8000;
	private Socket socket;
	
	public EchoClient() throws IOException {
		/*
		 * ����host��ʾEchoServer�������ڵ����������֣�����port��ʾEchoServer���̼����Ķ˿ڡ�
		 * ������host��ȡֵΪ��localhost��ʱ����ʾEchoClient��EchoServer����������ͬһ�������ϡ�
		 */
		socket = new Socket(host, port);
		
		
		/*
		 * Socket�໹���������췽����������ʽ�����ÿͻ��˵�IP��ַ�Ͷ˿�
		 *  ���һ������ͬʱ�����������ϵ����磬���Ϳ���ӵ���������ϵ�IP��ַ��Internet�к;������У�
		 */	
//		socket = new Socket(InetAddress address, int port, InetAddress localAddr,int localPort) throws IOException;
//		socket = new Socket(String host, int port, InetAddress localAddr, int localPort) throws IOException;
		
		
		/*
		 * ���·�ʽ�����趨�ͻ��˵ȴ��������ӵĳ�ʱʱ�䣬
		 * ���޶�ʱ���ڣ����ӳɹ�����connect()����˳������
		 * �޶�ʱ���ڣ�����ĳ���쳣�����׳����쳣
		 * �����޶�ʱ�䣬��û�����ӳɹ���Ҳû�г��������쳣����ô���׳�SocketTimeoutException
		 * ���timeout��Ϊ0����ʾ��Զ���ᳬʱ
		 */
//		socket = new Socket();
//		SocketAddress remoteAddr = new InetSocketAddress(host, port);
//		socket.connect(remoteAddr, 600);
		
		/*
		 * Socket�Ĺ��췽���������ӷ�����ʱ�����ܻ��׳�������쳣����ΪIOException��ֱ�ӻ������ࣩ��
		 * ������UnknownHostException������޷�ʶ�����������ֻ�IP��ַ���ͻ��׳������쳣
		 * ������ConnectException������û�з��������̼����ƶ��Ķ˿ڣ����߷��������ܾ̾����ӣ��ͻ��׳������쳣
		 * ������SocketTimeoutException������ȴ����ӳ�ʱ���ͻ��׳������쳣
		 * ������BindException������޷���Socket������ָ���ı���IP��ַ��˿ڰ󶨣��ͻ��׳������쳣
		 */
	}
	
	public EchoClient(int somePort) throws IOException {
		socket = new Socket(host, somePort);
	}
	
	private PrintWriter getWriter(Socket socket) throws IOException {
		OutputStream socketOut = socket.getOutputStream();
		return new PrintWriter(socketOut, true);
	}
	
	private BufferedReader getReader(Socket socket) throws IOException {
		InputStream socketIn = socket.getInputStream();
		return new BufferedReader(new InputStreamReader(socketIn));
	}
	
	public void talk() throws IOException {
		try {
			BufferedReader br = getReader(socket);
			PrintWriter pw = getWriter(socket);
			BufferedReader localReader = new BufferedReader(new InputStreamReader(System.in));
			String msg = null;
			while ((msg = localReader.readLine()) != null) {
				pw.println(msg);
				System.out.println(br.readLine());
				if (msg.equals("bye")) {
					break;
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		new EchoClient().talk();
	}
}
