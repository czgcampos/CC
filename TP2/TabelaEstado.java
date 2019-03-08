import java.util.HashMap;
import java.util.Map;
import java.net.InetAddress;
import java.lang.NumberFormatException;

public class TabelaEstado{
	private HashMap<String, Dados> servidores;
	private int nEntries;

	public TabelaEstado(){
		this.servidores=new HashMap<>();
		this.nEntries=0;
	}
	public void adicionaAgente(String[] msg){
		/* LOGIN:IP:PORTA */
		if(!servidores.containsKey(msg[2])){
			System.out.printf("Novo IP adicionado: %s, %s\n",msg[1],msg[2].toString());
			servidores.put(msg[2],new Dados(msg[1],msg[2]));
			nEntries++;
		}
	}
	public void removeAgente(String[] msg){
		/* REMOVE:IP:PORTA */
		if(servidores.containsKey(msg[2])){
			System.out.printf("Servidor Removido: %s, %s\n",msg[1],msg[2]);
			servidores.remove(msg[2]);
			nEntries--;
		}
	}
	public boolean updateInfo(String[] msg){
		/* DATA:IP:PORTA:CPU:RAM */
		for(Map.Entry<String, Dados> entry : servidores.entrySet()){
			try{
				int x=Integer.parseInt(msg[2]);
				int y=Integer.valueOf(entry.getValue().getPort().trim());
				if(x==y){
					long c=Long.parseLong(msg[3]);
					entry.getValue().setCpu(c);
					long r=Long.parseLong(msg[4]);
					entry.getValue().setRam(r);
					return true;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		System.out.printf("ERROR[TabelaEstado] - Unlisted IP: %s\n",msg[2]);
		return false;
	}
	public boolean updateRtt(String[] msg){
		/* RTT:ack:IP:PORTA:RTT:HMAC */
		for(Map.Entry<String, Dados> entry : servidores.entrySet()){
			try{
				int x=Integer.parseInt(msg[3]);
				int y=Integer.valueOf(entry.getValue().getPort().trim());
				if(x==y){
					String po=msg[3].trim();
					long c=Long.parseLong(msg[4]);
					entry.getValue().setRtt(c);
					entry.getValue().setHit();
					entry.getValue().setAutenticador(msg[5]);
					return true;
				}
			}catch(NumberFormatException e){
				e.printStackTrace();
			}
		}
		return false;
	}
	public void incMiss(String[] msg){
		/* RTT:miss:IP:PORTA:MISSR */
		if(servidores.containsKey(msg[3]))
			servidores.get(msg[3]).setMiss();
		else 
			System.out.printf("ERROR[TabelaEstado] - Unlisted IP: %s\n",msg[3]);
	}
	public synchronized String poll(){
		for(Dados d : servidores.values())
			d.resetPoll();
		/* Como os agentes sao todos corridos em localhost, tem de ser a porta a chave da tabela */
		String bRtt="",bCpu="",bRam="",bHit="",res="";
		long rtt=90000000,cpu=0,ram=0,pts=0;
		float percent=0;
		for(Dados d : servidores.values()){
			if(d.getRtt()<rtt){
				rtt=d.getRtt();
				bRtt=d.getPort();
			}
			if(d.getPercentHit()>percent){
				percent=d.getPercentHit();
				bHit=d.getPort();
			}
			if(d.getCpu()>cpu){
				cpu=d.getCpu();
				bCpu=d.getPort();
			}
			if(d.getRam()>ram){
				ram=d.getRam();
				bRam=d.getPort();
			}
		}
		//atribui pontos
		servidores.get(bRtt).addPoll(11);
		servidores.get(bHit).addPoll(10);
		servidores.get(bCpu).addPoll(5);
		servidores.get(bRam).addPoll(5);
		for(Dados d : servidores.values())
			if(d.getPoll()>pts)
				res=d.getPort();
		return res;	
	}
}