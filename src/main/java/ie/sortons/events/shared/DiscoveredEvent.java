package ie.sortons.events.shared;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.gwt.core.shared.GwtIncompatible;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.kfuntak.gwt.json.serialization.client.JsonSerializable;
import com.kfuntak.gwt.json.serialization.client.SkipNullSerialization;

import ie.sortons.gwtfbplus.shared.domain.graph.GraphEvent;
import ie.sortons.gwtfbplus.shared.domain.graph.GraphPlace;

@Entity
@Cache
@SkipNullSerialization
public class DiscoveredEvent implements JsonSerializable {

	@GwtIncompatible
	@ApiResourceProperty(name = "class")
	public final String classname = "ie.sortons.events.shared.DiscoveredEvent";

	@Id
	private String id;

	private String eventId;

	@Index
	private Long clientId;

	private List<SourcePage> sourcePages = new ArrayList<SourcePage>();

	private String name;
	private String location;
	private String locationId;

	private Double latitude;
	private Double longitude;

	@Index
	Date startTime;
	Date endTime;

	/**
	 * No args constructor for Objectify etc
	 */
	public DiscoveredEvent() {
	}

	public DiscoveredEvent(GraphEvent fbEvent, Long clientId, Set<SourcePage> eventSourcePages) {
		setEvent(fbEvent);
		setClientId(clientId);
		sourcePages.addAll(eventSourcePages);
		updateDatastoreId();
	}

	// TODO: having clientId and sourcePage is redundant. Is it possible to have
	// a
	// sourcePage without a clientId in it? (parent)
	public DiscoveredEvent(String eventId, SourcePage sourcePage) {
		this.eventId = eventId;
		this.clientId = sourcePage.getClientId();
		addSourcePage(sourcePage);
		updateDatastoreId();
	}

	/**
	 * Copy constructor
	 * 
	 * @param dEvent
	 * @param sourcePages
	 */
	public DiscoveredEvent(DiscoveredEvent dEvent, List<SourcePage> sourcePages) {
		this.eventId = dEvent.getEventId();
		this.clientId = dEvent.getClientId();
		this.name = dEvent.getName();
		this.location = dEvent.getLocation();
		this.locationId = dEvent.getLocationId();
		this.startTime = dEvent.getStartTime();
		this.endTime = dEvent.getEndTime();
		this.longitude = dEvent.getLongitude();
		this.latitude = dEvent.getLatitude();
		this.sourcePages = sourcePages;
	}

	public String getLocationId() {
		return locationId;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public DiscoveredEvent(GraphEvent graphEvent, Long clientId, List<SourcePage> sourcePages) {
		this.clientId = clientId;
		this.sourcePages = sourcePages;
		setEvent(graphEvent);
	}

	public void setEvent(GraphEvent fbEvent) {
		this.eventId = fbEvent.getId();
		this.name = fbEvent.getName();
		this.startTime = fbEvent.getStart_time();
		this.endTime = fbEvent.getEnd_time();
		if (fbEvent.getPlace() != null)
			this.setLocationPlace(fbEvent.getPlace());
	}

	private void setLocationPlace(GraphPlace fbPlace) {

		this.locationId = fbPlace.getId();
		this.location = fbPlace.getName();

		if (fbPlace.getLocation() != null) {
			this.latitude = fbPlace.getLocation().getLatitude();
			this.longitude = fbPlace.getLocation().getLongitude();

			if (fbPlace.getLocation().getStreet() != null)
				this.location += ", " + fbPlace.getLocation().getStreet();
			if (fbPlace.getLocation().getCity() != null)
				this.location += ", " + fbPlace.getLocation().getCity();
			if (fbPlace.getLocation().getState() != null)
				this.location += ", " + fbPlace.getLocation().getState();
			if (fbPlace.getLocation().getZip() != null)
				this.location += ", " + fbPlace.getLocation().getZip();
		}
	}

	// Encapsulating in preparation for fql deprecation
	public String getName() {
		return name;
	}

	public String getLocation() {
		return location;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public String getEventId() {
		return eventId;
	}

	public List<SourcePage> getSourcePages() {
		return sourcePages;
	}

	public boolean hasSourcePage(SourcePage fbPage) {
		return (sourcePages.contains(fbPage));
	}

	public boolean addSourcePage(SourcePage sourcePage) {
		if (!sourcePages.contains(sourcePage)) {
			sourcePages.add(sourcePage);
			return true;
		}
		return false;
	}

	public boolean addSourcePages(List<SourcePage> newSourcePages) {
		boolean changed = false;
		for (SourcePage newSourcePage : newSourcePages) {
			if (addSourcePage(newSourcePage))
				changed = true;
		}
		return changed;
	}

	public Long getClientId() {
		return clientId;
	}

	public boolean isDateOnly() {
		return false;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
		updateDatastoreId();
	}

	public void setClientIdFromCPD(ClientPageData clientPageData) {
		setClientId(clientPageData.getClientPageId());
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
		updateDatastoreId();
	}

	public void setSourcePages(List<SourcePage> sourcePages) {
		this.sourcePages = sourcePages;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public void updateDatastoreId() {
		this.id = clientId + "" + eventId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
		result = prime * result + ((eventId == null) ? 0 : eventId.hashCode());
		result = prime * result + ((latitude == null) ? 0 : latitude.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((locationId == null) ? 0 : locationId.hashCode());
		result = prime * result + ((longitude == null) ? 0 : longitude.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((sourcePages == null) ? 0 : sourcePages.hashCode());
		result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
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
		DiscoveredEvent other = (DiscoveredEvent) obj;
		if (clientId == null) {
			if (other.clientId != null)
				return false;
		} else if (!clientId.equals(other.clientId))
			return false;
		if (endTime == null) {
			if (other.endTime != null)
				return false;
		} else if (!endTime.equals(other.endTime))
			return false;
		if (eventId == null) {
			if (other.eventId != null)
				return false;
		} else if (!eventId.equals(other.eventId))
			return false;
		if (latitude == null) {
			if (other.latitude != null)
				return false;
		} else if (!latitude.equals(other.latitude))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (locationId == null) {
			if (other.locationId != null)
				return false;
		} else if (!locationId.equals(other.locationId))
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
		if (sourcePages == null) {
			if (other.sourcePages != null)
				return false;
		}
		if (!(sourcePages.size() == other.sourcePages.size())) {
			return false;
		}
		if (!sourcePages.containsAll(other.sourcePages)) {
			return false;
		}
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		return true;
	}

}
