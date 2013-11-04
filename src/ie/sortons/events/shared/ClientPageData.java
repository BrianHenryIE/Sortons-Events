package ie.sortons.events.shared;


import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;
import com.kfuntak.gwt.json.serialization.client.Serializer;

@Entity
public class ClientPageData implements JsonSerializable {

	// this will be either a page id or an app id or an interest list??
	@Id @Index
	public String clientPageId;

	public FbPage clientPage;

	public List<FbPage> includedPages = new ArrayList<FbPage>();

	public List<FbPage> ignoredPages = new ArrayList<FbPage>();

	@Ignore
	public List<FbPage> suggestedPages = new ArrayList<FbPage>();

	public ClientPageData() { }
//
//	public ClientPageData(ClientPageData.CPDOverlay overlay) {
//		this.clientPageId = overlay.getClientPageId();
//		this.clientPage = new FbPage(overlay.getClientPage().getName(), overlay.getClientPage().getPageUrl(), overlay.getClientPage().getPageId());
//
//		for (int i = 0; i < overlay.getIncludedPages().length(); i++) {
//			includedPages.add(new FbPage(overlay.getIncludedPages().get(i).getName(), overlay.getIncludedPages().get(i).getPageUrl(), overlay.getIncludedPages().get(i).getPageId()));
//		}
//
//
//		for (int i = 0; i < overlay.getIgnoredPages().length(); i++) {
//			ignoredPages.add(new FbPage(overlay.getIncludedPages().get(i).getName(), overlay.getIncludedPages().get(i).getPageUrl(), overlay.getIncludedPages().get(i).getPageId()));
//		}
//
//	}

	public ClientPageData(FbPage clientPageDetails) {
		this.clientPage = clientPageDetails;
		this.clientPageId = clientPageDetails.getPageId();
		addPage(clientPageDetails);
	}

	public String getClientPageId() {
		return this.clientPageId;
	}

	public FbPage getClientPage(){
		return this.clientPage;
	}


	public List<FbPage> getIncludedPages() {
		return new ArrayList<FbPage>(includedPages);
	}
	public List<FbPage> getIgnoredPages() {
		return new ArrayList<FbPage>(ignoredPages);
	}



	public void setSuggestedPages(List<FbPage> suggestedPages) {
		this.suggestedPages = suggestedPages;
	}

	public List<FbPage> getSuggestedPages() {
		return suggestedPages;
	}



	public boolean addPage(FbPage page) {
		boolean added = false;


		if(!includedPages.contains(page)){
			includedPages.add(page);
			added = true;
		}
		if(ignoredPages.contains(page)){
			ignoredPages.remove(page);
		}
		return added;
	}


	// TODO
	public boolean ignorePage(FbPage page) {

		boolean excluded = false;
		if(includedPages.contains(page)){
			includedPages.remove(page);
			excluded = true;
		}
		if(!ignoredPages.contains(page)){
			ignoredPages.add(page);
			excluded = true;
		}else{
			excluded = true;
		}
		return excluded;
	}


	public FbPage getPageById(String pageId){
		FbPage thePage = null;		
		for(FbPage page : includedPages){
			if(page.getPageId() == pageId){
				thePage = page;
			}
		}
		return thePage;
	}


	public List<String> getIncludedPageIds() {
		List<String> pageIds = new ArrayList<String>();
		for(FbPage page : includedPages){
			pageIds.add(page.getPageId());
		}
		return pageIds;
	}

	public List<String> getIgnoredPageIds() {
		List<String> pageIds = new ArrayList<String>();
		for(FbPage page : ignoredPages){
			pageIds.add(page.getPageId());
		}
		return pageIds;
	}

	
	
	public static ClientPageData fromJson(String Json) {
		Serializer serializer = (Serializer) GWT.create(Serializer.class);
		return (ClientPageData)serializer.deSerialize(Json,"ie.sortons.events.shared.ClientPageData");
	}
	
	
//
//	public static class CPDOverlay extends JavaScriptObject {
//
//		protected CPDOverlay() {} 
//
//		// TODO should this line be gone? It's mainly for datastore indexing...
//		public final native String getClientPageId() /*-{ return this.clientPageId; }-*/;
//
//		public final native FbPage.FBPOverlay getClientPage() /*-{ return this.clientPage; }-*/;
//
//		public final native JsArray<FbPage.FBPOverlay> getIncludedPages() /*-{ return this.includedPages; }-*/;
//		public final native JsArray<FbPage.FBPOverlay> getIgnoredPages() /*-{ return this.ignoredPages; }-*/;
//	}

}
