import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.Scanner;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Equipement {
	
	private PaireClesRSA maCle; // La paire de cle de l’equipement.
	private Certificat monCert; // Le certificat auto-signe.
	private String monNom; // Identite de l’equipement.
	private int monPort; // Le numéro de port d’ecoute.
	
	//Socket attributes
	private int ServerPort;
	private String ServerName;
	private Socket clientSocket = null;
	
	private ServerSocket serverSocket = null;
	private Socket NewServerSocket = null;
	
	private InputStream NativeIn = null;
	private ObjectInputStream ois = null;
	private OutputStream NativeOut = null;
	private ObjectOutputStream oos = null;
	
	
	Equipement (String nom, int port) throws Exception {
		// Constructeur de l’equipement identifie par nom
		// et qui « écoutera » sur le port port.
		
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
			eq = new Equipement(eq_name, 5000);
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
		
		
		System.out.println("S'agit-il d'un serveur? (y/n)");
		String serv = user_input.next();
		
	
		if (serv.equals("y")){
			// Creation de socket (TCP)
			try {
			eq.serverSocket = new ServerSocket(eq.monPort);

			} catch (IOException e) {
			// Gestion des exceptions
				System.out.println("Server socket failed");
			}
			// Attente de connextions
			try {
				eq.NewServerSocket = eq.serverSocket.accept();
			} catch (Exception e) {
			// Gestion des exceptions
				System.out.println("New socket creation failed");
			}
			// Creation des flux natifs et evolues
			try {
				eq.NativeIn = eq.NewServerSocket.getInputStream();
				eq.ois = new ObjectInputStream(eq.NativeIn);
				eq.NativeOut = eq.NewServerSocket.getOutputStream();
				eq.oos = new ObjectOutputStream(eq.NativeOut);

				

			} catch (IOException e) {
			// Gestion des exceptions

				System.out.println("Streams failed");
			}
			// Reception d’un String
			try {
				String res = (String) eq.ois.readObject();
				System.out.println(res);
				} catch (Exception e) {
				// Gestion des exceptions
					System.out.println("oupsi, reception failed :(");
				}
			// Emission d’un String
			try {
			eq.oos.writeObject(eq.monNom());
			eq.oos.flush();
			} catch (Exception e) {
			// Gestion des exceptions
				System.out.println("oupsi, emission failed :(");
			}
		}
		
		else {
			// Creation de socket (TCP)
			try {
				eq.clientSocket = new Socket("127.0.0.1", 5000);
			} catch (Exception e) {
			// Gestion des exceptions
				System.out.println("Socket creation failed");
			}
			// Creation des flux natifs et evolues
			try {
				eq.NativeOut = eq.clientSocket.getOutputStream();
				eq.oos = new ObjectOutputStream(eq.NativeOut);
				eq.NativeIn = eq.clientSocket.getInputStream();
				eq.ois = new ObjectInputStream(eq.NativeIn);

			} catch (Exception e) {
			// Gestion des exceptions
				System.out.println("Streams failed");
			}
			// Emission d’un String
			try {
			eq.oos.writeObject(eq.monNom());
			eq.oos.flush();
			} catch (Exception e) {
			// Gestion des exceptions
				System.out.println("oupsi, emission failed :(");
			}
			// Reception d’un String
			try {
				String res = (String) eq.ois.readObject();
				System.out.println(res);
				} catch (Exception e) {
				// Gestion des exceptions
					System.out.println("oupsi, reception failed :(");
				}

		}
		
		/*Equipement eqb;
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
		}*/
		
		
		
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
			System.out.println("NOPE");
		}
		
		return newc;
		
	}
	
	public void initInsertionClient () {
		// Demande au serveur à s'inserer en envoyant mon certificat
			try {
			this.oos.writeObject(this.monCert);
			this.oos.flush();
			} catch (Exception e) {
			// Gestion des exceptions
				System.out.println("oupsi, j'ai pas envoyé mon certif autosigné");
			}
		// Reception du certif du serveur
			Certificat certifServeur = null;
		try {
			certifServeur=  (Certificat) this.ois.readObject();
			System.out.println(certifServeur);
			} catch (Exception e) {
			// Gestion des exceptions
				System.out.println("oupsi, j'ai pas le certif du serveur");
			}
		//Verification certif serveur
		boolean okcertifserveur= certifServeur.verifCertif(certifServeur.pubkey);
	}
	
	public void affichage_da() {
		// Affichage de la liste des équipements de DA.
	}
	
	public void affichage_ca() {
		// Affichage de la liste des équipements de CA.
	}
	
	public void affichage() {
		// Affichage de l’ensemble des informations
		// de l’équipement.
	}
	
	public String monNom (){
		return monNom;
		// Recuperation de l’identite de l’équipement.

	}
	
	public PublicKey maClePub() {
		return null;
		// Recuperation de la clé publique de l’équipement.
	}
		
	public Certificat monCertif() {
		return null;
		// Recuperation du certificat auto-signé.
	}
}