import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

public class ListeCertif extends HashMap <PublicKey,Certificat> {
	public void afficheCA() {
		for (PublicKey key : this.keySet()) {
			System.out.println("Equipement :" + this.get(key).getIssuer() +"\n");
			System.out.println("cle plublique :" + key.toString() );
			System.out.println("Certificat :"+this.get(key).getSignature()+"\n");
		}
	}
	public void afficheDA(){
		
	}
	
	//assuming A is in DA of the source equipment 
	public ArrayList<Certificat> certifChain(String nomDest, ListeCertif CA_Dest, ListeCertif CA_this)
	{		
		ArrayList<ArrayList<Certificat>> ways = new ArrayList<ArrayList<Certificat>>(CA_this.size());
		ArrayList<Integer> right_ways = new ArrayList<Integer>();
		
		
		int i = 0;
		for(PublicKey key : CA_this.keySet()) //compute one way for each CA element
		{
			boolean no_more_next = false;
			
			PublicKey temp_key = key;
			
			while(!(CA_Dest.containsKey(temp_key)) && !no_more_next) //we get out if the key is in CA_Dest or there is no more next
			{
				no_more_next = true;
				for(PublicKey key2 : this.keySet()) //explore all DA keys
				{
					if(this.get(temp_key).pubkey == key2) //if we find the next element
					{
						ways.get(i).add(this.get(temp_key)); //we add it to the way
						temp_key = key2; 
						no_more_next = false; //we just found a next
						break; //no need to continue exploration
					}
				}
			}
			if(!no_more_next) //if we got out of the loop the right way
			{
				right_ways.add(i); //then it is a right way !
			}
			
			i++;
		}
		
		if (right_ways.isEmpty()){
			System.out.println("Error");
			return null;
		}
		else{
			return ways.get(right_ways.get(0));
		}
	}
}
