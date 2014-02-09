package ie.sortons.events.shared;

import ie.sortons.gwtfbplus.shared.domain.fql.FqlPage;

import java.util.ArrayList;
import java.util.List;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.gwt.core.shared.GwtIncompatible;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;
import com.kfuntak.gwt.json.serialization.client.SkipNullSerialization;

@Entity @SkipNullSerialization
public class ClientPageData implements JsonSerializable {

	
	/**
	 * @return the pageAdmins
	 */
	@GwtIncompatible
	public List<Long> getPageAdmins() {
		return pageAdmins;
	}

	/**
	 * @param pageAdmins the pageAdmins to set
	 */
	@GwtIncompatible
	public boolean addPageAdmin(Long admin) {
		return pageAdmins.add(admin);
	}

	@GwtIncompatible @ApiResourceProperty(name = "class")
	public final String classname = "ie.sortons.events.shared.ClientPageData";

	// this will be either a page id or an app id or an interest list??
	@Id
	@Index
	public Long clientPageId;

	public FqlPage clientPage;

	@GwtIncompatible @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	public List<Long> pageAdmins = new ArrayList<Long>();
	
	public List<FqlPage> includedPages = new ArrayList<FqlPage>();

	public List<FqlPage> ignoredPages = new ArrayList<FqlPage>();

	@Ignore
	public List<FqlPage> suggestedPages = new ArrayList<FqlPage>();

	public ClientPageData() {
	}

	public ClientPageData(FqlPage clientPageDetails) {
		this.clientPage = clientPageDetails;
		this.clientPageId = clientPageDetails.getPageId();
		addPage(clientPageDetails);
	}

	public Long getClientPageId() {
		return this.clientPageId;
	}

	public FqlPage getClientPage() {
		return this.clientPage;
	}

	public List<FqlPage> getIncludedPages() {
		return new ArrayList<FqlPage>(includedPages);
	}

	public List<FqlPage> getIgnoredPages() {
		return new ArrayList<FqlPage>(ignoredPages);
	}

	public void setSuggestedPages(List<FqlPage> suggestedPages) {
		this.suggestedPages = suggestedPages;
	}

	public List<FqlPage> getSuggestedPages() {
		return suggestedPages;
	}

	public boolean addPage(FqlPage page) {
		boolean added = false;
		if (page != null && !includedPages.contains(page)) {
			includedPages.add(page);
			added = true;
		}
		if (ignoredPages.contains(page)) {
			ignoredPages.remove(page);
		}
		return added;
	}

	// TODO
	public boolean ignorePage(FqlPage page) {

		boolean excluded = false;
		if (includedPages.contains(page)) {
			includedPages.remove(page);
			excluded = true;
		}
		if (page != null && !ignoredPages.contains(page)) {
			ignoredPages.add(page);
			excluded = true;
		}
		return excluded;
	}

	public FqlPage getPageById(Long pageId) {
		FqlPage thePage = null;
		for (FqlPage page : includedPages)
			if (page.getPageId().equals(pageId))
				thePage = page;
		return thePage;
	}

	public List<Long> getIncludedPageIds() {
		List<Long> pageIds = new ArrayList<Long>();
		for (FqlPage page : includedPages)
			if (page != null)
				pageIds.add(page.getPageId());
		return pageIds;
	}

	public List<Long> getIgnoredPageIds() {
		List<Long> pageIds = new ArrayList<Long>();
		for (FqlPage page : ignoredPages) {
			pageIds.add(page.getPageId());
		}
		return pageIds;
	}

}