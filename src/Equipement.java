import java.security.PublicKey;

public class Equipement {
	
	private PaireClesRSA maCle; // La paire de cle de l�equipement.
	private Certificat monCert; // Le certificat auto-signe.
	private String monNom; // Identite de l�equipement.
	private int monPort; // Le num�ro de port d�ecoute.
	
	Equipement (String nom, int port) throws Exception {
		// Constructeur de l�equipement identifie par nom
		// et qui � �coutera � sur le port port.
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
		return null;
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