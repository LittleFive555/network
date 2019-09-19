import java.net.*;
import java.io.*;
import java.util.Base64;

/*由于163的邮件服务器拒绝我的邮件, 总认为是垃圾邮件
	错误代码如下:
		554 DT:SPM 163 smtp11,D8CowADX7wsV7IJdze7ZDw--.14387S2 1568861220,
		please see http://mail.163.com/help/help_spam_16.htm?ip=223.104.3.244&hostid=smtp11&time=1568861220
	所以该测试代码作废,  需要换其他邮箱
*/



class TestSMTP {
	private String smtpServer = "smtp.163.com";						//SMTP邮件服务器的主机名
	private int port = 25;
	
	public static void main(String[] args) {
		Message msg = new Message("liulimengplay@163.com", 			//发送者的邮件地址
									"18810118832@163.com",			//接收者的邮件地址
									"javaSMTP 测试",						//邮件标题
									"我就是想测试一下我的代码, 为什么要给我退信!");
		new TestSMTP().sendMail(msg);
	}
	
	public void sendMail(Message msg) {
		Socket socket = null;
		try {
			socket = new Socket(smtpServer, port);					//连接到邮件服务器
			BufferedReader br = getReader(socket);
			PrintWriter pw = getWriter(socket);						//客户主机的名字
			String localhost = InetAddress.getLocalHost().getHostName();
			
			String username = "liulimengplay@163.com";
			String password = null;
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			password = reader.readLine();							//输入密码
			reader.close();
			
			Base64.Encoder encoder = Base64.getEncoder();
			username = encoder.encodeToString(username.getBytes());
			password = encoder.encodeToString(password.getBytes());
			
			sendAndReceive(null, br, pw);							//仅仅是为了接收服务器的响应数据
			sendAndReceive("HELO " + localhost, br, pw);
			
			sendAndReceive("AUTH LOGIN", br, pw);
			sendAndReceive(username, br, pw);
			sendAndReceive(password, br, pw);
			
			sendAndReceive("MAIL FROM: <" + msg.from + ">", br, pw);
			sendAndReceive("RCPT TO: <" + msg.to + ">", br, pw);
			sendAndReceive("DATA", br, pw);							//接下来开始发送邮件内容
			pw.println(msg.data);									//发送邮件内容
			System.out.println("Client>" + msg.data);
			sendAndReceive(".", br, pw);							//邮件发送完毕
			sendAndReceive("QUIT", br, pw);							//结束通信
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
	/**发送一行字符串, 并接受一行服务器的响应数据*/
	private void sendAndReceive(String str, BufferedReader br, PrintWriter pw) throws IOException{
		if (str != null) {
			System.out.println("Client>" + str);
			pw.println(str);							//发送完str字符串后, 还会发送"\r\n"
		}
		String response;
		if ((response = br.readLine()) != null) {
			System.out.println("Server>" +response);
		}
	}
	
	private PrintWriter getWriter(Socket socket) throws IOException {
		OutputStream out = socket.getOutputStream();
		return new PrintWriter(out, true);
	}
	
	private BufferedReader getReader(Socket socket) throws IOException {
		InputStream in = socket.getInputStream();
		return new BufferedReader(new InputStreamReader(in));
	}
}

class Message {						//表示邮件
	String from;					//发送者邮件地址
	String to;						//接收者邮件地址
	String subject;					//标题
	String content;					//正文
	String data;					//内容, 包括邮件标题和正文
	public Message(String from, String to, String subject, String content) {
		this.from = from;
		this.to = to;
		this.subject = subject;
		this.content = content;
		data = "Subject:" + subject + "\r\n" +content;
	}
}