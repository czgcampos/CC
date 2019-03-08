import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Formatter;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.io.IOException;
import java.util.HashMap;

public class PDU{
	private static final String HMAC_SHA1_ALGORITHM="HmacSHA1";
	private HashMap<String,String> lastKeys;
	public PDU(){
		this.lastKeys=new HashMap<String,String>();
	}
	private static String toHexString(byte[] bytes){
		Formatter formatter=new Formatter();
		for (byte b : bytes)
			formatter.format("%02x",b);
		return formatter.toString();
	}
	public static String calculateRFC2104HMAC(String data,String key) throws SignatureException,NoSuchAlgorithmException,InvalidKeyException{
		SecretKeySpec signingKey=new SecretKeySpec(key.getBytes(),HMAC_SHA1_ALGORITHM);
		Mac mac=Mac.getInstance(HMAC_SHA1_ALGORITHM);
		mac.init(signingKey);
		return toHexString(mac.doFinal(data.getBytes()));
	}
	public byte[] getRTTReq(InetAddress ipf,String portaf,String key){
		try{
			lastKeys.put(portaf,key);
			String hmac=calculateRFC2104HMAC("data",key);
			String ans="RTT:request:"+ipf.toString()+":"+portaf+":"+hmac+":"+"data:"+key;
			byte[] buffer=ans.getBytes();
			return buffer;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	public byte[] getRTTReply(String ipf,String portaf,String key,String hmac){
		try{
			key.trim();
			String ans;
			String hmac2=calculateRFC2104HMAC("data",key);
			if(hmac2.equals(lastKeys.get(portaf)))
				ans="RTT:reply:"+ipf.toString()+":"+portaf+":"+hmac+":"+"data:"+key+":"+"CHECK";
			else
				ans="RTT:reply:"+ipf.toString()+":"+portaf+":"+hmac+":"+"data:"+key+":"+"ERROR";
			byte[] buffer=ans.getBytes();
			return buffer;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	public byte[] getPduData(InetAddress ipf,String portaf){
		return null;
	}
	public byte[] getMissFinal(InetAddress ipf,String portaf,String key,String rs){
		String missStr;
		if(rs.equals("T"))
			missStr="RTT:miss"+ipf+":"+portaf+":TIMEOUT";
		else if(rs.equals("A"))
			missStr="RTT:miss"+ipf+":"+portaf+":AUTHENTICATION";
		else
			missStr="RTT:miss"+ipf+":"+portaf+":UNKNOWN";
		return missStr.getBytes();
	}
	public byte[] getAckFinal(InetAddress ipf,String portaf,String key,long rtt){
		/* RTT:ack:IP:PORTA:RTT:KEY */
		String ans="RTT:ack:"+ipf+":"+portaf+":"+rtt+":"+key;
		byte[] buffer=ans.getBytes();
		return buffer;
	}
	public byte[] getProbeReq(InetAddress to,String porta,int nProbes){
		String mensagem="JOIN:"+to.toString()+":"+porta+":"+nProbes;
		byte[] buffer=mensagem.getBytes();
		return buffer;
	}
}