package ie.sortons.events.shared;

import com.google.gwt.core.client.GWT;
import com.googlecode.objectify.annotation.AlsoLoad;
import com.googlecode.objectify.annotation.Embed;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;
import com.kfuntak.gwt.json.serialization.client.Serializer;


@Embed 
public class FbPage implements JsonSerializable, Comparable<FbPage> {
	
	@AlsoLoad("pageId") 
	public String page_id;
	
	public String name;
	
	@AlsoLoad("pageUrl") 
	public String page_url;

	public FbPageLocation location;

	public String getPageId(){
		return page_id;
	}

	public String getName(){
		return name;
	}

	public String getPageUrl(){
		return page_url;
	}
	
	public FbPageLocation getLocation(){
		return location;
	}

	public FbPage(){
	}

	public FbPage(String name, String pageUrl, String pageId){
		this.page_id = pageId;
		this.name = name;
		this.page_url = pageUrl;
	}



	public void setName(String name) {
		this.name = name;
	}


	public void setPageUrl(String pageUrl) {
		this.page_url = pageUrl;
	}


	@Override
	public int compareTo(FbPage other) {
		return this.page_id.compareTo(other.getPageId());
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
		return page_id.hashCode();
	}


	public static FbPage fromJson(String Json) {
		Serializer serializer = (Serializer) GWT.create(Serializer.class);
		return (FbPage)serializer.deSerialize(Json,"ie.sortons.events.shared.FbPage");
	}

	public String toJson() {
		Serializer serializer = (Serializer) GWT.create(Serializer.class);
		return serializer.serialize(this);
	}
}