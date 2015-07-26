package ie.sortons.events.shared.dto;

import ie.sortons.events.shared.WallPost;

import java.util.List;

/**
 * Neither Cloud Endpoints nor GwtProJsonSerializer seem to support generics
 * 
 * @author brianhenry
 *
 */
public class RecentPostsResponse extends SEResponse<WallPost> {

	private List<WallPost> data;
	
	public List<WallPost> getData() {
		return data;
	}
	
	public void setData(List<WallPost> data) {
		this.data = data;
	}
	
}
