package ie.sortons.events.shared;

import ie.sortons.gwtfbplus.client.widgets.suggestbox.FbSearchable;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlPage;

import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

public class DsFqlPage extends FqlPage implements FbSearchable, JsonSerializable {

	@Override
	public String getTitle() {
		return name;
	}

	@Override
	public String getSubTitle() {
		return (getLocation().getCity() != null ? getLocation().getCity() : "");
	}

	@Override
	public Long getUid() {
		return page_id;
	}

	@Override
	public String getSearchText() {
		return name + " " + location.getCity() + " " + location.getCountry() + " " + location.getName() + " " + location.getState() + " " + location.getStreet();
	}
}
