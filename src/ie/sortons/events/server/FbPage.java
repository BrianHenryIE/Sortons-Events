package ie.sortons.events.server;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class FbPage {
	
	@Id @Index
	private String id;
	private String name;
	private String link;

	public String getId(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public String getLink(){
		return link;
	}

	public FbPage(String name, String link, String id){
		this.id = id;
		this.name = name;
		this.link = link;
	}
}
