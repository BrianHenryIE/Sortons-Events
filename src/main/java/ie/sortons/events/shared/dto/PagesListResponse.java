package ie.sortons.events.shared.dto;

import ie.sortons.events.shared.SourcePage;

import java.util.List;

/**
 * Neither Cloud Endpoints nor GwtProJsonSerializer seem to support generics
 * 
 * and in this case I can't just return Strings!
 * 
 * @author brianhenry
 *
 */
public class PagesListResponse extends SEResponse<SourcePage> {

	private List<SourcePage> data;
	
	public List<SourcePage> getData() {
		return data;
	}
	
	public void setData(List<SourcePage> data) {
		this.data = data;
	}
	
}
