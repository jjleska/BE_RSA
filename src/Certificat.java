public class Certificat {
	static private BigInteger seqnum = BigInteger.ZERO;
	public X509Certificate x509;
	Certificat(String nom, PaireClesRSA cle, int validityDays) {
// Constructeur d�un certificat auto-sign� avec
// CN = nom, la cl� publique contenu dans PaireClesRSA,
// la dur�e de validit�.
}
public boolean verifCertif (PublicKey pubkey) {
// V�rification de la signature du certificat � l�aide
// de la cl� publique pass�e en argument.
}
}