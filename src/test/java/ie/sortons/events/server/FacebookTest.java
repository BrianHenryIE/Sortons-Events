package ie.sortons.events.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import ie.sortons.events.shared.Config;
import ie.sortons.gwtfbplus.shared.domain.graph.GraphEvent;
import ie.sortons.gwtfbplus.shared.domain.graph.GraphPage;
import ie.sortons.gwtfbplus.shared.domain.graph.GraphPostsResponse;

public class FacebookTest {

	/**
	 * For GAE testing
	 * 
	 * @see http://svenbuschbeck.net/wordpress/2012/05/junit-testing-and-gae-apis/
	 * @see https://developers.google.com/appengine/docs/java/tools/localunittesting#Setting_Up_a_Testing_Framework
	 */
	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalURLFetchServiceTestConfig());

	@Before
	public void setUp() {
		helper.setUp();
	}

	@After
	public void tearDown() {
		helper.tearDown();
	}

	String serverAccessToken = Config.getAppAccessTokenServer();

	String apiVersion = Config.getFbApiVersion();

	Facebook facebook = new Facebook(serverAccessToken, apiVersion);

	@Test
	public void testGettingSinglePage() {
		// 131737309490?fields=about,phone,name,id,link,location

		GraphPage page = facebook.getSinglePage("131737309490");

		assertEquals("https://www.facebook.com/trinityents/", page.getLink());

	}

	@Test
	public void testParsingMultipleEvents() {

		// ?ids=1136803873075556,144334812684489,1675367409446454,1779594608985039,1786712664905855,295586397479604,341678926173412,502015753331134

		String[] eventIdsArray = { "1136803873075556", "144334812684489", "1675367409446454", "1779594608985039",
				"1786712664905855", "295586397479604", "341678926173412", "502015753331134" };

		List<String> eventIdsList = Arrays.asList(eventIdsArray);

		Set<String> eventIdsSet = new HashSet<String>(eventIdsList);

		Map<String, GraphEvent> graphEvents = facebook.getGraphEventsFromEventIds(eventIdsSet);

		assertEquals(eventIdsList.size(), graphEvents.size());

		for (GraphEvent graphEvent : graphEvents.values())
			assertTrue(eventIdsList.contains(graphEvent.getId()));

	}

	@Test
	public void testFindEventsInFeed() throws URISyntaxException, UnsupportedEncodingException, IOException {

		// 131737309490/?fields=posts{message,link}
		String resource = "/GraphPostsWithEvents.json";
		assertNotNull("Test file missing: " + resource, getClass().getResource(resource));
		URL url = getClass().getResource(resource);
		Path resPath = Paths.get(url.toURI());
		String json = new String(Files.readAllBytes(resPath), "UTF8");

		Gson gson = new Gson();

		GraphPostsResponse gfr = gson.fromJson(json, GraphPostsResponse.class);

		Set<String> eventsFromFeed = facebook.findEventsInFeed(gfr.getPosts().getData());

		assertEquals(eventsFromFeed.size(), 8);

	}

	@Test
	public void testBuildGraphQueryStringForFeedAndEventsForMultiplePages() {

		String[] idsArray = { "131737309490", "139957459378369" };
		List<String> ids = Arrays.asList(idsArray);

		String correctCall = "?ids=131737309490,139957459378369&fields=posts%7Bmessage%2Clink%7D%2Cevents%7Bid%7D";

		String buildCall = facebook.buildGraphQueryStringForFeedAndEventsForMultiplePages(ids);

		assertEquals(correctCall, buildCall);

	}

	@Test
	public void testBatchAsyncUrlFetch() {

		String[] callsArray = { "me?fields=id,name", "DublinTheatre?fields=id,name" };

		List<String> callsList = Arrays.asList(callsArray);

		List<String> responses = facebook.batchAsyncUrlFetch(callsList);

		assertTrue(responses.size() == 2);

		String response2 = "{\"id\":\"632419800128560\",\"name\":\"Dublin Theatre\"}";

		assertEquals(responses.get(1), response2);
	}

	@Test
	public void testSomethingElse() {

		// UCD Societies list 12-09-2016
		String[] pageIds = { "100677603374187", "1025698610794836", "1032655460107491", "110396262321892",
				"115318095147197", "118467031573937", "126104739882", "126140080035", "131131310360391",
				"132009863517651", "133490393390530", "136338279838233", "137692179658572", "1397955010432953",
				"141777375891020", "150292068388184", "1537310269852943", "1549871928631896", "155350824485272",
				"156373707999", "1634309413469796", "1666658503570249", "168947703486139", "173887419321014",
				"175454489163498", "196251647059472", "196489580433396", "202260986482121", "207201872643691",
				"208084049281702", "209813799178687", "211370445611484", "212629178895896", "219588608236172",
				"230980453691830", "239881959387896", "258128561002245", "261859710608040", "268895813228504",
				"273389792679439", "278853952124958", "286540920602", "313567945399874", "316221368472305",
				"326513587544275", "332118046848072", "354215754630222", "357240167744640", "361238870620767",
				"386475224781565", "386694501375759", "447777375242973", "473152689386103", "474020326027660",
				"478278195616875", "488131577956080", "490691141097994", "520798321345668", "531648263677370",
				"537411052940345", "545537605500109", "563988313729429", "586221424798605", "608689605841217",
				"643916408972084", "757229274336264", "779022648865786", "805314142827990", "817422025038306",
				"851358724898334", "852838778137318", "944012582285011" };

		List<String> pageIdsList = Arrays.asList(pageIds);

		List<List<String>> partitionedSourcePageIds = Lists.partition(pageIdsList, 2);

		List<String> graphQueries = new ArrayList<String>();
		for (List<String> idPartition : partitionedSourcePageIds) {
			String partitionQuery = facebook.buildGraphQueryStringForFeedAndEventsForMultiplePages(idPartition);
			graphQueries.add(partitionQuery);
		}

	}

}
