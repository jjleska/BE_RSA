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
	
	private static Scanner user_input=null;
	
	
	
	Equipement (String nom, int port) throws Exception {
		// Constructeur de l’equipement identifie par nom
		// et qui « écoutera » sur le port port.
		
		this.monNom = nom;
		this.monPort = port;
		
	}
	
	public static void main(String[] args) throws CertificateException, IOException
	{
		Security.addProvider(new BouncyCastleProvider());
		
		
		user_input = new Scanner(System.in);
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
			
			eq.InitInsertionServer();
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
			eq.initInsertionClient();
			
			

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
	
	//Serveur
	public void InitInsertionServer(){
		// Reception de la cle publique client et son nom
		PublicKey clientPKey = null;
		String nomClient=null;
			try {
				clientPKey = (PublicKey) this.ois.readObject();
				nomClient = (String) this.ois.readObject();
				} catch (Exception e) {
				// Gestion des exceptions
					System.out.println("oupsi, clé client et nom pas reçus");
				}
			//Demande utilisateur pour l'ajout du serveur
			System.out.println("Ajouter le periphérique "+nomClient+"? (y/n)");
			String repajout = user_input.next();

			//Si on a récupéré le nom et l'utilisateur a accepté
			if((clientPKey != null) && (repajout.equals("y"))){
				//Creation du certificat
				try {
						Certificat serv_user = new Certificat(this.monNom, nomClient, clientPKey, this.maCle.Privee(), 60);
						
						//Envoi certificat client
						this.oos.writeObject(serv_user);
						this.oos.reset();
						
						//Envoi cle publique serveur et nom
						this.oos.writeObject(this.maCle.Publique());
						this.oos.reset();
						this.oos.writeObject(this.monNom);
						this.oos.reset();
						
						//Reception certificat client
						Certificat certifClient = null;
						try {
							certifClient=  (Certificat) this.ois.readObject();
							} catch (Exception e) {
							// Gestion des exceptions
								System.out.println("oupsi, le client ne m'a pas certifié");
							}
						//Verification certificat client
						boolean okcertifclient= certifClient.verifCertif(clientPKey);
						if (okcertifclient) {
							System.out.println("Connexion validée");
						}
						else {
							System.out.println("Certificat invalide");
						}

						
				} catch (Exception e) {
						// Gestion des exceptions
							System.out.println("oupsi, user certificate was not sent ");
						}
				
				}
				else{
					try {
						this.oos.writeObject("NOPE");
						this.oos.flush();
						} catch (Exception e) {
						// Gestion des exceptions
							System.out.println("Refus utilisateur ou mauvaise réception clé");
						}
				}
			}
	
	public void initInsertionClient () throws CertificateException, IOException {
		// Demande au serveur à s'inserer en envoyant sa clé et son nom
			try {
				
			this.oos.writeObject(this.monCert.pubkey);
			this.oos.reset();
			this.oos.writeObject(this.monNom);
			this.oos.reset();
			} catch (Exception e) {
			// Gestion des exceptions
				System.out.println("oupsi, j'ai pas envoyé ma clé et mon nom");
			}
		// Reception du certif du serveur
			Certificat certifServeur = null;
		try {
			certifServeur=  (Certificat) this.ois.readObject();
			} catch (Exception e) {
			// Gestion des exceptions
				System.out.println("oupsi, j'ai pas le certif du serveur");
			}

		//Reception cle serveur et nom
		PublicKey clepubserveur = null;
		String nomserveur=null;
		try {
			clepubserveur=  (PublicKey) this.ois.readObject();
			nomserveur=  (String) this.ois.readObject();

			} catch (Exception e) {
			// Gestion des exceptions
				System.out.println("oupsi, j'ai pas la clé du serveur");
			}

		
		boolean okcertifserveur= certifServeur.verifCertif(clepubserveur);
		if (okcertifserveur) {
			//Demande utilisateur pour l'ajout du serveur
			System.out.println("Ajouter le serveur ?(y/n)");
			String repajout = user_input.next();
			
			//Si l'utilisateur accepte d'ajouter le periphérique on certifie le serveur
			if (repajout.equals("y")) {
				Certificat certifserv = new Certificat(this.monNom, nomserveur, clepubserveur, this.maCle.Privee(), 60);
				
				//Envoi certificat serveur
				this.oos.writeObject(certifserv);
				this.oos.reset();
			}
			else {System.out.println("Refus d'ajout du périphérique");}
			
		}
		else {
			System.out.println("Certificat serveur invalide");
		}
	}
	
	
	
	
	public Certificat pairing(Equipement equb) throws CertificateException{
		PaireClesRSA pk1,pk2;
		pk1 = new PaireClesRSA();
		pk2 = new PaireClesRSA();
		
		Certificat newc= new Certificat(this.monNom,equb.monNom,pk1.Publique(), pk2.Privee(), 60);
		
		if(newc.verifCertif(pk2.Publique()))
		{
			System.out.println(newc);
		}else{
			System.out.println("NOPE");
		}
		
		return newc;
		
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
		return this.monCert;
		// Recuperation du certificat auto-signé.
	}
}