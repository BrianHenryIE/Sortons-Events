package ie.sortons.events.server.facebook.gson;

import com.google.gson.Gson;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AppStartupInfo {
	
	String appBookmark = ""; 
	Boolean hasFbSession = false;
	Boolean hasFbPage = false;
	String fbUid = "";
	String fbAccessToken = "";
	Long fbAccessTokenExpires = null;
	String fbPageId = "";
	
/*
	
	public String getAppBookmark() { return appBookmark; }
	public Boolean getHasFbSession() { return hasFbSession; }
	public String getFbUid() { return fbUid; }
	public String getFbAccessToken() { return fbAccessToken; }
	public String getFbAccessTokenExpires() { return fbAccessToken; }
	public String getFbPageId() { return fbPageId; }
	
	*/
	
	public String toJson(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
