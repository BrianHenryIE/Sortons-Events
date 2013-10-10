package ie.sortons.events.shared;


import ie.sortons.events.client.view.overlay.ClientPageDataOverlay;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class ClientPageData {

	// this will be either a page id or an app id or an interest list??
	@Id @Index
	private String clientPageId;

	private FbPage clientPage;

	private List<FbPage> includedPages = new ArrayList<FbPage>();

	private List<FbPage> ignoredPages = new ArrayList<FbPage>();

	public ClientPageData() { }

	public ClientPageData(ClientPageDataOverlay overlay) {
		this.clientPageId = overlay.getClientPageId();
		this.clientPage = new FbPage(overlay.getClientPage());

		for (int i = 0; i < overlay.getIncludedPages().length(); ++i) {
			includedPages.add(new FbPage(overlay.getIncludedPages().get(i)));
		}

		for (int i = 0; i < overlay.getIgnoredPages().length(); ++i) {
			ignoredPages.add(new FbPage(overlay.getIgnoredPages().get(i)));
		}

	}

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
}
