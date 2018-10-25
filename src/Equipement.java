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
	private static Certificat monCert; // Le certificat auto-signe.
	private String monNom; // Identite de l’equipement.
	private int monPort; // Le numéro de port d’ecoute.
	private static ListeCertif CA;//La liste CA
	private static ListeCertif DA;//La liste DA
	
	
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
	private static boolean quitter=false;
	

	Equipement (String nom, int port) throws Exception {
		// Constructeur de l’equipement identifie par nom
		// et qui « écoutera » sur le port port.
		
		this.monNom = nom;
		this.monPort = port;
		this.CA=new ListeCertif();
		this.DA=new ListeCertif();
		
	}
	
	public static void main(String[] args) throws CertificateException, IOException, ClassNotFoundException
	{
		Security.addProvider(new BouncyCastleProvider());
		
		
		user_input = new Scanner(System.in);
		System.out.println("Nom de l'equipement ?");
		String eq_name = user_input.next();

		int monport = 4000;
		int portserv=7000;


		Equipement eq;
		try {
			eq = new Equipement(eq_name, monport);
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

		
		while (!quitter) {
			System.out.println("Choisir une option :\n i=> Informations sur l'équipement\n r=> Liste des équipements du réseau domestique\n u=> Liste des équipements de UT\n s=>Insertion serveur\n c=>Insertion client\n ss=>Synchronisation serveur\n sc=>Synchronisation client\n q=>quitter\n");
			String option = user_input.next();
			
			switch(option) {
			
			case "q":
				quitter=true;
				break;
			//init serveur
			case "s" :
				System.out.println("Port ?");
				monport = Integer.parseInt(user_input.next());
				eq.monPort = monport;
				// Creation de socket (TCP)
				try {
				eq.serverSocket = new ServerSocket(eq.monPort);
	
				} catch (IOException e) {
				// Gestion des exceptions
					
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
				//eq.NewServerSocket.close();

				break;
			
			//init client
			case "c" :
				System.out.println("Port serveur ?");
				portserv = Integer.parseInt(user_input.next());
				// Creation de socket (TCP)
				try {
					
					eq.clientSocket = new Socket("127.0.0.1", portserv);
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
				eq.clientSocket.close();

				break;
				
			case "i" :
				System.out.println(monCert);
				break;
			case "u":
				CA.afficheCA();
				break;
			case "r" : 
				DA.afficheDA();
			
			//Synchro serveur
			case "ss" :
				//System.out.println("Port ?");
				//monport = Integer.parseInt(user_input.next());
				eq.monPort = monport;

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
					eq.recoitliste();
					eq.envoiliste();

					//eq.NewServerSocket.close();
				break;
			
			//Synchro client
			case "sc":
				System.out.println("Port serveur ?");
				portserv = Integer.parseInt(user_input.next());
				// Creation de socket (TCP)
				try {
					eq.clientSocket = new Socket("127.0.0.1", portserv);
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
				eq.envoiliste();
				eq.recoitliste();
				eq.clientSocket.close();
				
				break;
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
							//Ajout de Client dans la CA du serveur
							this.CA.put(clientPKey, certifClient);
						}
						else {
							System.out.println("Certificat invalide");
						}
						
				} catch (Exception e) {
						// Gestion des exceptions
							System.out.println("oupsi, user certificate was not sent ");
						}
				try{
				//Envoi du DA du client
					Integer new_DA_size = (Integer) this.CA.size() + this.DA.size();
					System.out.println(new_DA_size);
						//Envoi le nombre d'objets a recuperer 
					this.oos.writeObject(new_DA_size);
					this.oos.reset();
					
					}catch(Exception e){
						
						System.out.println("Error in DA size sending : " + e);
					}
				try{
						//Envoie de CA(A) et DA(A)
					for(PublicKey key : this.CA.keySet())
					{
						this.oos.writeObject(key);
						this.oos.reset();
						this.oos.writeObject(this.CA.get(key));
						this.oos.reset();
					}
					for(PublicKey key : this.DA.keySet())
					{
						this.oos.writeObject(key);
						this.oos.reset();
						this.oos.writeObject(this.DA.get(key));
						this.oos.reset();
					}
				}catch(Exception e){
					System.out.println("Error in DA sending");
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
	
	public void initInsertionClient () throws CertificateException, IOException, ClassNotFoundException {
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
			System.out.println("Ajouter le serveur "+nomserveur+"?(y/n)");
			String repajout = user_input.next();
			
			//Si l'utilisateur accepte d'ajouter le periphérique on certifie le serveur
			if (repajout.equals("y")) {
				//Ajout de Serveur dans la CA du client
				this.CA.put(clepubserveur, certifServeur);
				
				//Certification du serveur
				Certificat certifserv = new Certificat(this.monNom, nomserveur, clepubserveur, this.maCle.Privee(), 60);
				
				//Envoi certificat serveur
				try{
					this.oos.writeObject(certifserv);
					this.oos.reset();
				}catch(Exception e){
					System.out.println("Return server certificate failed");
				}
				
				//Reception de DA
				try{
					int new_DA_size = (Integer) this.ois.readObject();
					System.out.println(new_DA_size);
					PublicKey temp_pubkey;
					Certificat temp_certif;
					for (int i = 0; i<new_DA_size; i++)
					{
						temp_pubkey=  (PublicKey) this.ois.readObject();

						temp_certif=  (Certificat) this.ois.readObject();
						this.DA.put(temp_pubkey, temp_certif);
					}
					this.DA.afficheDA();
					System.out.println(this.DA);
				}catch(Exception e){
					System.out.println("DA reception failed");
				}
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
	

	public void majDA(Equipement C, Equipement B) {
		//On met a jour DA[A] apres l'insertion de C alors qu'on connait directement B
		Certificat dansa1 = null;
		Certificat dansa2 = null;
		Certificat dansb1 = null;
		Certificat dansb2 = null;
		dansa1 = this.DA.get(C.maClePub());
		dansa2 = this.CA.get(C.maClePub());
		dansb1 = B.DA.get(C.maClePub());
		dansb2 = B.CA.get(C.maClePub());
		if ((dansa1==null)&&(dansa2==null)&&((dansb1!=null)||(dansb2!=null))) {
			this.DA.put(C.maClePub(),C.monCertif());
			System.out.println("Equipement ajouté dans DA de "+this.monNom);
		}
	}
	public void envoiliste() {
		try {
			Integer new_DA_size = (Integer) this.CA.size() + this.DA.size();
			
				//Envoi le nombre d'objets a recuperer 
			this.oos.writeObject(new_DA_size);
			this.oos.reset();
			
		}catch(Exception e){
				
			System.out.println("Error in DA size sending : " + e);
			}
		try{
				//Envoie de CA(A) et DA(A)
			for(PublicKey key : this.CA.keySet())
			{
				this.oos.writeObject(key);
				this.oos.reset();
				this.oos.writeObject(this.CA.get(key));
				this.oos.reset();
			}
			for(PublicKey key : this.DA.keySet())
			{
				this.oos.writeObject(key);
				this.oos.reset();
				this.oos.writeObject(this.DA.get(key));
				this.oos.reset();
			}
		}catch(Exception e){
			System.out.println("Error in DA sending");
		}
	
	}
	
	public void recoitliste() {
		try{
			int new_DA_size = (Integer) this.ois.readObject();
			
			PublicKey temp_pubkey;
			Certificat temp_certif;
			for (int i = 0; i<new_DA_size; i++)
			{
				temp_pubkey=  (PublicKey) this.ois.readObject();

				temp_certif=  (Certificat) this.ois.readObject();
				if (!this.DA.containsKey(temp_pubkey)&&!this.CA.containsKey(temp_pubkey)) {
					this.DA.put(temp_pubkey, temp_certif);
				}
				
			}
			System.out.println(this.DA);
			this.DA.afficheDA();
			
		}catch(Exception e){
			System.out.println("DA reception failed");
		}
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
		System.out.println(this.monCert);
	}
	
	public String monNom (){
		return monNom;
		// Recuperation de l’identite de l’équipement.

	}
	
	public PublicKey maClePub() {
		return this.maCle.Publique();
		// Recuperation de la clé publique de l’équipement.
	}
		
	public Certificat monCertif() {
		return this.monCert;
		// Recuperation du certificat auto-signé.
	}
}