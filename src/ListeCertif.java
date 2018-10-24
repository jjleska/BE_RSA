import java.security.PublicKey;
import java.util.HashMap;

public class ListeCertif extends HashMap <PublicKey,Certificat> {
	public void afficheCA() {
		for (PublicKey key : this.keySet()) {
			System.out.println("Equipement :" + this.get(key).getIssuer() );
			System.out.println("cle plublique :" + key.toString() );
			System.out.println("Certificat :"+this.get(key).getSignature()+"\n");
		}
	}
	public void afficheDA(){
		
	}
}
