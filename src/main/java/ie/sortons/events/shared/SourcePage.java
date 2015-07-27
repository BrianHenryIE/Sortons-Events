package ie.sortons.events.shared;

import ie.sortons.gwtfbplus.client.widgets.suggestbox.FbSearchable;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlPage;

import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.gwt.core.shared.GwtIncompatible;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;
import com.kfuntak.gwt.json.serialization.client.SkipNullSerialization;

/**
 * @see http://developers.facebook.com/docs/reference/fql/page/
 */
@Entity
@SkipNullSerialization
public class SourcePage implements JsonSerializable, Comparable<SourcePage>, FbSearchable {

	@GwtIncompatible
	@ApiResourceProperty(name = "class")
	public final String classname = "ie.sortons.events.shared.SourcePage";

	@Index
	private Long clientId;
	
	/**
	 * A concatenation of the clientPageId and the SourcePage pageId
	 */
	@Id
	private String id;
	
	private String about;

	private String name;
	
	private Long pageId;

	// private String parent_page;

	private String phone;
	private String pageUrl;
	private String street;

	private String city;
	private String country;
	private Double latitude;
	private Double longitude;

	private String zip;

	private String state;

	// Needed on line 78, 245 ClientPageDataEndpoint
	public SourcePage(FqlPage fqlPage) {
		this.about = fqlPage.getAbout();

		this.street = fqlPage.getLocation().getStreet();
		this.city = fqlPage.getLocation().getCity();
		this.zip = fqlPage.getLocation().getZip();
		this.state = fqlPage.getLocation().getState();
		this.country = fqlPage.getLocation().getCountry();
		this.latitude = fqlPage.getLocation().getLatitude();
		this.longitude = fqlPage.getLocation().getLongitude();

		// this.parent_page = fqlPage.getParent_Page();
		this.phone = fqlPage.getPhone() != null ? fqlPage.getPhone() : null;
		this.pageUrl = fqlPage.getPageUrl() !=  null ? fqlPage.getPageUrl() : null;

		this.name = fqlPage.getName();
		this.pageId = fqlPage.getPageId();
		this.pageUrl = fqlPage.getPageUrl();
		
		setId();
	}

	public SourcePage() {
	}

	public SourcePage(String name, Long id, String link) {
		this.name = name;
		this.pageId = id;
		this.pageUrl = link;
		
		setId();
	}

	public String getName() {
		return name;
	}

	public Long getPageId() {
		return pageId;
	}

	public String getPageUrl() {
		return pageUrl;
	}

	public String getAbout() {

		return about;
	}

	public String getPhone() {
		return phone;
	}

	public String getStreet() {
		return street;
	}

	public String getCity() {
		return city;
	}

	public String getCountry() {
		return country;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public String getZip() {
		return zip;
	}

	public String getState() {
		return state;
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof SourcePage))
			return false;
		return compareTo((SourcePage) obj) == 0;
	}

	@Override
	public final int hashCode() {
		return pageId.hashCode();
	}

	@Override
	public int compareTo(SourcePage other) {
		return this.pageId.compareTo(other.getPageId());
	}

	// FbSearchable interface for suggestbox

	public String getTitle() {
		return name;
	}

	public String getSubTitle() {
		// return (getLocation().getCity() != null ? getLocation().getCity() :
		// "");
		return getFriendlyLocationString();
	}

	public Long getUid() {
		return pageId;
	}

	public String getSearchableString() {
		return name + " " + getCity() + " " + getCountry() + " " + getName() + " " + getState() + " " + getStreet();
	}

	// public getters and setters are needed for the serialization

	public void setAbout(String about) {
		this.about = about;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPageId(Long pageId) {
		this.pageId = pageId;
		setId();
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getFriendlyLocationString() {

		String location = "";

		if (street != null && !street.trim().equals("")) {
			location += street;
		}

		if (city != null && !city.trim().equals("")) {
			if (!location.equals(""))
				location += ", ";
			location += city;
		}

		if (state != null && !state.trim().equals("")) {
			if (!location.equals(""))
				location += ", ";
			location += state;
		}

		if (zip != null && !zip.trim().equals("")) {
			if (!location.equals(""))
				location += ", ";
			location += zip;
		}

		if (country != null && !country.trim().equals("")) {
			if (!location.equals(""))
				location += ", ";
			location += country;
		}

		location = location.replace(" ,", ",");
		location = location.replace(",,", ",");

		return location;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
		setId();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void setId(Long clientPageId, Long sourcePageId) {
		this.id = clientPageId + "" + sourcePageId;
	}
	
	public void setId(){
		this.id = this.clientId + "" + this.pageId;
	}

}
