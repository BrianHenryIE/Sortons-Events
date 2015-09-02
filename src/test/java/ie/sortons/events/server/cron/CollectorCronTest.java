package ie.sortons.events.server.cron;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import ie.sortons.events.server.cron.CollectorCron;
import ie.sortons.events.shared.ClientPageData;
import ie.sortons.events.shared.DiscoveredEvent;
import ie.sortons.events.shared.SourcePage;
import ie.sortons.gwtfbplus.shared.domain.FbResponse;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlStream;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlStream.FqlStreamItemAttachment;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlStream.FqlStreamItemAttachmentAdapter;

public class CollectorCronTest {

	// TODO In GwtFbPlus, test that a live stream call can be parsed

	// seems when the same event is found on two pages it's only recorded on one. (mergeevents)
	// e.g. ucdsocieties/freshers week

	private CollectorCron cc = new CollectorCron();

	private ClientPageData clientPageData;

	private List<FqlStream> fqlStream;

	private Gson gson = new GsonBuilder()
			.registerTypeAdapter(FqlStreamItemAttachment.class, new FqlStreamItemAttachmentAdapter()).create();

	private HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

	PrintWriter out;

	@Before
	public void setup() throws IOException, URISyntaxException {

		// TODO replace this with a json text file import of UCD Societies
		String cpdJson = "{    \"clientPageId\": \"176727859052209\",    \"clientPage\": {        \"pageId\": \"176727859052209\",        \"name\": \"Sortons Bistro\",        \"pageUrl\": \"http://www.facebook.com/pages/Sortons-Bistro/176727859052209\"    },    \"includedPages\": [        {            \"pageId\": \"176727859052209\",            \"name\": \"Sortons Bistro\",            \"pageUrl\": \"http://www.facebook.com/pages/Sortons-Bistro/176727859052209\"        },        {            \"pageId\": \"180585223095\",            \"name\": \"Wicklow Town\",            \"pageUrl\": \"http://www.facebook.com/wicklowtown\"        },        {            \"pageId\": \"144085445617928\",            \"name\": \"Hopkins Toymaster\",            \"pageUrl\": \"http://www.facebook.com/hopkinstoymaster1827\"        },        {            \"pageId\": \"195462027143869\",            \"name\": \"Wicklow Parish Summer Fete\",            \"pageUrl\": \"http://www.facebook.com/WicklowParishSummerFete\"        },        {            \"pageId\": \"153597848050326\",            \"name\": \"The Cats Pyjamas Cattery Wicklow\",            \"pageUrl\": \"http://www.facebook.com/pages/The-Cats-Pyjamas-Cattery-Wicklow/153597848050326\"        },        {            \"pageId\": \"327354357259\",            \"name\": \"Wicklow Veterinary Clinic\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Veterinary-Clinic/327354357259\"        },        {            \"pageId\": \"140345306086891\",            \"name\": \"WicklowTown.ie\",            \"pageUrl\": \"http://www.facebook.com/WicklowTown.ie\"        },        {            \"pageId\": \"142002899168824\",            \"name\": \"Heels- Ashford and Wicklow Town\",            \"pageUrl\": \"http://www.facebook.com/Heels.ie\"        },        {            \"pageId\": \"261291960566266\",            \"name\": \"Wicklow Arts Festival\",            \"pageUrl\": \"http://www.facebook.com/wicklow.artsfestival\"        },        {            \"pageId\": \"427072610713452\",            \"name\": \"Wicklow Tourism\",            \"pageUrl\": \"http://www.facebook.com/wicklow.tourism\"        },        {            \"pageId\": \"210398799111123\",            \"name\": \"Wicklow events\",            \"pageUrl\": \"http://www.facebook.com/wicklowevents\"        },        {            \"pageId\": \"509676619068377\",            \"name\": \"RockSkool.ie-Kilcoole Co.Wicklow\",            \"pageUrl\": \"http://www.facebook.com/RockSkoolMusic\"        },        {            \"pageId\": \"439340592846537\",            \"name\": \"Strictly Wicklow\",            \"pageUrl\": \"http://www.facebook.com/strictlywicklow\"        },        {            \"pageId\": \"196662593706553\",            \"name\": \"Wicklow Mykidstime\",            \"pageUrl\": \"http://www.facebook.com/Mykidstime.Wicklow\"        },        {            \"pageId\": \"206856689327295\",            \"name\": \"Wicklow Outdoors\",            \"pageUrl\": \"http://www.facebook.com/wicklowoutdoors\"        },        {            \"pageId\": \"133373533398528\",            \"name\": \"Ooooby-Wicklow\",            \"pageUrl\": \"http://www.facebook.com/pages/Ooooby-Wicklow/133373533398528\"        },        {            \"pageId\": \"152768388123618\",            \"name\": \"Wicklow Wine Co\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Wine-Co/152768388123618\"        },        {            \"pageId\": \"154783771243869\",            \"name\": \"Wicklow Gardens\",            \"pageUrl\": \"http://www.facebook.com/WicklowGardens\"        },        {            \"pageId\": \"135855496547\",            \"name\": \"Wicklow Jail\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Jail/135855496547\"        },        {            \"pageId\": \"139585832877561\",            \"name\": \"Foroige Wicklow Town Dudes & Divas\",            \"pageUrl\": \"http://www.facebook.com/pages/Foroige-Wicklow-Town-Dudes-Divas/139585832877561\"        },        {            \"pageId\": \"501725423220151\",            \"name\": \"Wicklow Photowalks & Workshops\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Photowalks-Workshops/501725423220151\"        },        {            \"pageId\": \"204563096335129\",            \"name\": \"Wicklow lost or found animals\",            \"pageUrl\": \"http://www.facebook.com/WicklowLostOrFoundAnimals\"        },        {            \"pageId\": \"148533678514648\",            \"name\": \"Wicklow County Enterprise Board\",            \"pageUrl\": \"http://www.facebook.com/wicklowenterpriseboard\"        },        {            \"pageId\": \"113134808716107\",            \"name\": \"Wicklow Hospice Foundation\",            \"pageUrl\": \"http://www.facebook.com/wicklowhospicefoundation\"        },        {            \"pageId\": \"106959406085130\",            \"name\": \"Wicklow Triathlon Club\",            \"pageUrl\": \"http://www.facebook.com/wicklowtriclub\"        },        {            \"pageId\": \"97908968195\",            \"name\": \"THE CAVE  ~Creative Arts Venture~,  WICKLOW TOWN\",            \"pageUrl\": \"http://www.facebook.com/TheCave.Wicklow\"        },        {            \"pageId\": \"107747952581273\",            \"name\": \"Wicklow GAA\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-GAA/107747952581273\"        },        {            \"pageId\": \"109205172443220\",            \"name\": \"West Wicklow Classic and Vintage Vehicle Club ( wwcvvc )\",            \"pageUrl\": \"http://www.facebook.com/WWCVVC\"        },        {            \"pageId\": \"207036582745784\",            \"name\": \"Wicklow Swimming Club\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Swimming-Club/207036582745784\"        },        {            \"pageId\": \"558775077501699\",            \"name\": \"EAI T/A Wicklow Way\",            \"pageUrl\": \"http://www.facebook.com/ecoactiveireland\"        },        {            \"pageId\": \"376218045727617\",            \"name\": \"St Patricks Day Parade Wicklow Town\",            \"pageUrl\": \"http://www.facebook.com/pages/St-Patricks-Day-Parade-Wicklow-Town/376218045727617\"        },        {            \"pageId\": \"417427591636316\",            \"name\": \"Wicklow kite festival\",            \"pageUrl\": \"http://www.facebook.com/WKFestival\"        },        {            \"pageId\": \"355118561181327\",            \"name\": \"Wicklow Dogs\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Dogs/355118561181327\"        },        {            \"pageId\": \"145408198834577\",            \"name\": \"wicklownews.net\",            \"pageUrl\": \"http://www.facebook.com/wicklownews\"        },        {            \"pageId\": \"184552674915335\",            \"name\": \"Wicklow County Arts Office\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-County-Arts-Office/184552674915335\"        },        {            \"pageId\": \"355501164464897\",            \"name\": \"Wicklow Weather\",            \"pageUrl\": \"http://www.facebook.com/wicklowweather\"        },        {            \"pageId\": \"132998870093195\",            \"name\": \"Wicklow Rugby Club minis section\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Rugby-Club-minis-section/132998870093195\"        },        {            \"pageId\": \"194857740543201\",            \"name\": \"Vocational School Carnew Co Wicklow.\",            \"pageUrl\": \"http://www.facebook.com/pages/Vocational-School-Carnew-Co-Wicklow/194857740543201\"        },        {            \"pageId\": \"124931677522919\",            \"name\": \"Have Your Cake And Eat It Wicklow Town\",            \"pageUrl\": \"http://www.facebook.com/pages/Have-Your-Cake-And-Eat-It-Wicklow-Town/124931677522919\"        },        {            \"pageId\": \"429604960418138\",            \"name\": \"Wicklow Junior Chess\",            \"pageUrl\": \"http://www.facebook.com/WicklowJuniorChess\"        },        {            \"pageId\": \"184520938250216\",            \"name\": \"WOW Whats On Wicklow\",            \"pageUrl\": \"http://www.facebook.com/whatsonWOW\"        },        {            \"pageId\": \"183791358269\",            \"name\": \"Wicklow Tidy Towns\",            \"pageUrl\": \"http://www.facebook.com/WicklowTidyTowns\"        },        {            \"pageId\": \"303022625484\",            \"name\": \"Wicklow RNLI Fundraising Branch\",            \"pageUrl\": \"http://www.facebook.com/WicklowRNLIFundraising\"        },        {            \"pageId\": \"231946786847785\",            \"name\": \"Wicklow Chamber\",            \"pageUrl\": \"http://www.facebook.com/wicklowchamber\"        },        {            \"pageId\": \"197777173652140\",            \"name\": \"Wicklow & District Credit Union Ltd\",            \"pageUrl\": \"http://www.facebook.com/wicklowcu\"        },        {            \"pageId\": \"160142460806715\",            \"name\": \"Wicklow Community Circus\",            \"pageUrl\": \"http://www.facebook.com/WicklowCommunityCircus\"        },        {            \"pageId\": \"106562746033733\",            \"name\": \"WAR: Wicklow Adventure Race\",            \"pageUrl\": \"http://www.facebook.com/warwicklowadventurerace\"        },        {            \"pageId\": \"421631631239284\",            \"name\": \"Wicklow Handmade Pens\",            \"pageUrl\": \"http://www.facebook.com/wicklowhandmadepens\"        },        {            \"pageId\": \"151512758312767\",            \"name\": \"Wicklow Dyspraxia/DCD/Dyslexia Information Page\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-DyspraxiaDCDDyslexia-Information-Page/151512758312767\"        },        {            \"pageId\": \"159746484091814\",            \"name\": \"Wicklow Gaol\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Gaol/159746484091814\"        },        {            \"pageId\": \"36872057519\",            \"name\": \"Mermaid County Wicklow Arts Centre\",            \"pageUrl\": \"http://www.facebook.com/MermaidCountyWicklowArtsCentre\"        },        {            \"pageId\": \"277129009088670\",            \"name\": \"Wicklow Community Notices\",            \"pageUrl\": \"http://www.facebook.com/wicklowcommunitynotices\"        },        {            \"pageId\": \"279747125502999\",            \"name\": \"Wicklow Snapchat\",            \"pageUrl\": \"http://www.facebook.com/WicklowSnapchat\"        },        {            \"pageId\": \"128437187214323\",            \"name\": \"Dominican College Wicklow Fundraising For the School of St Jude Tanzania\",            \"pageUrl\": \"http://www.facebook.com/pages/Dominican-College-Wicklow-Fundraising-For-the-School-of-St-Jude-Tanzania/128437187214323\"        },        {            \"pageId\": \"103394793051494\",            \"name\": \"Pet depot wicklow\",            \"pageUrl\": \"http://www.facebook.com/PetDepotWicklow\"        },        {            \"pageId\": \"144841699045624\",            \"name\": \"Wicklow Food and Garden Festival\",            \"pageUrl\": \"http://www.facebook.com/WicklowFoodAndGardenFestival\"        },        {            \"pageId\": \"200868766643632\",            \"name\": \"Wicklow Mental Health Association\",            \"pageUrl\": \"http://www.facebook.com/wicklowmentalhealth\"        },        {            \"pageId\": \"538318012896657\",            \"name\": \"Singing Lessons in Wicklow\",            \"pageUrl\": \"http://www.facebook.com/singinglessonsinwicklow\"        },        {            \"pageId\": \"213154872182320\",            \"name\": \"Wicklow Boxing Club\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Boxing-Club/213154872182320\"        },        {            \"pageId\": \"160504700636255\",            \"name\": \"Wicklow RNLI Lifeboat Station\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-RNLI-Lifeboat-Station/160504700636255\"        },        {            \"pageId\": \"103107273062739\",            \"name\": \"Glenealy, County Wicklow\",            \"pageUrl\": \"http://www.facebook.com/pages/Glenealy-County-Wicklow/103107273062739\"        },        {            \"pageId\": \"134297223363802\",            \"name\": \"The Gathering Wicklow 2013\",            \"pageUrl\": \"http://www.facebook.com/CountyWicklowGathering\"        },        {            \"pageId\": \"156016647746446\",            \"name\": \"Wicklow Town A.F.C\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Town-AFC/156016647746446\"        },        {            \"pageId\": \"157657640950008\",            \"name\": \"Wicklow County Council Library Service\",            \"pageUrl\": \"http://www.facebook.com/WicklowLibraries\"        },        {            \"pageId\": \"259696117507828\",            \"name\": \"STOMP Marketing & PR in Co Wicklow\",            \"pageUrl\": \"http://www.facebook.com/stompmarketingPR\"        },        {            \"pageId\": \"40069605687\",            \"name\": \"Bray  // C.o Wicklow Ireland\",            \"pageUrl\": \"http://www.facebook.com/pages/Bray-Co-Wicklow-Ireland/40069605687\"        },        {            \"pageId\": \"115976565146677\",            \"name\": \"Wicklow Macra\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Macra/115976565146677\"        },        {            \"pageId\": \"102180916490258\",            \"name\": \"Wicklow, Ireland\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Ireland/102180916490258\"        },        {            \"pageId\": \"120498064773999\",            \"name\": \"Wicklow Camera Club\",            \"pageUrl\": \"http://www.facebook.com/WicklowCameraClub\"        },        {            \"pageId\": \"167628113269431\",            \"name\": \"Wicklow Special Offers\",            \"pageUrl\": \"http://www.facebook.com/WicklowOffers\"        },        {            \"pageId\": \"145934835436569\",            \"name\": \"Wardrobe, Wicklow\",            \"pageUrl\": \"http://www.facebook.com/WardrobeWicklow\"        },        {            \"pageId\": \"1408504836030140\",            \"name\": \"Watching wicklow\",            \"pageUrl\": \"http://www.facebook.com/watchingwicklow\"        },        {            \"pageId\": \"116895565049332\",            \"name\": \"Phil Healy's Pub, Wicklow Town\",            \"pageUrl\": \"http://www.facebook.com/philhealys\"        },        {            \"pageId\": \"148468435223420\",            \"name\": \"WicklowTrad.com\",            \"pageUrl\": \"http://www.facebook.com/WicklowTrad\"        },        {            \"pageId\": \"261177883916297\",            \"name\": \"Bands For Boats-Wicklow RNLI fundraiser\",            \"pageUrl\": \"http://www.facebook.com/pages/Bands-For-Boats-Wicklow-RNLI-fundraiser/261177883916297\"        },        {            \"pageId\": \"371773307562\",            \"name\": \"Wicklow Town Hostel\",            \"pageUrl\": \"http://www.facebook.com/HostelWicklow\"        },        {            \"pageId\": \"324455570981308\",            \"name\": \"Startup Wicklow\",            \"pageUrl\": \"http://www.facebook.com/StartupWicklow\"        },        {            \"pageId\": \"116541711693851\",            \"name\": \"Wicklow Round Ireland Sailfest\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Round-Ireland-Sailfest/116541711693851\"        },        {            \"pageId\": \"12516136484\",            \"name\": \"Bridge Street Books Wicklow\",            \"pageUrl\": \"http://www.facebook.com/pages/Bridge-Street-Books-Wicklow/12516136484\"        },        {            \"pageId\": \"251238991630829\",            \"name\": \"Wicklow Enterprise Park\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Enterprise-Park/251238991630829\"        },        {            \"pageId\": \"10150091628215591\",            \"name\": \"Woodenbridge Hotel & Lodge, Wicklow\",            \"pageUrl\": \"http://www.facebook.com/WoodenbridgeHotelandLodge\"        },        {            \"pageId\": \"87804145777\",            \"name\": \"Summerhill House Hotel, Wicklow\",            \"pageUrl\": \"http://www.facebook.com/summerhillhousehotel\"        },        {            \"pageId\": \"182705418563422\",            \"name\": \"The Gardens Of Wicklow\",            \"pageUrl\": \"http://www.facebook.com/pages/The-Gardens-Of-Wicklow/182705418563422\"        },        {            \"pageId\": \"109284509099289\",            \"name\": \"Blainroe, Wicklow, Ireland\",            \"pageUrl\": \"http://www.facebook.com/pages/Blainroe-Wicklow-Ireland/109284509099289\"        },        {            \"pageId\": \"122007291762\",            \"name\": \"Wicklow Regatta Festival\",            \"pageUrl\": \"http://www.facebook.com/WicklowRegattaFestival\"        },        {            \"pageId\": \"108117582556198\",            \"name\": \"Wicklow Kabs - 66888\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Kabs-66888/108117582556198\"        },        {            \"pageId\": \"263806556977235\",            \"name\": \"Ballyknocken House Cookery School, Co. Wicklow.\",            \"pageUrl\": \"http://www.facebook.com/pages/Ballyknocken-House-Cookery-School-Co-Wicklow/263806556977235\"        },        {            \"pageId\": \"110444572315706\",            \"name\": \"Hartstown, Wicklow, Ireland\",            \"pageUrl\": \"http://www.facebook.com/pages/Hartstown-Wicklow-Ireland/110444572315706\"        },        {            \"pageId\": \"123311214379715\",            \"name\": \"County Wicklow, Ireland\",            \"pageUrl\": \"http://www.facebook.com/pages/County-Wicklow-Ireland/123311214379715\"        },        {            \"pageId\": \"204587016226812\",            \"name\": \"Wicklow Golf Club\",            \"pageUrl\": \"http://www.facebook.com/wicklowgolfclub\"        },        {            \"pageId\": \"111620038858137\",            \"name\": \"Ballygannon, Wicklow, Ireland\",            \"pageUrl\": \"http://www.facebook.com/pages/Ballygannon-Wicklow-Ireland/111620038858137\"        },        {            \"pageId\": \"113027872057537\",            \"name\": \"Wicklow Cancer Support\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Cancer-Support/113027872057537\"        },        {            \"pageId\": \"105509379484220\",            \"name\": \"Space Inside Wicklow Town\",            \"pageUrl\": \"http://www.facebook.com/pages/Space-Inside-Wicklow-Town/105509379484220\"        },        {            \"pageId\": \"545265462176942\",            \"name\": \"Wicklow Holistic Retreats\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Holistic-Retreats/545265462176942\"        },        {            \"pageId\": \"272934752731268\",            \"name\": \"Aughrim, Co. Wicklow Events Guide\",            \"pageUrl\": \"http://www.facebook.com/pages/Aughrim-Co-Wicklow-Events-Guide/272934752731268\"        },        {            \"pageId\": \"130017223732336\",            \"name\": \"Wicklow Montessori Primary School\",            \"pageUrl\": \"http://www.facebook.com/wicklowmontessoriprimaryschool\"        },        {            \"pageId\": \"142083712551384\",            \"name\": \"Wicklow Disability Group\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Disability-Group/142083712551384\"        },        {            \"pageId\": \"219122368147481\",            \"name\": \"Halloween in Wicklow\",            \"pageUrl\": \"http://www.facebook.com/HalloweeninWicklow\"        },        {            \"pageId\": \"359103824111918\",            \"name\": \"Wicklow Community Garden 2012\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Community-Garden-2012/359103824111918\"        },        {            \"pageId\": \"157386547666491\",            \"name\": \"Wicklow Toastmasters\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Toastmasters/157386547666491\"        },        {            \"pageId\": \"198477380164681\",            \"name\": \"Wicklow\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow/198477380164681\"        },        {            \"pageId\": \"178881055485251\",            \"name\": \"Stephen Donnelly for Wicklow\",            \"pageUrl\": \"http://www.facebook.com/DonnellyforWicklow\"        },        {            \"pageId\": \"354216523796\",            \"name\": \"Wicklow GAA Photos\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-GAA-Photos/354216523796\"        },        {            \"pageId\": \"160720457332233\",            \"name\": \"Wicklow GAA\",            \"pageUrl\": \"http://www.facebook.com/WicklowGAA\"        },        {            \"pageId\": \"343746670780\",            \"name\": \"The Wicklow Heather\",            \"pageUrl\": \"http://www.facebook.com/pages/The-Wicklow-Heather/343746670780\"        },        {            \"pageId\": \"125202347556447\",            \"name\": \"Wicklow Directory\",            \"pageUrl\": \"http://www.facebook.com/WicklowDirectory\"        },        {            \"pageId\": \"127113230709031\",            \"name\": \"Wicklow County Council Planning and Heritage Office\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-County-Council-Planning-and-Heritage-Office/127113230709031\"        },        {            \"pageId\": \"184831150589\",            \"name\": \"Dublin Wicklow MR Team\",            \"pageUrl\": \"http://www.facebook.com/DWMRT\"        },        {            \"pageId\": \"243487329063078\",            \"name\": \"Kildare/Wicklow Destination\",            \"pageUrl\": \"http://www.facebook.com/pages/KildareWicklow-Destination/243487329063078\"        },        {            \"pageId\": \"139031682850966\",            \"name\": \"Wicklow RFC\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-RFC/139031682850966\"        },        {            \"pageId\": \"152286738253651\",            \"name\": \"Operation Transformation Wicklow Town\",            \"pageUrl\": \"http://www.facebook.com/OperationTransformationWicklowTown\"        },        {            \"pageId\": \"279123598766426\",            \"name\": \"Wicklow Ladies Football\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Ladies-Football/279123598766426\"        },        {            \"pageId\": \"131811223556744\",            \"name\": \"Wicklow - SPCA\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-SPCA/131811223556744\"        },        {            \"pageId\": \"170785756317317\",            \"name\": \"Wicklow Animal Welfare\",            \"pageUrl\": \"http://www.facebook.com/WicklowAnimalWelfare\"        },        {            \"pageId\": \"166626530151932\",            \"name\": \"Madeinwicklow Writing\",            \"pageUrl\": \"http://www.facebook.com/Madeinwicklow\"        },        {            \"pageId\": \"210103215721449\",            \"name\": \"Foróige Wicklow\",            \"pageUrl\": \"http://www.facebook.com/pages/For%C3%B3ige-Wicklow/210103215721449\"        },        {            \"pageId\": \"106160539451370\",            \"name\": \"North Wicklow/South Dublin ET 2nd Level Action Group\",            \"pageUrl\": \"http://www.facebook.com/pages/North-WicklowSouth-Dublin-ET-2nd-Level-Action-Group/106160539451370\"        },        {            \"pageId\": \"139719246134603\",            \"name\": \"Wicklow Jazz at The Mezzanine Cafe\",            \"pageUrl\": \"http://www.facebook.com/WicklowJazz\"        },        {            \"pageId\": \"220256888023332\",            \"name\": \"Wicklow Business Network\",            \"pageUrl\": \"http://www.facebook.com/wicklowconet\"        },        {            \"pageId\": \"242188242499704\",            \"name\": \"Business in Wicklow\",            \"pageUrl\": \"http://www.facebook.com/pages/Business-in-Wicklow/242188242499704\"        },        {            \"pageId\": \"191593624272023\",            \"name\": \"County Wicklow (The Garden of Ireland)\",            \"pageUrl\": \"http://www.facebook.com/pages/County-Wicklow-The-Garden-of-Ireland/191593624272023\"        },        {            \"pageId\": \"370577509665821\",            \"name\": \"County Wicklow Partnership Festivals & Events Forum\",            \"pageUrl\": \"http://www.facebook.com/CountyWicklowPartnershipFestivalsEvents\"        },        {            \"pageId\": \"261433833868972\",            \"name\": \"Wicklow Mountains National Park\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Mountains-National-Park/261433833868972\"        },        {            \"pageId\": \"443615702328176\",            \"name\": \"Radio Nova, Wicklow - 95.7 FM & 100.3 FM\",            \"pageUrl\": \"http://www.facebook.com/radionovawicklow\"        },        {            \"pageId\": \"172141782813841\",            \"name\": \"Wicklow County Childcare\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-County-Childcare/172141782813841\"        },        {            \"pageId\": \"113825605347920\",            \"name\": \"Christmas Gift and Craft Fair Wicklow Town\",            \"pageUrl\": \"http://www.facebook.com/pages/Christmas-Gift-and-Craft-Fair-Wicklow-Town/113825605347920\"        },        {            \"pageId\": \"253113681408336\",            \"name\": \"Wicklow Male Voice Choir  Christmas Concert\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Male-Voice-Choir-Christmas-Concert/253113681408336\"        },        {            \"pageId\": \"392343744152126\",            \"name\": \"Wicklow Page\",            \"pageUrl\": \"http://www.facebook.com/wicklow.page\"        },        {            \"pageId\": \"329020330496865\",            \"name\": \"Wicklow Community Forum\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Community-Forum/329020330496865\"        },        {            \"pageId\": \"430352633728129\",            \"name\": \"Wicklow Hockey Club\",            \"pageUrl\": \"http://www.facebook.com/wicklow.hockeyclub\"        },        {            \"pageId\": \"178145885603837\",            \"name\": \"BirdWatch Ireland Wicklow Branch\",            \"pageUrl\": \"http://www.facebook.com/birdwatchwicklow\"        },        {            \"pageId\": \"324754317539958\",            \"name\": \"Wicklow Comhairle na nÓg\",            \"pageUrl\": \"http://www.facebook.com/WicklowComhairlenanOg\"        },        {            \"pageId\": \"633311706682180\",            \"name\": \"The Wicklow Rock Art Project\",            \"pageUrl\": \"http://www.facebook.com/WicklowRockArtProject\"        },        {            \"pageId\": \"479433408740578\",            \"name\": \"East Coast Surf School\",            \"pageUrl\": \"http://www.facebook.com/eastcoastsurfschool\"        },        {            \"pageId\": \"143751348969384\",            \"name\": \"Wicklow County Council\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-County-Council/143751348969384\"        },        {            \"pageId\": \"425211207584541\",            \"name\": \"Wicklow League Fixtures/Results\",            \"pageUrl\": \"http://www.facebook.com/wicklowleague\"        },        {            \"pageId\": \"104724622937571\",            \"name\": \"Wicklow Child & Family Project\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Child-Family-Project/104724622937571\"        },        {            \"pageId\": \"459197237512845\",            \"name\": \"Wicklow Skate Park & Outdoor Gym\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Skate-Park-Outdoor-Gym/459197237512845\"        },        {            \"pageId\": \"205639146187108\",            \"name\": \"The Wicklow Town Retailers Group\",            \"pageUrl\": \"http://www.facebook.com/pages/The-Wicklow-Town-Retailers-Group/205639146187108\"        },        {            \"pageId\": \"468419026566601\",            \"name\": \"Wicklow Nature\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Nature/468419026566601\"        },        {            \"pageId\": \"214891775203340\",            \"name\": \"Wicklow Town & District Chamber of Commerce\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Town-District-Chamber-of-Commerce/214891775203340\"        },        {            \"pageId\": \"203533826366131\",            \"name\": \"EmployAbility Service Wicklow\",            \"pageUrl\": \"http://www.facebook.com/pages/EmployAbility-Service-Wicklow/203533826366131\"        },        {            \"pageId\": \"242737693361\",            \"name\": \"Wicklow Bowl & KidZone\",            \"pageUrl\": \"http://www.facebook.com/Wicklowbowlkidzone\"        },        {            \"pageId\": \"606783826021299\",            \"name\": \"Wicklow Fitness by WLD Dance & Fitness\",            \"pageUrl\": \"http://www.facebook.com/WLDFitness\"        },        {            \"pageId\": \"301328169973421\",            \"name\": \"Wicklow Broadband\",            \"pageUrl\": \"http://www.facebook.com/WicklowBband\"        },        {            \"pageId\": \"147978328734171\",            \"name\": \"Wicklow Rose 2013\",            \"pageUrl\": \"http://www.facebook.com/wicklowrose2013\"        },        {            \"pageId\": \"137090586311867\",            \"name\": \"Wicklow (Dáil Éireann constituency)\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-D%C3%A1il-%C3%89ireann-constituency/137090586311867\"        },        {            \"pageId\": \"109254272433853\",            \"name\": \"Wicklow RFC\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-RFC/109254272433853\"        },        {            \"pageId\": \"322962794463458\",            \"name\": \"Paranormal Wicklow Gaol\",            \"pageUrl\": \"http://www.facebook.com/pages/Paranormal-Wicklow-Gaol/322962794463458\"        },        {            \"pageId\": \"400351246641892\",            \"name\": \"Wicklow Boat Charters\",            \"pageUrl\": \"http://www.facebook.com/WicklowBoatCharters\"        },        {            \"pageId\": \"357827910941996\",            \"name\": \"Music Generation Wicklow\",            \"pageUrl\": \"http://www.facebook.com/pages/Music-Generation-Wicklow/357827910941996\"        },        {            \"pageId\": \"130347813699609\",            \"name\": \"Wicklow's Historic Gaol\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklows-Historic-Gaol/130347813699609\"        },        {            \"pageId\": \"133245870071254\",            \"name\": \"The Wicklow Way\",            \"pageUrl\": \"http://www.facebook.com/walkingthewicklowway\"        },        {            \"pageId\": \"109485365744524\",            \"name\": \"Wicklow Mountains\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Mountains/109485365744524\"        },        {            \"pageId\": \"222875104515501\",            \"name\": \"Wicklow\",            \"pageUrl\": \"http://www.facebook.com/wicklowofficial\"        },        {            \"pageId\": \"210411599028045\",            \"name\": \"Christmas in Wicklow\",            \"pageUrl\": \"http://www.facebook.com/ChristmasinWicklow\"        },        {            \"pageId\": \"102582896483653\",            \"name\": \"Wicklow Town Council\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Town-Council/102582896483653\"        },        {            \"pageId\": \"320453224661617\",            \"name\": \"Wicklow Food Producers\",            \"pageUrl\": \"http://www.facebook.com/WicklowFood\"        },        {            \"pageId\": \"275776349151431\",            \"name\": \"Wicklow Town Ireland.\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Town-Ireland/275776349151431\"        },        {            \"pageId\": \"191784260848719\",            \"name\": \"Wicklow Sailing Club\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Sailing-Club/191784260848719\"        },        {            \"pageId\": \"160913113943786\",            \"name\": \"Wicklow Local Authorities\",            \"pageUrl\": \"http://www.facebook.com/WicklowCountyCouncil\"        },        {            \"pageId\": \"195461990496616\",            \"name\": \"Wicklow Dinghy Sailing\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-Dinghy-Sailing/195461990496616\"        },        {            \"pageId\": \"200204546665229\",            \"name\": \"Wicklow SPCA\",            \"pageUrl\": \"http://www.facebook.com/pages/Wicklow-SPCA/200204546665229\"        },        {            \"pageId\": \"77722956378\",            \"name\": \"Halpin's Bridge Cafe\",            \"pageUrl\": \"http://www.facebook.com/halpinsbridgecafe\"        }    ],    \"ignoredPages\": [        {            \"pageId\": \"130806596969418\",            \"name\": \"Keep Going Sure It's Grand\",            \"pageUrl\": \"http://www.facebook.com/keepgoingsureitsgrand\"        },        {            \"pageId\": \"107431775978628\",            \"name\": \"The Whipround\",            \"pageUrl\": \"http://www.facebook.com/pages/The-Whipround/107431775978628\"        },        {            \"pageId\": \"162908837053953\",            \"name\": \"Total Car Care, Glenealy\",            \"pageUrl\": \"http://www.facebook.com/pages/Total-Car-Care-Glenealy/162908837053953\"        },        {            \"pageId\": \"264435814478\",            \"name\": \"HilltopTreks\",            \"pageUrl\": \"http://www.facebook.com/pages/HilltopTreks/264435814478\"        },        {            \"pageId\": \"220018101365634\",            \"name\": \"MyStreet.ie\",            \"pageUrl\": \"http://www.facebook.com/MyStreet.ie\"        },        {            \"pageId\": \"103921891271\",            \"name\": \"Glenview Hotel Wicklow\",            \"pageUrl\": \"http://www.facebook.com/glenviewhotel\"        },        {            \"pageId\": \"165692700154574\",            \"name\": \"Conways Toymaster Sunderland\",            \"pageUrl\": \"http://www.facebook.com/pages/Conways-Toymaster-Sunderland/165692700154574\"        },        {            \"pageId\": \"100002069652985\",            \"name\": \"Wynnes Bar Wicklow\",            \"pageUrl\": \"http://www.facebook.com/wynnes.wicklow\"        }    ],    \"includedPageIds\": [        \"176727859052209\",        \"180585223095\",        \"144085445617928\",        \"195462027143869\",        \"153597848050326\",        \"327354357259\",        \"140345306086891\",        \"142002899168824\",        \"261291960566266\",        \"427072610713452\",        \"210398799111123\",        \"509676619068377\",        \"439340592846537\",        \"196662593706553\",        \"206856689327295\",        \"133373533398528\",        \"152768388123618\",        \"154783771243869\",        \"135855496547\",        \"139585832877561\",        \"501725423220151\",        \"204563096335129\",        \"148533678514648\",        \"113134808716107\",        \"106959406085130\",        \"97908968195\",        \"107747952581273\",        \"109205172443220\",        \"207036582745784\",        \"558775077501699\",        \"376218045727617\",        \"417427591636316\",        \"355118561181327\",        \"145408198834577\",        \"184552674915335\",        \"355501164464897\",        \"132998870093195\",        \"194857740543201\",        \"124931677522919\",        \"429604960418138\",        \"184520938250216\",        \"183791358269\",        \"303022625484\",        \"231946786847785\",        \"197777173652140\",        \"160142460806715\",        \"106562746033733\",        \"421631631239284\",        \"151512758312767\",        \"159746484091814\",        \"36872057519\",        \"277129009088670\",        \"279747125502999\",        \"128437187214323\",        \"103394793051494\",        \"144841699045624\",        \"200868766643632\",        \"538318012896657\",        \"213154872182320\",        \"160504700636255\",        \"103107273062739\",        \"134297223363802\",        \"156016647746446\",        \"157657640950008\",        \"259696117507828\",        \"40069605687\",        \"115976565146677\",        \"102180916490258\",        \"120498064773999\",        \"167628113269431\",        \"145934835436569\",        \"1408504836030140\",        \"116895565049332\",        \"148468435223420\",        \"261177883916297\",        \"371773307562\",        \"324455570981308\",        \"116541711693851\",        \"12516136484\",        \"251238991630829\",        \"10150091628215591\",        \"87804145777\",        \"182705418563422\",        \"109284509099289\",        \"122007291762\",        \"108117582556198\",        \"263806556977235\",        \"110444572315706\",        \"123311214379715\",        \"204587016226812\",        \"111620038858137\",        \"113027872057537\",        \"105509379484220\",        \"545265462176942\",        \"272934752731268\",        \"130017223732336\",        \"142083712551384\",        \"219122368147481\",        \"359103824111918\",        \"157386547666491\",        \"198477380164681\",        \"178881055485251\",        \"354216523796\",        \"160720457332233\",        \"343746670780\",        \"125202347556447\",        \"127113230709031\",        \"184831150589\",        \"243487329063078\",        \"139031682850966\",        \"152286738253651\",        \"279123598766426\",        \"131811223556744\",        \"170785756317317\",        \"166626530151932\",        \"210103215721449\",        \"106160539451370\",        \"139719246134603\",        \"220256888023332\",        \"242188242499704\",        \"191593624272023\",        \"370577509665821\",        \"261433833868972\",        \"443615702328176\",        \"172141782813841\",        \"113825605347920\",        \"253113681408336\",        \"392343744152126\",        \"329020330496865\",        \"430352633728129\",        \"178145885603837\",        \"324754317539958\",        \"633311706682180\",        \"479433408740578\",        \"143751348969384\",        \"425211207584541\",        \"104724622937571\",        \"459197237512845\",        \"205639146187108\",        \"468419026566601\",        \"214891775203340\",        \"203533826366131\",        \"242737693361\",        \"606783826021299\",        \"301328169973421\",        \"147978328734171\",        \"137090586311867\",        \"109254272433853\",        \"322962794463458\",        \"400351246641892\",        \"357827910941996\",        \"130347813699609\",        \"133245870071254\",        \"109485365744524\",        \"222875104515501\",        \"210411599028045\",        \"102582896483653\",        \"320453224661617\",        \"275776349151431\",        \"191784260848719\",        \"160913113943786\",        \"195461990496616\",        \"200204546665229\",        \"77722956378\"    ],    \"ignoredPageIds\": [        \"130806596969418\",        \"107431775978628\",        \"162908837053953\",        \"264435814478\",        \"220018101365634\",        \"103921891271\",        \"165692700154574\",        \"100002069652985\"    ]}";
		clientPageData = gson.fromJson(cpdJson, ClientPageData.class);

		// Example FQL Stream
		String resource = "/UCDSocietiesStreamWithEvents.json";

		assertNotNull("Test file missing: " + resource, getClass().getResource(resource));

		URL url = getClass().getResource(resource);
		Path resPath = Paths.get(url.toURI());

		String json = new String(Files.readAllBytes(resPath), "UTF8");

		Type fooType = new TypeToken<FbResponse<FqlStream>>() {
		}.getType();

		FbResponse<FqlStream> response = gson.fromJson(json, fooType);

		fqlStream = response.getData();

		// OLD:
		//

		//
		// PrintWriter writer = new PrintWriter("somefile.txt");
		// Mockito.when(response.getWriter()).thenReturn(writer);
		// out = response.getWriter();
		//
		// // when(response.getWriter()).thenReturn(writer);
		//

		// cc.setPrintWriter(response.getWriter());
	}

	/**
	 * The method should generated the FQL stream call when given a SourcePage object
	 * 
	 * Output such as the following is correct: SELECT source_id, post_id, permalink, actor_id, target_id, message,
	 * attachment.media, created_time, type FROM stream WHERE source_id = 12345 AND actor_id = 12345 AND created_time >
	 * 1435082343
	 * 
	 * The test uses regex to ignore the time
	 */
	@Test
	public void testGetFqlStreamCall() {

		SourcePage sourcePage = new SourcePage("UCD Societies", 208084049281702l,
				"https://www.facebook.com/UCDSocieties");

		String fqlCallRegexPattern = "SELECT%20source_id%2C%20post_id%2C%20permalink%2C%20actor_id%2C%20target_id%2C%20message%2C%20attachment\\.media%2C%20created_time%2C%20type%20FROM%20stream%20WHERE%20source_id%20%3D%20208084049281702%20AND%20actor_id%20=%20208084049281702%20AND%20created_time%20%3E%20\\d+";

		String methodResult = cc.getFqlStreamCall(sourcePage);

		assertTrue(methodResult.matches(fqlCallRegexPattern));
	}

	@Test
	public void testFindPostsByPageInStream() {

		List<FqlStream> pagePost = cc.findPostsByPageInStream(fqlStream);

		for (FqlStream post : pagePost) {
			assertTrue(post.getActorId().equals(208084049281702l));
		}

	}

	@Test
	public void testFindPostsByPageToSave() {

		// List<WallPost> wallPosts = cc.findPostsByPageToSave(fqlStream);

		// TODO this test doesn't work with past data... the method only looks at data from the past 15 minutes
		// There are ways to change the time!
	}

	@Test
	public void testFindEventsInStreamPosts() {

		List<Long> eventIds = cc.findEventIdsInStreamPosts(fqlStream);

		assertEquals(10, eventIds.size());
	}

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalURLFetchServiceTestConfig());

	@Before
	public void setUp() {
		helper.setUp();
	}

	@After
	public void tearDown() {
		helper.tearDown();
	}

	@Ignore
	@Test
	public void testDoGet() {

		// Get the list of source pages from the datastore
		List<SourcePage> sourcePages = new ArrayList<SourcePage>();

		SourcePage sourcePage1 = new SourcePage("UCD Musical Society", 326513587544275l,
				"https://www.facebook.com/UCDMUSICALSOC");
		sourcePage1.setClientId(197528567092983l); // UCDEvents

		SourcePage sourcePage2 = new SourcePage("UCD Musical Society", 326513587544275l,
				"https://www.facebook.com/UCDMUSICALSOC");
		sourcePage2.setClientId(428055040731753l); // FOMOUCD

		SourcePage sourcePage3 = new SourcePage("UCD Musical Society", 326513587544275l,
				"https://www.facebook.com/UCDMUSICALSOC");
		sourcePage3.setClientId(208084049281702l); // UCDSocieties

		// 1678665409024104 <-event

		List<DiscoveredEvent> eventsPosted = new ArrayList<DiscoveredEvent>();
		eventsPosted.add(new DiscoveredEvent(1678665409024104l, sourcePage1));
		eventsPosted.add(new DiscoveredEvent(1678665409024104l, sourcePage2));
		eventsPosted.add(new DiscoveredEvent(1678665409024104l, sourcePage3));

		System.out.println(eventsPosted.size() + " events posted.\n");

		// Now we have many lists of events including some duplicates and many
		// nulls. Merge them.

		List<DiscoveredEvent> mergedLists = new ArrayList<DiscoveredEvent>();
		mergedLists.addAll(eventsPosted);

		// mergedLists.removeAll(Collections.singleton(null));

		List<DiscoveredEvent> allEvents = cc.mergeEvents(mergedLists);

		System.out.println(allEvents.size() + " total events (duplicates merged).\n");

		// Some of the events don't have info, i.e. from the event_member table
		// Some will be in the past – the fql finding details will filter them
		// out

		List<DiscoveredEvent> eventsReady = cc.findEventDetails(allEvents);

		System.out.println(eventsReady.size() + " future events.\n");

	}

	@Test
	public void testDuplicateIssue() {

		Map<Long, String> names = new HashMap<Long, String>();
		names.put(197528567092983l, "UCD Events");
		names.put(428055040731753l, "FOMO UCD");
		names.put(208084049281702l, "UCD Societies");

		List<SourcePage> sourcePages = new ArrayList<SourcePage>();

		SourcePage sourcePage1 = new SourcePage("UCD Societies", 208084049281702l,
				"https://www.facebook.com/UCDSocieties");
		sourcePage1.setClientId(197528567092983l); // UCDEvents

		SourcePage sourcePage2 = new SourcePage("UCD Societies", 208084049281702l,
				"https://www.facebook.com/UCDSocieties");
		sourcePage2.setClientId(428055040731753l); // FOMOUCD

		SourcePage sourcePage3 = new SourcePage("UCD Societies", 208084049281702l,
				"https://www.facebook.com/UCDSocieties");
		sourcePage3.setClientId(208084049281702l); // UCDSocieties

		sourcePages.add(sourcePage1);
		sourcePages.add(sourcePage2);
		sourcePages.add(sourcePage3);

		System.out.println("sourcePages.size() : " + sourcePages.size() +"\n");

		// Get the calls for reading their walls
		Map<SourcePage, String> fqlCalls = new HashMap<SourcePage, String>();
		
		for (SourcePage sourcePage : sourcePages){
			System.out.println(names.get(sourcePage.getClientId()));
			fqlCalls.put(sourcePage, cc.getFqlStreamCall(sourcePage));
		}
		
		System.out.println("fqlCalls.size : " + fqlCalls.size());
		
		Map<SourcePage, String> jsonWalls = cc.asyncFqlCall(fqlCalls);

		System.out.println("jsonWalls.size : " + jsonWalls.size());
		
		Map<SourcePage, List<FqlStream>> parsedWalls = cc.parseJsonWalls(jsonWalls);

		System.out.println("parsedWalls.size : " + parsedWalls.size());
		
		for (List<FqlStream> stream : parsedWalls.values()) {
			List<Long> a = cc.findEventIdsInStreamPosts(stream);
			System.out.println("event ids found: " + a.size());
		}

		List<DiscoveredEvent> eventsPosted = cc.findEventsInStreams(parsedWalls);

		System.out.println(eventsPosted.size() + " events posted.\n");
		for (DiscoveredEvent de : eventsPosted) {
			System.out.println(de.getEventId() + " for " + names.get(de.getClientId()));
		}

		List<DiscoveredEvent> eventsCreated = cc.findCreatedEventsByPages(sourcePages);

		System.out.println(eventsCreated.size() + " events created.\n");
		for (DiscoveredEvent de : eventsCreated) {
			System.out.println(de.getEventId() + " for " + names.get(de.getClientId()));
		}

		// Now we have many lists of events including some duplicates and many
		// nulls. Merge them.

		List<DiscoveredEvent> mergedLists = new ArrayList<DiscoveredEvent>();
		mergedLists.addAll(eventsPosted);
		mergedLists.addAll(eventsCreated);
		// mergedLists.removeAll(Collections.singleton(null));

		List<DiscoveredEvent> allEvents = cc.mergeEvents(mergedLists);

		System.out.println(allEvents.size() + " total events (duplicates merged).\n");

		// Some of the events don't have info, i.e. from the event_member table
		// Some will be in the past – the fql finding details will filter them
		// out

		List<DiscoveredEvent> eventsReady = cc.findEventDetails(allEvents);

		System.out.println(eventsReady.size() + " future events.\n");

	}
}