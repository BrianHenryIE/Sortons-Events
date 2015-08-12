package ie.sortons.events.shared;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	@GwtIncompatible // @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Set<Long> pageAdmins = new HashSet<Long>();
	
	@Ignore
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



	// TODO Are these next three needed?
	public Long getClientPageId() {
		return this.clientPageId;
	}

	public String getName(){
		return clientPage.getName();
	}
	
	public String getPageUrl() {
		return clientPage.getPageUrl();
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

	// Ignored because ClientEndpoints doesn't need to generate the data, if a client needs it, it can
	// but fock, there's no source so we can't compile this if we try this
	//@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public List<Long> getIncludedPageIds() {
		List<Long> pageIds = new ArrayList<Long>();
		for (SourcePage page : includedPages)
			if (page != null)
				pageIds.add(page.getPageId());
		return pageIds;
	}
	
	//@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public List<SourcePage> getIncludedPages() {
		// TODO why is this a new ArrayList?
		return new ArrayList<SourcePage>(includedPages);
	}

	//@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public Map<Long, SourcePage> getIncludedIdsPagesMap() {

		Map<Long, SourcePage> sourceClientPages = new HashMap<Long, SourcePage>();

		for (SourcePage page :includedPages)
			sourceClientPages.put(page.getPageId(), page);

		return sourceClientPages;
	}

	
	// public getters and setters for serialization
	
	public SourcePage getClientPage() {
		return clientPage;
	}

	public void setClientPage(SourcePage clientPage) {
		this.clientPage = clientPage;
	}

	public void setClientPageId(Long clientPageId) {
		this.clientPageId = clientPageId;
	}
	
	public void setIncludedPages(List<SourcePage> includedPages) {
		this.includedPages = includedPages;
	}

	
	/**
	 * @return the pageAdmins
	 */
	@GwtIncompatible
	public Set<Long> getPageAdmins() {
		return pageAdmins;
	}

	@GwtIncompatible
	public void setPageAdmins(Set<Long> pageAdmins) {
		this.pageAdmins = pageAdmins;
	}
	
	/**
	 * @param pageAdmins the pageAdmins to set
	 */
	@GwtIncompatible
	public boolean addPageAdmin(Long admin) {
		return pageAdmins.add(admin);
	}


	
}