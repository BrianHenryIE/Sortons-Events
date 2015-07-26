package ie.sortons.events.shared.dto;

import ie.sortons.events.shared.ClientPageData;

import java.util.List;

/**
 * Neither Cloud Endpoints nor GwtProJsonSerializer seem to support generics
 * 
 * @author brianhenry
 *
 */
public class ClientPageDataResponse extends SEResponse<ClientPageData> {

	private List<ClientPageData> data;

	public List<ClientPageData> getData() {
		return data;
	}
	
	public void setData(List<ClientPageData> data) {
		this.data = data;
	}
	
}
