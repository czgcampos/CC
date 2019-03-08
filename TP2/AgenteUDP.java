import java.net.MulticastSocket;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.UnknownHostException;
import com.sun.management.ThreadMXBean;
import java.lang.management.ManagementFactory;
import java.util.Random;
import java.io.IOException;

public class AgenteUDP{
	private boolean connected;
	private DatagramSocket agenteSocket;
	private InetAddress agenteIP;
	private int agentePort;
	private byte[] agenteReceive=new byte[512];
	private PDU pdu;

	public AgenteUDP(int portt){
		this.connected=false;
		try{
			this.agenteIP=InetAddress.getByName("localhost");
			this.agentePort=portt;
			this.pdu=new PDU();
			this.agenteSocket=new DatagramSocket(this.agentePort,this.agenteIP);
		}catch(Exception e){
			System.out.println("ERROR[AGUDP]: Creating agent socket?");
		}
		Runnable al=new AgenteListener();
		new Thread(al).start();
	}
	public static void main(String args[]){
		if(args!=null){
			int p=Integer.parseInt(args[0]);
			AgenteUDP jamesBond=new AgenteUDP(p);
		}
		else
			System.out.println("java AgenteUDP porta");
	}
	public class AgenteListener implements Runnable{
		//private MulticastSocket socket;
		String mensagem;
		private String vnum;
		byte[] bufferE=new byte[512];
		byte[] bufferR=new byte[512];
		String[] sp=new String[10];
		private String generateKey(){
			Random rn=new Random();
			int result=rn.nextInt(10000-1000+1)+1000;
			return Integer.toString(result);
		}
		private void handleRtt(InetAddress ipt,int portt){
			Runnable rttThread=new Runnable(){
				DatagramPacket rttRPacket;
				DatagramPacket rttSPacket;
				public void run(){
					try{
						String key=generateKey().trim();
						bufferE=new byte[512];
						String ap=""+agentePort; ap.trim();
						bufferE=pdu.getRTTReq(agenteIP,ap,key);
						rttSPacket=new DatagramPacket(bufferE,bufferE.length,ipt,portt);
						agenteSocket.send(rttSPacket);
						long t1=System.currentTimeMillis();
						long t2;
						rttRPacket=new DatagramPacket(bufferR,bufferR.length);
						agenteSocket.receive(rttRPacket);
						t2= System.currentTimeMillis();
						long res=t2-t1;
						String reply=new String(rttRPacket.getData());
						sp=reply.split(":");
						if(res>10000)
							bufferE=pdu.getMissFinal(agenteIP,ap,key,"T");
						else if(sp[6].equals("ERROR"))
							bufferE=pdu.getMissFinal(agenteIP,ap,key,"A");
						else
							bufferE=pdu.getAckFinal(agenteIP,ap,key,res);	
						rttRPacket=new DatagramPacket(bufferE,bufferE.length,ipt,portt);
						agenteSocket.send(rttRPacket);	
					}catch(IOException e){
						e.printStackTrace();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			};
			Thread r=new Thread(rttThread);
			r.start();
		}
		public void sendInfo(InetAddress ipt,int portt)throws IOException{
			Runnable infoThread=new Runnable(){
			public void run(){
				ThreadMXBean status=(ThreadMXBean) ManagementFactory.getThreadMXBean();
					long cput=status.getThreadCpuTime(Thread.currentThread().getId());
					long ram=status.getThreadAllocatedBytes(Thread.currentThread().getId());
					/* DATA:IP:PORTA:CPU:RAM */
					String info="DATA:"+agenteIP+":"+agentePort+":"+cput+":"+ram+":"+generateKey();
					bufferE=info.getBytes();
					try{
						DatagramPacket infoPacket=new DatagramPacket(bufferE,bufferE.length,ipt,portt);
						agenteSocket.send(infoPacket);
					}catch(IOException e){
						e.printStackTrace();
					}
				}
			};
			Thread r=new Thread(infoThread);
			r.start();
		}
		public void enviaDados(InetAddress ipt,int portt){
			try{
				if(connected){
					handleRtt(ipt,portt);
					sendInfo(ipt,portt);
				}
				else{
					mensagem="LOGIN:"+agenteIP.toString()+":"+agenteSocket.getLocalPort();
					System.out.println(mensagem);
					connected=true;
					bufferE=mensagem.getBytes();
					DatagramPacket pacote=new DatagramPacket(bufferE,bufferE.length,ipt,portt);
					agenteSocket.send(pacote);
				}	
			}catch(Exception e){
				System.out.println("ERRO[AgenteUDP] Error sending reply."+e);
				connected=false;
			}
		}
		public void run(){
			System.out.println("####################\n#####AGENTE UDP#####\n####################");
			try{
				MulticastSocket socket=new MulticastSocket(8888);
				InetAddress group=InetAddress.getByName("239.8.8.8");
				socket.joinGroup(group);
				while(true){
					byte[] buffer=new byte[512];
					DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
					socket.receive(packet);
					Random rn=new Random();
					int result=rn.nextInt(10-1+1)+1;
					Thread.slepp(result);
					String mensagemR=new String(packet.getData());
					String[] sp=mensagemR.split(":");
					if(mensagemR.contains("JOIN")){
						InetAddress sendTo=InetAddress.getByName("localhost");
						int portTo=Integer.parseInt(sp[2]);
						enviaDados(sendTo,portTo);
					}
				}
			}catch(UnknownHostException e){
				e.printStackTrace();
			}catch(Exception e){
				System.out.println("ERRO[AgenteUDP] Not receiving data from monitor."+e);
			}
		}
	}
}