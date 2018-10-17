import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.Scanner;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Equipement {
	
	private PaireClesRSA maCle; // La paire de cle de l�equipement.
	private Certificat monCert; // Le certificat auto-signe.
	private String monNom; // Identite de l�equipement.
	private int monPort; // Le num�ro de port d�ecoute.
	
	
	Equipement (String nom, int port) throws Exception {
		// Constructeur de l�equipement identifie par nom
		// et qui � �coutera � sur le port port.
		
		this.monNom = nom;
		this.monPort = port;
		
	}
	
	public static void main(String[] args)
	{
		Security.addProvider(new BouncyCastleProvider());
		
		
		Scanner user_input = new Scanner(System.in);
		System.out.println("Nom de l'equipement ?");
		String eq_name = user_input.next();

		Equipement eq;
		try {
			eq = new Equipement(eq_name, 4500);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	
		eq.maCle = new PaireClesRSA();
		try {
			eq.monCert = new Certificat(eq.monNom, eq.maCle, 30);
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(eq.monCert.verifCertif(eq.maCle.Publique())){
			System.out.println(eq.monCert);
		}else{
			System.out.println("BITE");
		}
		
		Equipement eqb;
		try {
			eqb = new Equipement("blob", 4400);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		try {
			Certificat cert = eqb.pairing(eq);
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		
		
	}
	
	public Certificat pairing(Equipement equb) throws CertificateException{
		PaireClesRSA pk1,pk2;
		pk1 = new PaireClesRSA();
		pk2 = new PaireClesRSA();
		
		Certificat newc= new Certificat(this.monNom,equb.monNom,pk1, pk2, 60);
		
		if(newc.verifCertif(pk2.Publique()))
		{
			System.out.println(newc);
		}else{
			System.out.println("BITE AUSSI");
		}
		
		return newc;
		
	}
	
	public void affichage_da() {
		// Affichage de la liste des �quipements de DA.
	}
	
	public void affichage_ca() {
		// Affichage de la liste des �quipements de CA.
	}
	
	public void affichage() {
		// Affichage de l�ensemble des informations
		// de l��quipement.
	}
	
	public String monNom (){
		return monNom;
		// Recuperation de l�identite de l��quipement.

	}
	
	public PublicKey maClePub() {
		return null;
		// Recuperation de la cl� publique de l��quipement.
	}
		
	public Certificat monCertif() {
		return null;
		// Recuperation du certificat auto-sign�.
	}
}