package ie.sortons.events.client.widgets;

import ie.sortons.events.client.EventOverlay;
import ie.sortons.events.client.EventOverlay.FbPage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class EventWidget extends Composite {

	private static EventWidgetUiBinder uiBinder = GWT
			.create(EventWidgetUiBinder.class);

	interface EventWidgetUiBinder extends UiBinder<Widget, EventWidget> {
	}


	@UiField Anchor eventLink;

	@UiField Image eventPicture;

	@UiField Label startTime;
	
	@UiField Label location;
	
	@UiField FlowPanel pages;
	
	// TODO: Editor framework
	public EventWidget(EventOverlay rowEvent) {
	
		initWidget(uiBinder.createAndBindUi(this));
		
		eventLink.setText(rowEvent.getName());
		eventLink.setHref("http://www.facebook.com/event.php?eid="  + rowEvent.getEid());
		eventLink.setTarget("_blank");
		eventPicture.setUrl(rowEvent.getPic_square());
	    startTime.setText(rowEvent.getStartTimeString());
	    location.setText(rowEvent.getLocation());

	    for(FbPage page : rowEvent.getFbPagesDetail()) {
	    	
	    	Image image = new Image("//graph.facebook.com/" + page.getId() + "/picture?type=square");
	    	image.setHeight("25px");
	    	image.setWidth("25px");
	    	Link link = new Link(page.getLink(), image, page.getName());
	    	link.getElement().getStyle().setMarginLeft(10, Unit.PX);
	    	link.setTarget("_blank");
	    	
	    	pages.add(link); 

	    }
		
	}

}
