package ie.sortons.events.shared;

import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.gwt.core.shared.GwtIncompatible;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;
import com.kfuntak.gwt.json.serialization.client.SkipNullSerialization;

import ie.sortons.gwtfbplus.client.widgets.suggestbox.FbSearchable;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlPage;

/**
 * @see http://developers.facebook.com/docs/reference/fql/page/
 */
@Entity
@Cache
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
		this.pageUrl = fqlPage.getPageUrl() != null ? fqlPage.getPageUrl() : null;

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

	public void setId() {
		this.id = this.clientId + "" + this.pageId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((about == null) ? 0 : about.hashCode());
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((latitude == null) ? 0 : latitude.hashCode());
		result = prime * result + ((longitude == null) ? 0 : longitude.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((pageId == null) ? 0 : pageId.hashCode());
		result = prime * result + ((pageUrl == null) ? 0 : pageUrl.hashCode());
		result = prime * result + ((phone == null) ? 0 : phone.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((street == null) ? 0 : street.hashCode());
		result = prime * result + ((zip == null) ? 0 : zip.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SourcePage other = (SourcePage) obj;
		if (about == null) {
			if (other.about != null)
				return false;
		} else if (!about.equals(other.about))
			return false;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (clientId == null) {
			if (other.clientId != null)
				return false;
		} else if (!clientId.equals(other.clientId))
			return false;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (latitude == null) {
			if (other.latitude != null)
				return false;
		} else if (!latitude.equals(other.latitude))
			return false;
		if (longitude == null) {
			if (other.longitude != null)
				return false;
		} else if (!longitude.equals(other.longitude))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (pageId == null) {
			if (other.pageId != null)
				return false;
		} else if (!pageId.equals(other.pageId))
			return false;
		if (pageUrl == null) {
			if (other.pageUrl != null)
				return false;
		} else if (!pageUrl.equals(other.pageUrl))
			return false;
		if (phone == null) {
			if (other.phone != null)
				return false;
		} else if (!phone.equals(other.phone))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (street == null) {
			if (other.street != null)
				return false;
		} else if (!street.equals(other.street))
			return false;
		if (zip == null) {
			if (other.zip != null)
				return false;
		} else if (!zip.equals(other.zip))
			return false;
		return true;
	}


}
