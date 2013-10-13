package ie.sortons.events.client.view.widgets;

import ie.sortons.events.client.view.overlay.FbPageOverlay;
import ie.sortons.events.shared.DiscoveredEvent;
import ie.sortons.events.shared.FbEvent;
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
	public EventWidget(DiscoveredEvent.Overlay rowEvent) {
	
		GWT.<Resources>create(Resources.class).css().ensureInjected();
		

		FbEvent.Overlay fbEvent = rowEvent.getFbEvent();
		
		initWidget(uiBinder.createAndBindUi(this));
		
		eventLink.setText(rowEvent.getFbEvent().getName());
		eventLink.setHref("http://www.facebook.com/event.php?eid="  + fbEvent.getEid());
		eventLink.setTarget("_blank");
		eventPicture.setUrl("//graph.facebook.com/" + fbEvent.getEid() + "/picture?type=square");	
	    startTime.setText(fbEvent.getStartTimeString());
	    location.setText(fbEvent.getLocation());

	   
	    pages.clear();
	    FbPageOverlay page;
	    while((page = rowEvent.getSourcePages().shift())!=null) {
	    	
	    	Image pageImage = new Image("//graph.facebook.com/" + page.getPageId() + "/picture?type=square");
	    	pageImage.setHeight("25px");
	    	pageImage.setWidth("25px");
	    	
	    	ToolTipPanel pageImageToolTip = new ToolTipPanel(page.getName(), pageImage);
	    	pageImageToolTip.getElement().getStyle().setMarginLeft(10, Unit.PX);
	    	pageImageToolTip.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
	    	
	    	Link pageLink = new Link(page.getPageUrl(), pageImageToolTip);
	    	pageLink.setTarget("_blank");
	    	
	    	pages.add(pageLink); 

	    }
		
	}

}
