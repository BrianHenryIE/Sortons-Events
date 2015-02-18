package ie.sortons.events.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface Resources extends ClientBundle {

	public static final Resources INSTANCE = GWT.create(Resources.class);

	@Source("sortonsevents.css")
	Style css();
	

	public interface DefaultStyle extends CssResource {
		
	}

	public interface Style extends CssResource {

		String adminPanelButton();
		String adminGlass();
	}

}