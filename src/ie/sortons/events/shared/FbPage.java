package ie.sortons.events.shared;

import com.googlecode.objectify.annotation.Embed;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Embed
public class FbPage implements FbPageI {
	
	@Id @Index
	private String pageId;
	private String name;
	private String pageUrl;

	public String getPageId(){
		return pageId;
	}
	
	public String getName(){
		return name;
	}
	
	public String getPageUrl(){
		return pageUrl;
	}

	public FbPage(){
	}
	
	public FbPage(String name, String pageUrl, String pageId){
		this.pageId = pageId;
		this.name = name;
		this.pageUrl = pageUrl;
	}
}
