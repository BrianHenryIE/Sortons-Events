package ie.sortons.events.client.view.widgets;

import ie.sortons.events.shared.DiscoveredEvent;
import ie.sortons.events.shared.SourcePage;
import ie.sortons.gwtfbplus.client.resources.GwtFbPlusResources;
import ie.sortons.gwtfbplus.client.widgets.Link;
import ie.sortons.gwtfbplus.client.widgets.popups.ToolTipPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class EventWidget extends Composite {

	private static EventWidgetUiBinder uiBinder = GWT
			.create(EventWidgetUiBinder.class);

	interface EventWidgetUiBinder extends UiBinder<Widget, EventWidget> {
	}


	@UiField Anchor eventLink;

	@UiField SimplePanel eventPicture;

	@UiField Label startTime;
	
	@UiField Label location;
	
	@UiField FlowPanel pages;

	
	// TODO: Editor framework
	public EventWidget(DiscoveredEvent rowEvent) {
	
		GWT.<GwtFbPlusResources>create(GwtFbPlusResources.class).css().ensureInjected();
		
		// FqlEvent fbEvent = rowEvent.getFbEvent();
		
		initWidget(uiBinder.createAndBindUi(this));
		
		eventLink.setText(rowEvent.getName());
		eventLink.setHref("//www.facebook.com/event.php?eid="  + rowEvent.getEventId());
		eventLink.setTarget("_blank");
		Image eventImage = new Image("//graph.facebook.com/" + rowEvent.getEventId() + "/picture?type=square");
		eventImage.getElement().getStyle().setHeight(50, Unit.PX);
		eventImage.getElement().getStyle().setWidth(50, Unit.PX);
		eventPicture.add(new Link("//www.facebook.com/event.php?eid="  + rowEvent.getEventId(), eventImage, "_blank"));
		
	    startTime.setText(rowEvent.isDateOnly() ? DateTimeFormat.getFormat("EEEE, dd MMMM, yyyy").format(rowEvent.getStartTime()) : DateTimeFormat.getFormat("EEEE, dd MMMM, yyyy, 'at' k:mm").format(rowEvent.getStartTime()) );
	    location.setText(rowEvent.getLocation());

	   
	    pages.clear();
	    
	    for(SourcePage page : rowEvent.getSourcePages() ){
	    	
	    	Image pageImage = new Image("//graph.facebook.com/" + page.getFbPageId() + "/picture?type=square");
	    	pageImage.setHeight("25px");
	    	pageImage.setWidth("25px");
	    	
	    	ToolTipPanel pageImageToolTip = new ToolTipPanel(page.getName(), pageImage);
	    	pageImageToolTip.getElement().getStyle().setMarginLeft(10, Unit.PX);
	    	pageImageToolTip.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
	    	
	    	Link pageLink = new Link(page.getPageUrl(), pageImageToolTip, "_blank");
	    	
	    	pages.add(pageLink); 

	    }
		
	}

}
