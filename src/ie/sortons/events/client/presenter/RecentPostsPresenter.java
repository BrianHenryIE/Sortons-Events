package ie.sortons.events.client.presenter;

import ie.sortons.events.client.RpcService;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class RecentPostsPresenter implements Presenter {

	@SuppressWarnings("unused")
	private final RpcService dao;
	
	private final Display display;
	
	List<String> posts = new ArrayList<String>();
	
	public interface Display {

		void setPosts(List<String> posts);

		void setPresenter(RecentPostsPresenter presenter);
		
		Widget asWidget();
	}
	
	public RecentPostsPresenter(final RpcService dao, Display view){
		this.dao = dao;
		this.display = view;
		System.out.println("RecentPostsPresenter constructor");
		view.setPresenter(this);
		
		
		
		posts.add("http://www.facebook.com/MillTheatreDundrum/posts/10153726715485231");
		posts.add("http://www.facebook.com/oreillytheatre/posts/10151968187323412");
		posts.add("http://www.facebook.com/MillTheatreDundrum/posts/621496674554574");
		posts.add("http://www.facebook.com/ActingOutTheatreGroup/posts/10151865715676246");
		posts.add("http://www.facebook.com/NAYDIreland/posts/10151936838097321");
		posts.add("http://www.facebook.com/photo.php?fbid=10151975382927991&set=a.10150633303077991.390954.63888647990&type=1");
		posts.add("http://www.facebook.com/photo.php?fbid=10152125809770660&set=a.10150755603315660.462898.200269315659&type=1");
		posts.add("http://www.facebook.com/permalink.php?story_fbid=10152157563183189&id=219036148107167");
		posts.add("http://www.facebook.com/RoughMagicTheatreCompany/posts/714524001384");
		posts.add("http://www.facebook.com/dublinfringefestival/posts/10152799050485639");
		posts.add("http://www.facebook.com/photo.php?fbid=705312786167351&set=a.414237871941512.97514.210016822363619&type=1");
		posts.add("http://www.facebook.com/atenderthing/posts/10153549246732355");
		posts.add("http://www.facebook.com/oreillytheatre/posts/10151967745403412");
		posts.add("http://www.facebook.com/photo.php?fbid=10152167262747929&set=a.10150735927262929.422573.199758082928&type=1");
		posts.add("http://www.facebook.com/TheLirAcademy/posts/261042270729379");
		posts.add("http://www.facebook.com/dublinfringefestival/posts/10152798872380639");
		posts.add("http://www.facebook.com/Gatetheatre/posts/10153808192690437");
		posts.add("http://www.facebook.com/permalink.php?story_fbid=700368363341097&id=138069587125");
		posts.add("http://www.facebook.com/permalink.php?story_fbid=10152157264418189&id=219036148107167");
		posts.add("http://www.facebook.com/MillTheatreDundrum/posts/286343104849549");
		posts.add("http://www.facebook.com/ProjectArtsCentreDublin/posts/10100798975159187");
		posts.add("http://www.facebook.com/OperaTheatreCompany/posts/10152233661296563");
		posts.add("http://www.facebook.com/smashingtimestheatrecompany/posts/10152228722582068");
		posts.add("http://www.facebook.com/atenderthing/posts/10202715985477323");
		posts.add("http://www.facebook.com/isacs.ie/posts/10203138282727402");
		posts.add("http://www.facebook.com/ProjectArtsCentreDublin/posts/10151863457850060");
		posts.add("http://www.facebook.com/photo.php?fbid=652845058091240&set=a.538516492857431.1073741827.536057743103306&type=1");
		posts.add("http://www.facebook.com/isacs.ie/posts/10203138244526447");
		posts.add("http://www.facebook.com/thegaietytheatredublin/posts/10151869863485636");
		posts.add("http://www.facebook.com/abbeytheatredublin/posts/747330255285818");
		
		
		
		display.setPosts(posts);
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

		System.out.println("getting more posts!");
		
		posts.add("http://www.facebook.com/MillTheatreDundrum/posts/10153726715485231");
		posts.add("http://www.facebook.com/oreillytheatre/posts/10151968187323412");
		posts.add("http://www.facebook.com/MillTheatreDundrum/posts/621496674554574");
		posts.add("http://www.facebook.com/ActingOutTheatreGroup/posts/10151865715676246");
		posts.add("http://www.facebook.com/NAYDIreland/posts/10151936838097321");
		posts.add("http://www.facebook.com/photo.php?fbid=10151975382927991&set=a.10150633303077991.390954.63888647990&type=1");
		posts.add("http://www.facebook.com/photo.php?fbid=10152125809770660&set=a.10150755603315660.462898.200269315659&type=1");
		posts.add("http://www.facebook.com/permalink.php?story_fbid=10152157563183189&id=219036148107167");
		posts.add("http://www.facebook.com/RoughMagicTheatreCompany/posts/714524001384");
		posts.add("http://www.facebook.com/dublinfringefestival/posts/10152799050485639");
		posts.add("http://www.facebook.com/photo.php?fbid=705312786167351&set=a.414237871941512.97514.210016822363619&type=1");
		posts.add("http://www.facebook.com/atenderthing/posts/10153549246732355");
		posts.add("http://www.facebook.com/oreillytheatre/posts/10151967745403412");
		posts.add("http://www.facebook.com/photo.php?fbid=10152167262747929&set=a.10150735927262929.422573.199758082928&type=1");
		posts.add("http://www.facebook.com/TheLirAcademy/posts/261042270729379");
		posts.add("http://www.facebook.com/dublinfringefestival/posts/10152798872380639");
		posts.add("http://www.facebook.com/Gatetheatre/posts/10153808192690437");
		posts.add("http://www.facebook.com/permalink.php?story_fbid=700368363341097&id=138069587125");
		posts.add("http://www.facebook.com/permalink.php?story_fbid=10152157264418189&id=219036148107167");
		posts.add("http://www.facebook.com/MillTheatreDundrum/posts/286343104849549");
		posts.add("http://www.facebook.com/ProjectArtsCentreDublin/posts/10100798975159187");
		posts.add("http://www.facebook.com/OperaTheatreCompany/posts/10152233661296563");
		posts.add("http://www.facebook.com/smashingtimestheatrecompany/posts/10152228722582068");
		posts.add("http://www.facebook.com/atenderthing/posts/10202715985477323");
		posts.add("http://www.facebook.com/isacs.ie/posts/10203138282727402");
		posts.add("http://www.facebook.com/ProjectArtsCentreDublin/posts/10151863457850060");
		posts.add("http://www.facebook.com/photo.php?fbid=652845058091240&set=a.538516492857431.1073741827.536057743103306&type=1");
		posts.add("http://www.facebook.com/isacs.ie/posts/10203138244526447");
		posts.add("http://www.facebook.com/thegaietytheatredublin/posts/10151869863485636");
		posts.add("http://www.facebook.com/abbeytheatredublin/posts/747330255285818");
		
	}

}
