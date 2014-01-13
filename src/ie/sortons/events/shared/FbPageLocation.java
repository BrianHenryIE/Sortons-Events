package ie.sortons.events.shared;

import com.googlecode.objectify.annotation.Embed;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

/**
 * The location of this place. Applicable to all Places
 * 
 * @author brianhenry
 * 
 */
@Embed
public class FbPageLocation implements JsonSerializable {

	/**
	 * @return the Street of the location
	 */
	public String getStreet() {
		return street;
	}

	/**
	 * @return the City of the location
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @return the State of the location
	 */
	public String getState() {
		return state;
	}

	/**
	 * @return the Country of the location
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @return the Zip code of the location
	 */
	public String getZip() {
		return zip;
	}

	/**
	 * @return the Latitude of the location
	 */
	public String getLatitude() {
		// return (latitude == null ? null : Double.parseDouble(latitude));
		return latitude;
	}

	/**
	 * @return the Longitude of the location
	 */
	public String getLongitude() {
		// return (longitude == null ? null : Double.parseDouble(longitude));
		return longitude;
	}

	/**
	 * @return the ID of the location
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the Name of the location
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the ID of the parent location of this location
	 */
	public String getLocatedIn() {
		return located_in;
	}

	public String friendlyString() {
		String location = "";

		if (street != null && !street.trim().equals("")) {
			location += street;
		}

		if (city != null && !city.trim().equals("")) {
			if(!location.equals(""))
				location += ", "; 
			location += city;
		}

		if (state != null && !state.trim().equals("")) {
			if(!location.equals(""))
				location += ", "; 
			location += state;
		}

		if (zip != null && !zip.trim().equals("")) {
			if(!location.equals(""))
				location += ", "; 
			location += zip;
		}

		if (country != null && !country.trim().equals("")) {
			if(!location.equals(""))
				location += ", "; 
			location += country;
		}

		return location;
	}

	public String street;
	public String city;
	public String state;
	public String country;
	public String zip;
	public String latitude;
	public String longitude;
	public String id;
	public String name;
	public String located_in;

}