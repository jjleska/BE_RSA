import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

public class ListeCertif extends HashMap <PublicKey,Certificat> {
	public void afficheCA() {
		for (PublicKey key : this.keySet()) {
			System.out.println("Equipement qui nous certifie:" + this.get(key).getIssuer() );
			System.out.println("cle plublique :" + key.toString() );
			System.out.println("Certificat :"+this.get(key).getSignature()+"\n");
		}
	}
	public void afficheDA(){
		for (PublicKey key : this.keySet()) {
			System.out.println("Source :" + this.get(key).getIssuer() );
			System.out.println("Destination :"+this.get(key).getDest()+"\n");
			System.out.println("cle plublique :" + key.toString()+"\n" );

		}
	}

	//assuming A is in DA of the source equipment 
	public ArrayList<Certificat> certifChain( ListeCertif CA_Dest, ListeCertif CA_this)
	{		
		ArrayList<ArrayList<Certificat>> ways = new ArrayList<ArrayList<Certificat>>(CA_this.size());
		ArrayList<Integer> right_ways = new ArrayList<Integer>();


		int i = 0;
		for(PublicKey key : CA_this.keySet()) //compute one way for each CA element
		{
			ways.get(i).add(CA_this.get(key)); //add first element
			boolean no_more_next = false;

			PublicKey temp_key = key;

			while(!(CA_Dest.containsKey(temp_key)) && !no_more_next) //we get out if the key is in CA_Dest or there is no more next
			{
				no_more_next = true;
				for(PublicKey key2 : this.keySet()) //explore all DA keys
				{
					if(this.get(temp_key).pubkey == key2 && this.get(key2).verifCertif(key2)) //if we find the next element
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
				ways.get(i).add(CA_Dest.get(temp_key)); //we can add the last element
			}

			i++;
		}


		//check valid chains
		if (right_ways.isEmpty()){
			System.out.println("Error");
			return null;
		}	

		else{
			int right_index = 0;

			for(i = 1; i<right_ways.size();i++){
				if(ways.get(i).size() < ways.get(right_index).size()){
					right_index = i;
				}
			}
			return ways.get(right_ways.get(right_index));
		}
	}
}
