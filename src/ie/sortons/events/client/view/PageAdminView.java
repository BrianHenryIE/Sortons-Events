package ie.sortons.events.client.view;

import ie.sortons.events.client.presenter.PageAdminPresenter;
import ie.sortons.events.client.view.widgets.AdminPageItem;
import ie.sortons.gwtfbplus.client.resources.GwtFbPlusResources;
import ie.sortons.gwtfbplus.client.widgets.popups.SelectedLoading;
import ie.sortons.gwtfbplus.client.widgets.suggestbox.FbSearchable;
import ie.sortons.gwtfbplus.client.widgets.suggestbox.FbSingleSuggestbox;
import ie.sortons.gwtfbplus.shared.domain.fql.FqlPage;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class PageAdminView extends Composite implements PageAdminPresenter.Display {

	private static AdminViewUiBinder uiBinder = GWT.create(AdminViewUiBinder.class);

	interface AdminViewUiBinder extends UiBinder<Widget, PageAdminView> {
	}

	PageAdminPresenter presenter;
	GwtFbPlusResources resources = GwtFbPlusResources.INSTANCE;

	public void setPresenter(PageAdminPresenter presenter) {
		this.presenter = presenter;
	}

	SelectedLoading selectedItem = new SelectedLoading();
	List<FbSearchable> pages = new ArrayList<FbSearchable>();
	FbSingleSuggestbox suggestBox = new FbSingleSuggestbox(pages, "Enter a Facebook Page URL, Page ID or search suggestions", selectedItem);

	public PageAdminView() {
		initWidget(uiBinder.createAndBindUi(this));
		resources.facebookStyles().ensureInjected();
		suggestBox.setWidth("100%");
		suggestBox.getElement().getStyle().setMarginBottom(10, Unit.PX);
		addPageInput.add(suggestBox);
	}

	@UiField
	SimplePanel addPageInput;

	@UiField
	Label includedCount;

	@UiField
	FlowPanel includedPagesPanel;

	@UiField
	Label closeButton;
	
	public Label getCloseButton(){
		return closeButton;
	}
	
	@Override
	public void setIncludedPages(List<FqlPage> includedPagesList) {
		includedPagesPanel.clear();
		for (FqlPage page : includedPagesList) {
			AdminPageItem api = new AdminPageItem(page, presenter);
			includedPagesPanel.add(api);
		}
		includedCount.setText("(" + includedPagesList.size() + ")");
	}

	@Override
	public FbSingleSuggestbox getSuggestBox() {
		return suggestBox;
	}

}
