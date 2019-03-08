public class Dados{
	private String ip;
	private String port;
	private String autenticador;
	private long ram;
 	private long cpu;
	private long rtt;
	private long banda;
	private int nhit;
	private int nmiss;
	private int nPoll;

	public Dados(String ip,String port){
		this.ip=ip;
		this.port=port;
		this.ram=-1;
		this.cpu=-1;
		this.rtt=-1;
		this.banda=-1;
		this.nhit=0;
		this.nmiss=0;
		this.autenticador="";
		this.nPoll=0;
	}
	public String getIp(){return this.ip;}
	public String getPort(){return this.port;}
	public long getRam(){return this.ram;}
	public long getCpu(){return this.cpu;}
	public long getRtt(){return this.rtt;}
	public long getBanda(){return this.banda;}
	public String getAutenticador(){return this.autenticador;}
	public int getHits(){return this.nhit;}
	public int getMiss(){return this.nmiss;}
	public float getPercentHit(){return (this.nhit/(this.nhit+this.nmiss));}
	public int getPoll(){return this.nPoll;}
	public void setPort(String port){this.port=port;}
	public void setRam(long ram){this.ram=ram;}
	public void setCpu(long cpu){this.cpu=cpu;}
	public void setRtt(long rtt){this.rtt=rtt;}
	public void setBanda(long banda){this.banda=banda;}
	public void setAutenticador(String aut){this.autenticador=aut;}
	public void setHit(){this.nhit+=1;}
	public void setMiss(){this.nmiss+=1;}
	public void addPoll(int p){this.nPoll+=p;}
	public void resetPoll(){this.nPoll=0;}
}