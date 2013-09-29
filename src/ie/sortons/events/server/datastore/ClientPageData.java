package ie.sortons.events.server.datastore;

import ie.sortons.events.shared.DsFbPage;

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
	private DsFbPage clientPage;
	
	@Getter
	private List<DsFbPage> includedPages = new ArrayList<DsFbPage>();

	@Getter
	private List<DsFbPage> excludedPages = new ArrayList<DsFbPage>();

	
	public ClientPageData() { }
	

	public ClientPageData(DsFbPage clientPageDetails) {
		this.clientPage = clientPageDetails;
		this.clientPageId = clientPageDetails.getPageId();
	}

	public boolean addPage(DsFbPage page) {
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
	
	public boolean excludePage(DsFbPage page) {
		
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
