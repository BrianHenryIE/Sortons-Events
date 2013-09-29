package ie.sortons.events.shared;

import org.jsonmaker.gwt.client.JsonizerException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.googlecode.objectify.annotation.Embed;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;



@Embed
public class DsFbPage implements DsFbPageJsonizer {


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

	public DsFbPage(){
	}

	public DsFbPage(String name, String pageUrl, String pageId){
		this.pageId = pageId;
		this.name = name;
		this.pageUrl = pageUrl;
	}


	public void setPageId(String pageId) {
		this.pageId = pageId;
	}


	public void setName(String name) {
		this.name = name;
	}


	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}

	@Override
	public Object asJavaObject(JavaScriptObject jsValue)
			throws JsonizerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String asString(Object javaValue) throws JsonizerException {
		// TODO Auto-generated method stub
		return null;
	}

	public String asJsonString() {
		// Create a DsFbPageJsonizer instance
		DsFbPageJsonizer pj = (DsFbPageJsonizer)GWT.create(DsFbPageJsonizer.class);
		// Jsonize
		return  pj.asString(this);
	}

}
