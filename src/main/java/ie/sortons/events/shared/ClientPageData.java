package ie.sortons.events.shared;


import java.util.ArrayList;
import java.util.List;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.gwt.core.shared.GwtIncompatible;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;
import com.kfuntak.gwt.json.serialization.client.SkipNullSerialization;

@Cache @Entity @SkipNullSerialization
public class ClientPageData implements JsonSerializable {

	@GwtIncompatible @ApiResourceProperty(name = "class")
	public final String classname = "ie.sortons.events.shared.ClientPageData";

	
	// this will be either a page id or an app id or an interest list??
	@Id
	@Index
	private Long clientPageId;

	private SourcePage clientPage;

	@GwtIncompatible @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private List<Long> pageAdmins = new ArrayList<Long>();
	
	private List<SourcePage> includedPages = new ArrayList<SourcePage>();

	@Ignore
	private List<SourcePage> suggestedPages = new ArrayList<SourcePage>();

	
	public ClientPageData() {
	}

	public ClientPageData(SourcePage clientPageDetails) {
		this.clientPage = clientPageDetails;
		this.clientPageId = clientPageDetails.getPageId();
		addPage(clientPageDetails);
	}


	/**
	 * @param pageAdmins the pageAdmins to set
	 */
	@GwtIncompatible
	public boolean addPageAdmin(Long admin) {
		return pageAdmins.add(admin);
	}
	
	public Long getClientPageId() {
		return this.clientPageId;
	}

	public List<SourcePage> getIncludedPages() {
		// TODO why is this a new ArrayList?
		return new ArrayList<SourcePage>(includedPages);
	}

	public String getName(){
		return clientPage.getName();
	}
	
	public String getPageUrl() {
		return clientPage.getPageUrl();
	}
	
	/**
	 * @return the pageAdmins
	 */
	@GwtIncompatible
	public List<Long> getPageAdmins() {
		return pageAdmins;
	}

	public void setSuggestedPages(List<SourcePage> suggestedPages) {
		this.suggestedPages = suggestedPages;
	}

	public List<SourcePage> getSuggestedPages() {
		return suggestedPages;
	}

	public boolean addPage(SourcePage page) {
		boolean added = false;
		if (page != null && !includedPages.contains(page)) {
			includedPages.add(page);
			added = true;
		}
		return added;
	}

	// TODO
	public boolean removePage(SourcePage page) {

		boolean excluded = false;
		if (includedPages.contains(page)) {
			includedPages.remove(page);
			excluded = true;
		}
	
		return excluded;
	}

	public SourcePage getPageById(Long pageId) {
		SourcePage thePage = null;
		for (SourcePage page : includedPages)
			if (page.getPageId().equals(pageId))
				thePage = page;
		return thePage;
	}

	// Ignored because the generated data will never be accessed by the getter
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public List<Long> getIncludedPageIds() {
		List<Long> pageIds = new ArrayList<Long>();
		for (SourcePage page : includedPages)
			if (page != null)
				pageIds.add(page.getPageId());
		return pageIds;
	}

}