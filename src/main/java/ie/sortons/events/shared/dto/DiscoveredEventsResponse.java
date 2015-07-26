package ie.sortons.events.shared.dto;

import ie.sortons.events.shared.DiscoveredEvent;

import java.util.List;

/**
 * Neither Cloud Endpoints nor GwtProJsonSerializer seem to support generics
 * 
 * @author brianhenry
 *
 */
public class DiscoveredEventsResponse extends SEResponse<DiscoveredEvent> {

	private List<DiscoveredEvent> data;
	
	public List<DiscoveredEvent> getData() {
		return data;
	}
	
	public void setData(List<DiscoveredEvent> data) {
		this.data = data;
	}
	
}
