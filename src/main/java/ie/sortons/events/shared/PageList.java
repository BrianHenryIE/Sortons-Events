package ie.sortons.events.shared;

import java.util.Arrays;
import java.util.List;

import com.kfuntak.gwt.json.serialization.client.JsonSerializable;

public class PageList implements JsonSerializable {

	public List<String> list;

	public List<String> getList() {
		return list;
	}

	public PageList() {
	}

	public PageList(String newList) {
		this.list = Arrays.asList(newList.split(","));
	}
}
