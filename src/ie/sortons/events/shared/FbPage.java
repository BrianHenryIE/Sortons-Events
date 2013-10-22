package ie.sortons.events.shared;

import org.jsonmaker.gwt.client.JsonizerException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.googlecode.objectify.annotation.Embed;
import com.googlecode.objectify.annotation.Index;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;
import com.kfuntak.gwt.json.serialization.client.Serializer;



@Embed
public class FbPage implements JsonSerializable, FbPageJsonizer, Comparable<FbPage> {


	@Index
	public String pageId;
	public String name;
	public String pageUrl;

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


//
//	public static class FBPOverlay extends JavaScriptObject {
//		protected FBPOverlay() {}
//
//		public final native String getPageId() /*-{ return this.pageId; }-*/;
//		public final native String getName() /*-{ return this.name; }-*/;
//		public final native String getPageUrl() /*-{ return this.pageUrl; }-*/;
//
//	}


	
	
	public static FbPage fromJson(String Json) {
	        Serializer serializer = (Serializer) GWT.create(Serializer.class);
	        return (FbPage)serializer.deSerialize(Json,"ie.sortons.events.shared.FbPage");
	}
	 
	public String toJson() {
	        Serializer serializer = (Serializer) GWT.create(Serializer.class);
	        return serializer.serialize(this);
	}
}
