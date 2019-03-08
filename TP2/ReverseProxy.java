import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;

public class ReverseProxy{
	private ServerSocket serverSocket;
	private int port=7800;
	private TabelaEstado servidores;
	public void startServer(){
		try{
			this.serverSocket=new ServerSocket(this.port);
			System.out.println("ReverseProxy ONLINE");
			while(true){
				Socket socket=serverSocket.accept();
				System.out.println("ReverseProxy: new connection!");
				AgenteTCP tcp=new AgenteTCP(socket);
				new Thread(tcp).start();
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	public ReverseProxy(TabelaEstado te){
		this.servidores=te;
	}
	public class AgenteTCP implements Runnable{
		private Socket socket;
		private BufferedReader in;
		private BufferedWriter out;

		public AgenteTCP(Socket s){
			try{
				this.socket=s;
				this.in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
				this.out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			}catch(IOException e){
				System.out.println("ERRO[AGENTETCP] "+e);
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
		public void run(){
			try{
				String op=null;
				while((op=this.in.readLine())!=null){
					System.out.println("in from client: "+op);
					if(op.equals("poll")){
						String portOut=servidores.poll();
						String res="POLL:localhost:"+portOut;
						System.out.println(res);
						write(res);
					}
					if(op.equals("quit"))
						System.out.println("Client DC");
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}