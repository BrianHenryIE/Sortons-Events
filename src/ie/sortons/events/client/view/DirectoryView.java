package ie.sortons.events.client.view;

import ie.brianhenry.gwtbingmaps.client.BingMap;
import ie.brianhenry.gwtbingmaps.client.api.Infobox;
import ie.brianhenry.gwtbingmaps.client.api.InfoboxOptions;
import ie.brianhenry.gwtbingmaps.client.api.Location;
import ie.brianhenry.gwtbingmaps.client.api.MapOptions;
import ie.brianhenry.gwtbingmaps.client.api.Pushpin;
import ie.brianhenry.gwtbingmaps.client.api.PushpinOptions;
import ie.brianhenry.gwtbingmaps.client.api.ViewOptions;
import ie.sortons.events.client.presenter.DirectoryPresenter;
import ie.sortons.events.client.view.widgets.MapPageWidget;
import ie.sortons.events.client.view.widgets.PageWidget;
import ie.sortons.gwtfbplus.client.api.Canvas;
import ie.sortons.gwtfbplus.client.resources.GwtFbPlusResources;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlPage;

import java.util.List;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

public class DirectoryView extends Composite implements DirectoryPresenter.Display {

	private FlowPanel panel = new FlowPanel();

	private FlowPanel pagesList = new FlowPanel();

	private BingMap map;

	private FlowPanel list = new FlowPanel();

	private FlowPanel left = new FlowPanel();
	private FlowPanel right = new FlowPanel();

	private final String credentials = "ApmYYZr2urnVJhMJMOaMgjhH7lAISlyMpSEkIs6cqxYMwg85epCC6c1ZXgWIWFao";

	public DirectoryView() {

		boolean enableSearchLogo = false;
		boolean showDashboard = true;
		boolean showMapTypeSelector = false;
		boolean showScalebar = false;
		boolean useInertia = false;

		MapOptions mapOptions = MapOptions.getMapOptions(credentials, null, null, null, null, null, null, null, null, null, null, null,
				enableSearchLogo, null, null, null, null, showDashboard, showMapTypeSelector, showScalebar, null, useInertia);

		// TODO
		// Figure out what zoomed out is for people who don't have their current location set
		Location center = Location.newLocation(53.3433567, -6.2441148);
		String mapTypeId = "fb";
		int zoom = 12;

		ViewOptions viewOptions = ViewOptions.newViewOptions(null, center, null, null, mapTypeId, null, zoom);

		// Set up the blank map
		map = new BingMap("eventsMap", mapOptions, viewOptions);

		map.setSize("810px", "400px");
		map.getElement().getStyle().setPosition(Position.RELATIVE);
		map.getElement().getStyle().setMarginBottom(10, Unit.PX);

		panel.add(map);

		panel.add(pagesList);

		right.getElement().getStyle().setWidth(400, Unit.PX);
		left.getElement().getStyle().setWidth(400, Unit.PX);

		left.getElement().getStyle().setLeft(0, Unit.PX);
		right.getElement().getStyle().setRight(0, Unit.PX);

		left.getElement().getStyle().setProperty("float", "left");
		right.getElement().getStyle().setProperty("float", "left");

		list.getElement().getStyle().setWidth(810, Unit.PX);
		list.getElement().getStyle().setPosition(Position.RELATIVE);
		list.getElement().getStyle().setOverflow(Overflow.HIDDEN);
		list.add(left);
		list.add(right);

		panel.add(list);

		initWidget(panel);

	}

	@Override
	public void setPages(List<FqlPage> pages) {

		int count = 0;

		for (FqlPage page : pages) {

			if (page.getLocation() != null && page.getLocation().getLatitude() != null && page.getLocation().getLongitude() != null) {
				Location location = Location.newLocation(page.getLocation().getLatitude(), page.getLocation().getLongitude());

				MapPageWidget item = new MapPageWidget(page);
				InfoboxOptions infoboxOptions = InfoboxOptions.getInfoboxOptions(400, 100, null, true, 0, true, false, null, null, item.getElement()
						.getInnerHTML());
				Infobox itemInfobox = Infobox.getInfobox(location, infoboxOptions);

				PushpinOptions options = PushpinOptions.setPushPinOptions(25, 28, GwtFbPlusResources.INSTANCE.mapPushPin().getSafeUri().asString(),
						false, null, null, null);
				Pushpin pushpin = Pushpin.getPushpin(location, options);

				map.addPinWithClickInfobox(pushpin, itemInfobox);
			}

			if (count % 2 == 0)
				left.add(new PageWidget(page));
			else
				right.add(new PageWidget(page));

			count++;

		}

		Timer timer = new Timer() {
			public void run() {
				Canvas.setSize();
			}
		};
		timer.schedule(1000);
	}
}