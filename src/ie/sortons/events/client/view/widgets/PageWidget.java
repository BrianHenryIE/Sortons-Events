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

public class PageWidget extends Composite {

	private static PageWidgetUiBinder uiBinder = GWT.create(PageWidgetUiBinder.class);

	interface PageWidgetUiBinder extends UiBinder<Widget, PageWidget> {
	}

	public PageWidget() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	Anchor title;

	@UiField
	Image picture;

	@UiField
	Label about;

	@UiField
	Label contact;

	public PageWidget(FqlPage page) {
		initWidget(uiBinder.createAndBindUi(this));

		picture.setUrl("//graph.facebook.com/" + page.getPageId() + "/picture?type=square");

		about.setText(page.getAbout());

		if (page.getLocation() != null) {
			String contactString = "";

			String address = page.getLocation().getFriendlyString();
			
			address = address.replace(", Ireland", "");
			// this deletes the , Dublin from , Dublin 2
			// address = address.replace(", Dublin", "");
			
			
			if (address != null)
				contactString += address;

			if (address != null && !address.equals("") && page.getPhone() != null)
				contactString += " \u00b7 ";

			if (page.getPhone() != null)
				contactString += page.getPhone();

			contact.setText(contactString);
		}
		
		title.setText(page.getName());
		title.setHref("//www.facebook.com/" + page.getPageId());
		title.setTarget("_blank");

	}

}
