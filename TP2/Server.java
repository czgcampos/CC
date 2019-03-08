class Server{
	private MonitorUDP monitor;
	private ReverseProxy rp;
	private TabelaEstado te;
	private PDU pdu;
	public Server(){
		this.te=new TabelaEstado();
		try{
			this.monitor=new MonitorUDP(te);
			this.rp=new ReverseProxy(te);
			Runnable ms=new MStarter();
			Runnable rps=new RPStarter();
			new Thread(ms).start();
			new Thread(rps).start();
		}catch(Exception e){
			System.out.println("ERROR[Server]: MonitorUDP");
		}
	}
	public static void main(String args[])throws Exception{
		System.out.println("SERVER RUNNING");
		Server s=new Server();
	}
	public class MStarter implements Runnable{
		public MStarter(){}
		public void run(){
			monitor.executa();
		}
	}
	public class RPStarter implements Runnable{
		public RPStarter(){}
		public void run(){
			rp.startServer();
		}
	}
}