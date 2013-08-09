package ie.sortons.events.server;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FbPage {

	
	private String id;
	private String name;
	private String link;


	public FbPage(String name, String link, String id){
		this.id = id;
		this.name = name;
		this.link = link;
	}
}
