package ie.sortons.events.client.presenter;

import ie.sortons.events.client.RpcService;
import ie.sortons.events.shared.WallPost;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class RecentPostsPresenter implements Presenter {

	private final Display display;
	
	List<String> posts = new ArrayList<String>();
	
	public interface Display {

		void setPosts(List<String> posts);

		void setPresenter(RecentPostsPresenter presenter);
		
		Widget asWidget();
	}
	
	public RecentPostsPresenter(final RpcService dao, Display view){
		
		this.display = view;
		System.out.println("RecentPostsPresenter constructor");
		view.setPresenter(this);
		
		
		dao.getWallPostsForPage(new AsyncCallback<List<WallPost>>(){

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(List<WallPost> result) {
				
				List<String> urls = new ArrayList<String>();
				
				for(WallPost post : result)
					urls.add(post.getUrl());
				
				System.out.println(urls.size());
				
				display.setPosts(urls);
				
			}});
		
		
	}
	
	@Override
	public void go(HasWidgets container) {
		container.clear();
		container.add(display.asWidget());
	}
	
	public void getCPD(){
		// ClientPageData cpd = dao.getClientPageData();
	}

	public void getNewPosts() {
		// TODO 
		
		// hit up the dao and get more posts.

		// System.out.println("getting more posts!");
		
	
	}

}
