import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
public class Client{
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;
	private BufferedReader systemIn;

	public Client(String hostname,int porto){
		try{
			this.socket=new Socket(hostname,porto);
			this.in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.systemIn=new BufferedReader(new InputStreamReader(System.in));
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	public void write(String msg){
		try{
			this.out.write(msg);
			this.out.newLine();
			this.out.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void startClient(){
		try{
			Runnable cl=new ClientListener();
			new Thread(cl).start();
			String op;
			while((op=systemIn.readLine())!=null)
				write(op);
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
		}catch(UnknownHostException e){
			System.out.println("ERRO: Server down");
			e.printStackTrace();
		}catch(Exception e){
			System.out.println("ERRO: "+e.getMessage());
			e.printStackTrace();
		}
	}
	public static void main(String[] args){
		try{
			if(args!=null){
				Client c=new Client("127.0.0.1",7800);
				c.startClient();
			}
			else System.out.println("java Client porta");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public class ClientListener implements Runnable{
		public ClientListener(){}
		public void run(){
			System.out.println("ClientListener");
			String message;
			try{
				while((message=in.readLine())!=null && !message.equals("kill listener"))
					System.out.println(message);
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
}