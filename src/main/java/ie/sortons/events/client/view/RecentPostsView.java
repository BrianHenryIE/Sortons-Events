package ie.sortons.events.client.view;

import ie.sortons.events.client.presenter.RecentPostsPresenter;
import ie.sortons.gwtfbplus.client.api.Canvas;
import ie.sortons.gwtfbplus.client.api.Canvas.PageInfo;
import ie.sortons.gwtfbplus.client.widgets.EmbeddedPost;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

public class RecentPostsView extends Composite implements RecentPostsPresenter.Display {

	FlowPanel panel = new FlowPanel();

	FlowPanel left = new FlowPanel();
	FlowPanel right = new FlowPanel();

	List<String> posts = new ArrayList<String>();
	private int i = 0;

	private RecentPostsPresenter presenter;

	/**
	 * 
	 */
	public RecentPostsView() {

		initWidget(panel);

		left.getElement().getStyle().setWidth(390, Unit.PX);
		left.getElement().getStyle().setLeft(0, Unit.PX);

		right.getElement().getStyle().setWidth(390, Unit.PX);
		right.getElement().getStyle().setRight(0, Unit.PX);
		right.getElement().getStyle().setTop(0, Unit.PX);
		right.getElement().getStyle().setPosition(Position.ABSOLUTE);

		panel.add(left);
		panel.add(right);

		infiniteScroll.run();

	}

	int lowestScrollPos = 0;
	PageInfo info;

	// If we've scrolled down, do stuff
	Timer infiniteScroll = new Timer() {

		@Override
		public void run() {

			Canvas.getPageInfo(new AsyncCallback<PageInfo>() {

				@Override
				public void onFailure(Throwable caught) {
					System.out.println("fail");
				}

				@Override
				public void onSuccess(PageInfo info) {
					RecentPostsView.this.info = info;

					// If we've scrolled down
					if (info.getScrollTop() > lowestScrollPos || lowestScrollPos == 0) {
						RecentPostsView.this.lowestScrollPos = info.getScrollTop();
						addPostsToPanelWhenNeeded();
					}

					infiniteScroll.schedule(500);
				}
			});
		}
	};

	void addFirst(int n) {
		int stop = Math.min(i + n, posts.size() - 1);
		while (i < stop) {
			EmbeddedPost np;
			np = new EmbeddedPost(posts.get(i), 390, new Command() {

				@Override
				public void execute() {
					Canvas.setSize();
				}

			});
			np.getElement().getStyle().setMarginBottom(25, Unit.PX);
			if (i % 2 == 1)
				left.add(np);
			else
				right.add(np);
			i++;
		}
	}

	// To stop too many posts rendering at once...
	Command nextCallback = new Command() {

		@Override
		public void execute() {
			addPostsToPanelWhenNeeded();
			Canvas.setSize();
		}
	};

	// If we're going to have multiple of these, we might as well spread them
	// out.
	private int numResizeTimers = 1;

	public void addPostsToPanelWhenNeeded() {

		// TODO
		// A call back to the presenter saying we need more posts!!

		if ((panel.getOffsetHeight() - info.getScrollTop()) < (3 * info.getClientHeight())) {

			if (i < posts.size()) {

				EmbeddedPost np = new EmbeddedPost(posts.get(i), 390, nextCallback);
				np.getElement().getStyle().setMarginBottom(25, Unit.PX);

				if (left.getOffsetHeight() <= right.getOffsetHeight())
					left.add(np);
				else
					right.add(np);

				i++;
			} else {
				Timer resize = new Timer() {
					@Override
					public void run() {
						Canvas.setSize(810, panel.getOffsetHeight());
						numResizeTimers--;
					}
				};

				resize.schedule(Math.min(5000, 1000 * numResizeTimers));
				numResizeTimers++;

				if (!waitingForPostsFromPresenter) {
					presenter.getNewPosts();
					waitingForPostsFromPresenter = true;
				} else {
					waitingForPostsFromPresenter = false;
				}
			}
		}

	}

	// This is so we don't query the presenter for more posts while it's waiting
	// for an async task to complete.
	private boolean waitingForPostsFromPresenter = false;

	@Override
	public void setPosts(List<String> posts) {
		this.posts = posts;

		addFirst(12);
	}

	@Override
	public void setPresenter(RecentPostsPresenter presenter) {
		this.presenter = presenter;
	}

}
