import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class Certificat {
	
	static private BigInteger seqnum = BigInteger.ZERO;
	public X509Certificate x509;
	
	
	Certificat(String nom, PaireClesRSA cle, int validityDays) throws CertificateException {
		// Constructeur d’un certificat auto-signé avec
		// CN = nom, la clé publique contenu dans PaireClesRSA,
		// la durée de validité.
		
		PublicKey pubkey = cle.Publique();
		PrivateKey privkey = cle.Privee();
		ContentSigner sigGen;
		
		try {
			 sigGen = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(privkey);
		} catch (OperatorCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(pubkey.getEncoded()) ;
		X500Name issuer = new X500Name ("CN="+nom);
		X500Name subject = new X500Name ("CN="+nom);
		seqnum=seqnum.add(BigInteger.ONE);
		Date startDate = new Date(System.currentTimeMillis());
		Date endDate = new Date(System.currentTimeMillis()+validityDays*24*60*60*1000);
		X509v1CertificateBuilder v1CertGen = new X509v1CertificateBuilder (issuer, seqnum, startDate, endDate, subject, subPubKeyInfo);
		
		
		// Opération nécessaire pour pouvoir par la suite avoir un certificat
		// au format x509
		JcaX509CertificateConverter converter = (new JcaX509CertificateConverter()).setProvider("BC");
		// On calcule la signature et on cree un certificate !
		x509 = converter.getCertificate(v1CertGen.build(sigGen));
	
	}
	
	Certificat(String nom1, String nom2, PaireClesRSA cle1, PaireClesRSA cle2, int validityDays) throws CertificateException {
		// Constructeur d’un certificat auto-signé avec
		// CN = nom, la clé publique contenu dans PaireClesRSA,
		// la durée de validité.
		
		PublicKey pubkey1 = cle1.Publique();
		PrivateKey privkey2 = cle2.Privee();
		ContentSigner sigGen;
		
		try {
			 sigGen = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(privkey2);
		} catch (OperatorCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(pubkey1.getEncoded()) ;
		X500Name issuer = new X500Name ("CN="+nom1);
		X500Name subject = new X500Name ("CN="+nom2);
		seqnum=seqnum.add(BigInteger.ONE);
		Date startDate = new Date(System.currentTimeMillis());
		Date endDate = new Date(System.currentTimeMillis()+validityDays*24*60*60*1000);
		X509v1CertificateBuilder v1CertGen = new X509v1CertificateBuilder (issuer, seqnum, startDate, endDate, subject, subPubKeyInfo);
		
		
		// Opération nécessaire pour pouvoir par la suite avoir un certificat
		// au format x509
		JcaX509CertificateConverter converter = (new JcaX509CertificateConverter()).setProvider("BC");
		// On calcule la signature et on cree un certificate !
		x509 = converter.getCertificate(v1CertGen.build(sigGen));
	
	}
	public String toString(){
		return this.x509.toString();
	}
	
	public boolean verifCertif (PublicKey pubkey) {
		// Vérification de la signature du certificat à l’aide
		// de la clé publique passée en argument.
		try {
			x509.verify(pubkey);
		}
		catch(Exception e) {
			return false;
		}
		return true;

	}
}