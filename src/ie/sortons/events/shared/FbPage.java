package ie.sortons.events.shared;

import ie.sortons.events.client.view.overlay.FbPageOverlay;

import org.jsonmaker.gwt.client.JsonizerException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.googlecode.objectify.annotation.Embed;
import com.googlecode.objectify.annotation.Index;



@Embed
public class FbPage implements FbPageJsonizer, Comparable<FbPage> {


	@Index
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


	public FbPage(FbPageOverlay overlay) {
		this.pageId = overlay.getPageId();
		this.name = overlay.getName();
		this.pageUrl = overlay.getPageUrl();
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

	// I think I can leave this alone
	@Override
	public Object asJavaObject(JavaScriptObject jsValue)
			throws JsonizerException {
		// TODO Auto-generated method stub
		return null;
	}
	
	// I think I can leave this alone
	@Override
	public String asString(Object javaValue) throws JsonizerException {
		// TODO Auto-generated method stub
		return null;
	}
	
	// How ot use this on client side and GSON server side?!
	public String asJsonString() {
		// Create a FbPageJsonizer instance
		FbPageJsonizer pj = (FbPageJsonizer)GWT.create(FbPageJsonizer.class);
		// Jsonize
		return  pj.asString(this);
	}

	@Override
	public int compareTo(FbPage other) {
		return this.pageId.compareTo(other.getPageId());
	}

	@Override
	public final boolean equals(Object obj) {
	  if (obj == this)
	    return true;
	  if (!(obj instanceof FbPage))
	    return false;
	  return compareTo((FbPage) obj) == 0;
	}
	
	@Override
	public final int hashCode() {
	  return pageId.hashCode();
	}
	
}
