package ie.sortons.events.client.view;

import ie.brianhenry.gwtbingmaps.client.BingMap;
import ie.sortons.events.client.view.widgets.MyEventWidget;
import ie.sortons.gwtfbplus.client.api.Canvas;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlEvent;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlEventMember;

import java.util.List;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

public class MyEventsView extends Composite {

	private FlowPanel panel = new FlowPanel();

	private FlowPanel pagesList = new FlowPanel();

	private BingMap map;

	private FlowPanel list = new FlowPanel();

	private FlowPanel left = new FlowPanel();
	private FlowPanel right = new FlowPanel();


	public MyEventsView() {

	


		initWidget(panel);

	}


	public void setEvents(List<FqlEvent> events) {


		for(FqlEvent e : events){
			panel.add(new MyEventWidget(e));
		}

		Timer timer = new Timer() {
			public void run() {
				Canvas.setSize();
			}
		};
		timer.schedule(1000);
	}


	public void setInvitees(List<FqlEventMember> events) {
		
		// TODO
		
	}
}