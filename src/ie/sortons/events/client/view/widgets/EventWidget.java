package ie.sortons.events.client.view.widgets;

import ie.sortons.events.client.view.overlay.FbEventOverlay;
import ie.sortons.events.client.view.overlay.FbEventOverlay.FbPage;
import ie.sortons.gwtfbplus.client.newresources.Resources;
import ie.sortons.gwtfbplus.client.widgets.Link;
import ie.sortons.gwtfbplus.client.widgets.popups.ToolTipPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
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
	public EventWidget(FbEventOverlay rowEvent) {
	
		GWT.<Resources>create(Resources.class).newCss().ensureInjected();
		
		
		
		
		initWidget(uiBinder.createAndBindUi(this));
		
		eventLink.setText(rowEvent.getName());
		eventLink.setHref("http://www.facebook.com/event.php?eid="  + rowEvent.getEid());
		eventLink.setTarget("_blank");
		eventPicture.setUrl("//graph.facebook.com/" + rowEvent.getEid() + "/picture?type=square");
	    startTime.setText(rowEvent.getStartTimeString());
	    location.setText(rowEvent.getLocation());

	   
	    pages.clear();
	    
	    for(FbPage page : rowEvent.getFbPagesDetail()) {
	    	
	    	Image pageImage = new Image("//graph.facebook.com/" + page.getId() + "/picture?type=square");
	    	pageImage.setHeight("25px");
	    	pageImage.setWidth("25px");
	    	
	    	ToolTipPanel pageImageToolTip = new ToolTipPanel(page.getName(), pageImage);
	    	pageImageToolTip.getElement().getStyle().setMarginLeft(10, Unit.PX);
	    	pageImageToolTip.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
	    	
	    	Link pageLink = new Link(page.getLink(), pageImageToolTip);
	    	pageLink.setTarget("_blank");
	    	
	    	pages.add(pageLink); 

	    }
		
	}

}
