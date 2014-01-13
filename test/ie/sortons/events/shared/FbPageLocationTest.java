package ie.sortons.events.shared;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.kfuntak.gwt.json.serialization.client.Serializer;

//{"street":"Box 33, UCD Student Centre, University College Dublin", "city":"Dublin", "state":"", "country":"Ireland", "zip":"Dublin 4", "latitude":"53.3758673496", "longitude":"-6.37404267403"}
public class FbPageLocationTest extends GWTTestCase {

	public String getModuleName() {
		return "ie.sortons.events.Sortonsevents";
	}

	public void testDeserialisation() {

		String json = "{\"street\":\"Box 33, UCD Student Centre, University College Dublin\", \"city\":\"Dublin\", \"state\":\"\", \"country\":\"Ireland\", \"zip\":\"Dublin 4\", \"latitude\":\"53.3758673496\", \"longitude\":\"-6.37404267403\"}";

		Serializer serializer = (Serializer) GWT.create(Serializer.class);
		FbPageLocation location = (FbPageLocation) serializer.deSerialize(json, "ie.sortons.events.shared.FbPageLocation");

		assertEquals("error", "Dublin", location.getCity());

	}

}
