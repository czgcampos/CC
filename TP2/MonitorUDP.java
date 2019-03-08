import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.Random;

public class MonitorUDP{
	private TabelaEstado servidores;
	private InetAddress ipMonitor;
	private DatagramSocket serverSocketPR;
	private DatagramSocket serverSocketML;
	private DatagramSocket serverSocketBE;
	private PDU pdu;

	public MonitorUDP(TabelaEstado t)throws Exception{
		this.ipMonitor=InetAddress.getByName("localhost");
		this.serverSocketPR=new DatagramSocket(9999,this.ipMonitor);
		this.serverSocketML=new DatagramSocket(9998,this.ipMonitor);
		this.servidores=t;
		this.pdu=new PDU();
	}
	public void executa(){
		try{
			Runnable listener=new MonitorListener();
			new Thread(listener).start();
			Runnable pb=new ProbingRequest(34);
			new Thread(pb).start();
		}catch(Exception e){
			System.out.println("executa??");
		}
	}
	public class ProbingRequest implements Runnable{
		private int intervalo;
		private int nProbes;

		public ProbingRequest(int i){
			this.intervalo=i;
			this.nProbes=0;
		}
		public void beam(InetAddress ip,int porta)throws IOException{
			serverSocketBE=new DatagramSocket();
			InetAddress to=InetAddress.getByName("localhost");
			byte[] buffer=pdu.getProbeReq(to,"9998",nProbes);
			DatagramPacket pacote=new DatagramPacket(buffer,buffer.length,ip,porta);
			serverSocketBE.send(pacote);
			System.out.println("PROBING REQUEST("+ip+" Port: "+8888+")");
		}
		public void run(){
			try{
				System.out.println("Sending periodical probing requests.");
				while(true){
					beam(InetAddress.getByName("239.8.8.8"),8888);
					Thread.sleep(intervalo*100);
				}
			}catch(Exception e){
				System.out.println("ERROR[Monitor] Not handling Probing Request."+e);
			}
		}
	}
	class MonitorListener implements Runnable{
		private byte[] recDados=new byte[512];
		private byte[] resposta=new byte[512];
		private int nEntradas;

		public MonitorListener(){
			this.nEntradas=0;
		}
		public void run(){
			try{
				System.out.println("MonitorListener opened successfully!");
				while(true){
					String mensagem=new String();
					String[] sp=new String[10];
					recDados=new byte[512];
					resposta=new byte[512];
					DatagramPacket recPacket=new DatagramPacket(recDados,recDados.length);
					serverSocketML.receive(recPacket);
					mensagem=new String(recPacket.getData());
					InetAddress senderIP=recPacket.getAddress();
					int senderPort=recPacket.getPort();
					sp=mensagem.split(":");
					if(sp[0].equals("LOGIN")){
						servidores.adicionaAgente(sp);
						nEntradas++;
					}
					else if(sp[0].equals("RTT")){
						if(sp[1].equals("request")){
							String key=sp[4].trim();
							byte[] buffer=pdu.getRTTReply(sp[2],sp[3],sp[6],sp[4]);
							DatagramPacket pacrtt=new DatagramPacket(buffer,buffer.length,senderIP,senderPort);
							serverSocketML.send(pacrtt);
						}
						else if(sp[1].equals("ack"))
							if(servidores.updateRtt(sp)) System.out.println("RTT UPDATE");
						else if(sp[1].equals("miss"))
							servidores.incMiss(sp);
					}
					else if(sp[0].equals("DATA"))
						servidores.updateInfo(sp);
					else if(sp[0].equals("logout"))
						nEntradas--;
				}
			}catch(Exception e){
				System.out.println("ERROR[MLISTENER] Not handling Probing Responses. ");
				e.printStackTrace();
				serverSocketPR.close();
			}
		}
	}
}