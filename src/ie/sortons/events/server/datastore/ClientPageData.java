package ie.sortons.events.server.datastore;

import ie.sortons.events.shared.FbPage;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class ClientPageData {
	
	// this will be either a page id or an app id or an interest list??
	@Id @Index @Getter
	private String clientPageId;

	@Getter
	private FbPage clientPage;
	
	@Getter
	private List<FbPage> includedPages = new ArrayList<FbPage>();

	@Getter
	private List<FbPage> excludedPages = new ArrayList<FbPage>();

	
	public ClientPageData() { }
	

	public ClientPageData(FbPage clientPageDetails) {
		this.clientPage = clientPageDetails;
		this.clientPageId = clientPageDetails.getPageId();
	}

	public boolean addPage(FbPage page) {
		boolean added = false;
		if(!includedPages.contains(page)){
			includedPages.add(page);
			added = true;
		}
		if(excludedPages.contains(page)){
			excludedPages.remove(page);
		}
		return added;
	}
	
	public boolean excludePage(FbPage page) {
		
		boolean excluded = false;
		if(includedPages.contains(page)){
			includedPages.remove(page);
			excluded = true;
		}
		if(!excludedPages.contains(page)){
			excludedPages.add(page);
			excluded = true;
		}else{
			excluded = true;
		}
		return excluded;
	}


}
