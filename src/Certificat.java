import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class Certificat {
	
	static private BigInteger seqnum = BigInteger.ZERO;
	public X509Certificate x509;
	
	Certificat(String nom, PaireClesRSA cle, int validityDays) {
		// Constructeur d�un certificat auto-sign� avec
		// CN = nom, la cl� publique contenu dans PaireClesRSA,
		// la dur�e de validit�.
		// Fesse.
	}
	public boolean verifCertif (PublicKey pubkey) {
		return false;
		// V�rification de la signature du certificat � l�aide
		// de la cl� publique pass�e en argument.
	}
}