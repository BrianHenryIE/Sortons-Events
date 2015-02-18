package ie.sortons.events.client.view.widgets;

import ie.sortons.gwtfbplus.client.resources.GwtFbPlusResources;
import ie.sortons.gwtfbplus.client.widgets.Link;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlEvent;

import com.google.gwt.core.client.GWT;
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

public class MyEventWidget extends Composite {

	private static EventWidgetUiBinder uiBinder = GWT
			.create(EventWidgetUiBinder.class);

	interface EventWidgetUiBinder extends UiBinder<Widget, MyEventWidget> {
	}


	@UiField Anchor eventLink;

	@UiField SimplePanel eventPicture;

	@UiField Label startTime;
	
	@UiField Label location;
	
	@UiField FlowPanel pages;

	
	// TODO: Editor framework
	public MyEventWidget(FqlEvent fbEvent) {
	
		GWT.<GwtFbPlusResources>create(GwtFbPlusResources.class).css().ensureInjected();
		
		initWidget(uiBinder.createAndBindUi(this));
		
		eventLink.setText(fbEvent.getName());
		eventLink.setHref("//www.facebook.com/event.php?eid="  + fbEvent.getEid());
		eventLink.setTarget("_blank");
		Image eventImage = new Image("//graph.facebook.com/" + fbEvent.getEid() + "/picture?type=square");
		eventImage.getElement().getStyle().setHeight(50, Unit.PX);
		eventImage.getElement().getStyle().setWidth(50, Unit.PX);
		eventPicture.add(new Link("//www.facebook.com/event.php?eid="  + fbEvent.getEid(), eventImage, "_blank"));
		
	    startTime.setText(fbEvent.is_date_only ? DateTimeFormat.getFormat("EEEE, dd MMMM, yyyy").format(fbEvent.getStartTime()) : DateTimeFormat.getFormat("EEEE, dd MMMM, yyyy, 'at' k:mm").format(fbEvent.getStartTime()) );
	    location.setText(fbEvent.getLocation());

	   
	    pages.clear();
	

	}

}
