package ie.sortons.events.client.view.widgets;

import ie.sortons.gwtfbplus.shared.domain.fql.FqlPage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class MapPageWidget extends Composite {

	private static MapEventWidgetUiBinder uiBinder = GWT.create(MapEventWidgetUiBinder.class);

	interface MapEventWidgetUiBinder extends UiBinder<Widget, MapPageWidget> {
	}

	public MapPageWidget() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	Anchor title;

	@UiField
	Image picture;

	@UiField
	Label about;

	public MapPageWidget(FqlPage page) {
		initWidget(uiBinder.createAndBindUi(this));

		picture.setUrl("//graph.facebook.com/" + page.getPageId() + "/picture?type=square");
		
		about.setText(page.getAbout());
		
		title.setText(page.getName());
		title.setHref("//www.facebook.com/" + page.getPageId());
		title.setTarget("_blank");

	}

}
