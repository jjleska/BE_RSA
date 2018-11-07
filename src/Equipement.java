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
import java.util.ArrayList;
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

	private static InputStream NativeIn = null;
	private static ObjectInputStream ois = null;
	private static OutputStream NativeOut = null;
	private static ObjectOutputStream oos = null;

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
		System.out.println("Port machine?");
		monport = Integer.parseInt(user_input.next());
		eq.monPort = monport;


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
				// Creation de socket serveur
				try {
					eq.serverSocket = new ServerSocket(eq.monPort);

				} catch (IOException e) {
					// Gestion des exceptions

				}

				// Attente de connections
				try {
					eq.NewServerSocket = eq.serverSocket.accept();
				} catch (Exception e) {
					// Gestion des exceptions
					System.out.println("New socket creation failed");
					e.printStackTrace();
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
					e.printStackTrace();
				}

				eq.InitInsertionServer();

				//Fermeture flux et sockets
				ois.close();
				oos.close();
				NativeIn.close();
				NativeOut.close();
				eq.NewServerSocket.close();

				break;

				//init client
			case "c" :
				System.out.println("Port serveur ?");
				portserv = Integer.parseInt(user_input.next());
				// Creation de socket vers le serveur
				try {

					eq.clientSocket = new Socket("127.0.0.1", portserv);
				} catch (Exception e) {
					// Gestion des exceptions
					System.out.println("Socket creation failed in init");
					e.printStackTrace();
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

				//Fermeture des flux et socket
				ois.close();
				oos.close();
				NativeIn.close();
				NativeOut.close();
				eq.clientSocket.close();

				break;

			case "i" :
				System.out.println(monCert);
				break;
			case "u":
				CA.afficheCA();
				break;
			case "r" : 
				if(DA.size()!=0) {
					DA.afficheDA();}
				else {
					System.out.println("Ma liste DA est vide : je ne connais que mes voisins directs"+"\n");
				}
				break;

				//Synchro serveur
			case "ss" :

				eq.monPort = monport;
				// Creation de socket serveur
				try {
					eq.serverSocket = new ServerSocket(eq.monPort);

				} catch (IOException e) {
					// Gestion des exceptions

				}
				// Attente de connections
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
				//System.out.println("Clé publique envoyee? ");
				//J'envoie ma clé
				eq.oos.writeObject(eq.maClePub());
				eq.oos.reset();

				/*
				//Je lis la reponse du client pour savoir la qte d'info a recevoir
				PublicKey temp_pubkey=  (PublicKey) eq.ois.readObject(); //donc pas ca

				//et les conditions ici aussi sont foireuses
				if (eq.CA.containsKey(temp_pubkey)) {
					eq.recoitliste();
					eq.envoiliste();
					System.out.println("CA synchro");
				}
				else if (eq.DA.containsKey(temp_pubkey)) {
					System.out.println("DA synchro starts");
					eq.envoi_ListeCertif(eq.CA);

				}
				 */
				String synchro_message=  (String) eq.ois.readObject();
				System.out.println(synchro_message);

				if (synchro_message.equals("--CA_sync--")) {
					eq.recoitliste();
					eq.envoiliste();
					System.out.println("CA synchro");
				}
				else if (synchro_message.equals("--DA_sync--")) {
					//System.out.println("DA synchro starts");
					eq.envoi_ListeCertif(eq.CA);

					ArrayList<Certificat> chaine = eq.recoit_CertifChain();

					//verifier la chaine
					Boolean chaineok=false;
					if (chaine.size()!=0) {
						chaineok=true;
					}
					System.out.println("Verification de la chaine : "+chaineok);
					//envoie le resultat de la verif de chaine au client
					eq.oos.writeObject(chaineok);
					eq.oos.reset();
					// si ok entre-certification
					if (chaineok) {
						eq.InitServerAuto();
					}
					//ajouter au DA

				}
				else if(synchro_message.equals("--STOP_SYNC--")){
					System.out.println("Composantes independantes");
					eq.InitInsertionServer();
				}
				else{
					System.out.println("Erreur : le message est errone");
				}

				//Fermeture des flux et socket
				ois.close();
				oos.close();
				NativeIn.close();
				NativeOut.close();
				eq.NewServerSocket.close();

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
				//Je lis la cle publique envoyee par le serveur
				PublicKey temp_pubkeyc=  (PublicKey) eq.ois.readObject();
				/*//J'envoie la mienne
				eq.oos.writeObject(eq.maClePub());
				eq.oos.reset();*/



				if (eq.CA.containsKey(temp_pubkeyc)) {
					eq.oos.writeObject((String) "--CA_sync--");
					eq.oos.reset();
					eq.envoiliste();
					eq.recoitliste();
				}
				else if (eq.DA.containsKey(temp_pubkeyc)) {
					eq.oos.writeObject((String) "--DA_sync--");
					eq.oos.reset();

					ListeCertif CA_serv = new ListeCertif();
					CA_serv = eq.recoit_ListeCertif();

					ArrayList<Certificat> certif_chain = new ArrayList<Certificat>();
					certif_chain = eq.DA.certifChain(CA_serv, eq.CA);

					eq.envoi_CertifChain(certif_chain);
					//recoit le res de la verif de chaine
					Boolean checkcertif=  (Boolean) eq.ois.readObject();
					//entre certif et ajout au CA
					if (checkcertif) {
						eq.initClientAuto();
					}
				}
				else {
					eq.oos.writeObject((String) "--STOP_SYNC--");
					eq.oos.reset();
					System.out.println("Composantes indépendantes");
					eq.initInsertionClient();
				}
				//Fermeture des flux et socket
				ois.close();
				oos.close();
				NativeIn.close();
				NativeOut.close();

				eq.clientSocket.close();

				break;
			}


		}



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

		//Envoi cle publique serveur et nom
		try {
			this.oos.writeObject(this.maCle.Publique());
			this.oos.reset();
			this.oos.writeObject(this.monNom);
			this.oos.reset();
		} catch (IOException e2) {
			System.out.println("Je n'ai pas envoyé ma clé ou mon nom");
			e2.printStackTrace();
		}


		//Demande utilisateur pour l'ajout du serveur
		System.out.println("Ajouter le periphérique "+nomClient+"? (y/n)");
		String repajout = user_input.next();

		//envoi de la reponse de l'ajout
		try {
			this.oos.writeObject(repajout);
			this.oos.reset();
		} catch (IOException e1) {
			System.out.println("Reponse non envoyée");
			e1.printStackTrace();
		}
		//reception reponse du client pour l'ajout
		String repclient=null;
		try {
			repclient=  (String) this.ois.readObject();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		//Si on a récupéré le nom et l'utilisateur a accepté
		if((clientPKey != null) && (repajout.equals("y"))&&(repclient.equals("y"))){
			//Creation du certificat
			try {
				Certificat serv_user = new Certificat(this.monNom, nomClient, clientPKey, this.maCle.Privee(), 60);

				//Envoi certificat client
				this.oos.writeObject(serv_user);
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
			System.out.println("Refus de l'un des partis");
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


		//Demande utilisateur pour l'ajout du serveur
		System.out.println("Ajouter le serveur "+nomserveur+"?(y/n)");
		String repajout = user_input.next();

		//reception reponse du serveur pour l'ajout
		String repserveur=null;
		try {
			repserveur=  (String) this.ois.readObject();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//envoi de la reponse de l'ajout
		try {
			this.oos.writeObject(repajout);
			this.oos.reset();
		} catch (IOException e1) {
			System.out.println("Reponse non envoyée");
			e1.printStackTrace();
		}

		// Reception du certif du serveur
		if (repajout.equals("y")&&repserveur.equals("y")) {
			Certificat certifServeur = null;
			try {
				certifServeur=  (Certificat) this.ois.readObject();
			} catch (Exception e) {
				// Gestion des exceptions
				System.out.println("oupsi, j'ai pas le certif du serveur");
			}
			boolean okcertifserveur= certifServeur.verifCertif(clepubserveur);

			if (okcertifserveur) {

				//Si l'utilisateur accepte d'ajouter le periphérique on certifie le serveur
				if (repajout.equals("y")&&repserveur.equals("y")) {
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
						//System.out.println(new_DA_size);
						PublicKey temp_pubkey;
						Certificat temp_certif;
						for (int i = 0; i<new_DA_size; i++)
						{
							temp_pubkey=  (PublicKey) this.ois.readObject();

							temp_certif=  (Certificat) this.ois.readObject();
							this.DA.put(temp_pubkey, temp_certif);
						}
						//this.DA.afficheDA();
						//System.out.println(this.DA);
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
		else {
			System.out.println("Opération refusée par l'un des partis");
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
			//System.out.println(this.DA);
			//this.DA.afficheDA();

		}catch(Exception e){
			System.out.println("DA reception failed");
		}
	}


	public void envoi_ListeCertif(ListeCertif input) {
		try {
			Integer chaine_size = (Integer) input.size();

			//Envoi le nombre d'objets a recuperer 
			this.oos.writeObject(chaine_size);
			this.oos.reset();

		}catch(Exception e){

			System.out.println("Error in chain size sending : " + e);
		}
		try{
			//Envoi de la chaine
			for(PublicKey key : input.keySet())
			{
				this.oos.writeObject(key);
				this.oos.reset();
				this.oos.writeObject(input.get(key));
				this.oos.reset();
			}

		}catch(Exception e){
			System.out.println("Error in chain sending");
		}

	}

	public ListeCertif recoit_ListeCertif() {
		ListeCertif chaine = new ListeCertif();
		try{
			int chaine_size = (Integer) this.ois.readObject();

			PublicKey temp_pubkey;
			Certificat temp_certif;
			for (int i = 0; i<chaine_size; i++)
			{
				temp_pubkey=  (PublicKey) this.ois.readObject();

				temp_certif=  (Certificat) this.ois.readObject();

				chaine.put(temp_pubkey, temp_certif);
			}

		}catch(Exception e){
			System.out.println("chain reception failed");
		}
		return chaine;
	}

	public void envoi_CertifChain(ArrayList<Certificat> input) {
		Integer chaine_size = 0;
		chaine_size = (Integer) input.size();
		System.out.println(chaine_size);
		try {

			//Envoi le nombre d'objets a recuperer 
			this.oos.writeObject(chaine_size);
			this.oos.reset();

		}catch(Exception e){

			System.out.println("Error in certif chain size sending : " + e);
		}
		try{
			//Envoi de la chaine
			for(int i = 0; i<chaine_size;i++)
			{
				this.oos.writeObject(input.get(i));
				this.oos.reset();
			}

		}catch(Exception e){
			System.out.println("Error in certif chain sending : " + e);
		}

	}

	public ArrayList<Certificat> recoit_CertifChain() {
		ArrayList<Certificat> chaine = new ArrayList<Certificat>();
		try{
			int chaine_size = (Integer) this.ois.readObject();

			Certificat temp_certif;
			for (int i = 0; i<chaine_size; i++)
			{
				temp_certif=  (Certificat) this.ois.readObject();

				chaine.add(temp_certif);
			}

		}catch(Exception e){
			System.out.println("chain reception failed");
		}
		return chaine;
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
	public void InitServerAuto(){
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


		//Si on a récupéré le nom 
		if((clientPKey != null)){
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
	public void initClientAuto () throws CertificateException, IOException, ClassNotFoundException {
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


			//Si l'utilisateur accepte d'ajouter le periphérique on certifie le serveur
			if (true) {
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
					//System.out.println(new_DA_size);
					PublicKey temp_pubkey;
					Certificat temp_certif;
					for (int i = 0; i<new_DA_size; i++)
					{
						temp_pubkey=  (PublicKey) this.ois.readObject();

						temp_certif=  (Certificat) this.ois.readObject();
						this.DA.put(temp_pubkey, temp_certif);
					}
					//this.DA.afficheDA();
					//System.out.println(this.DA);
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
}