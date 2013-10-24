package ie.sortons.events.shared;

import com.google.gwt.core.client.GWT;
import com.googlecode.objectify.annotation.Embed;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;
import com.kfuntak.gwt.json.serialization.client.Serializer;



@Embed 
public class FbPage implements JsonSerializable, Comparable<FbPage> {
	// @SerializeClassField(false)
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


	public static FbPage fromJson(String Json) {
		Serializer serializer = (Serializer) GWT.create(Serializer.class);
		return (FbPage)serializer.deSerialize(Json,"ie.sortons.events.shared.FbPage");
	}

	public String toJson() {
		Serializer serializer = (Serializer) GWT.create(Serializer.class);
		return serializer.serialize(this);
	}
}
