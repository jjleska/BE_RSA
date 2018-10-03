import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.sql.Date;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class Certificat {
	
	static private BigInteger seqnum = BigInteger.ZERO;
	public X509Certificate x509;
	
	
	Certificat(String nom, PaireClesRSA cle, int validityDays) {
		// Constructeur d’un certificat auto-signé avec
		// CN = nom, la clé publique contenu dans PaireClesRSA,
		// la durée de validité.
		
		PublicKey pubkey = cle.Publique();
		PrivateKey privkey = cle.Privee();
		
		try {
			ContentSigner sigGen = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(privkey);
		} catch (OperatorCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(pubkey.getEncoded()) ;
		X509Name issuer = new X509Name ("CN="+nom);
		X509Name subject = new X509Name ("CN="+nom);
		seqnum=seqnum.add(BigInteger.ONE);
		Date startDate = new Date(System.currentTimeMillis());
		Date endDate = new Date(System.currentTimeMillis()+validityDays*24*60*60*1000);
		X509v1CertificateBuilder v1CertGen = new X509v1CertificateBuilder (issuer, seqnum, startDate, endDate, subject, subPubKeyInfo);

	}
	
	public boolean verifCertif (PublicKey pubkey) {
		return false;
		// Vérification de la signature du certificat à l’aide
		// de la clé publique passée en argument.
	}
}