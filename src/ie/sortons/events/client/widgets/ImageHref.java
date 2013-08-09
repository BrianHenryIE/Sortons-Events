package ie.sortons.events.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class ImageHref extends Composite {

	@UiField Image theImage;
	
	private static ImageHrefUiBinder uiBinder = GWT
			.create(ImageHrefUiBinder.class);

	interface ImageHrefUiBinder extends UiBinder<Widget, ImageHref> {
	}

	private String link;
	
	public ImageHref(String id, String link) {
		initWidget(uiBinder.createAndBindUi(this));
		this.link = link;
		theImage.setUrl("//graph.facebook.com/" + id + "/picture?type=square");
		
	}


	@UiHandler("theImage")
	void onClick(ClickEvent e) {
		String url = link;
        Window.open(url, "_blank", "");
	}


}
