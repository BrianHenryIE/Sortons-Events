package ie.sortons.events.client;

import java.util.Date;
import java.util.HashSet;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.i18n.client.DateTimeFormat;


// TODO... stop writing things twice


public class EventOverlay extends JavaScriptObject {
	
	protected EventOverlay() {} 
	
	public final native String getEid() /*-{ return this.eid; }-*/;
	public final native String getName() /*-{ return this.name; }-*/;
	public final native String getLocation() /*-{ return this.location; }-*/;
	public final native String getStart_time() /*-{ return this.start_time; }-*/;
	public final native String getPic_square() /*-{ return this.pic_square; }-*/;
	private final native JsArray<FbPage> getFbPagesDetails() /*-{ return this.fbPagesDetail; }-*/;
	
	public final HashSet<FbPage> getFbPagesDetail() {
		
		HashSet<FbPage> pages = new HashSet<FbPage>();
		
		for(int i=0; i<getFbPagesDetails().length(); i++){
			pages.add(getFbPagesDetails().get(i));
		}
		
		return pages;
	}
	
	public final Date getStartTimeDate() { return (this.getStart_time().length()>10 ? DateTimeFormat.getFormat("yyyy-MM-dd'T'hh:mm:ssZ").parse(getStart_time()) : DateTimeFormat.getFormat("yyyy-MM-dd").parse(getStart_time()) ); }
	

	public static class FbPage extends JavaScriptObject {
		protected FbPage() {}
		
		public final native String getId() /*-{ return this.id; }-*/;
		public final native String getName() /*-{ return this.name; }-*/;
		public final native String getLink() /*-{ return this.link; }-*/;
	}
	
	
	
//	"2013-09-15T11:00:00+0100"
//	"2013-09-15T03:00:00.000-07:00",
	
    /**
     * @return The start time formatted e.g. Thursday, 11 September, 2014, at 14:00
     */
	public final String getStartTimeString() { return (this.getStart_time().length()>10 ? DateTimeFormat.getFormat("EEEE, dd MMMM, yyyy, 'at' k:mm").format(getStartTimeDate()) : DateTimeFormat.getFormat("EEEE, dd MMMM, yyyy").format(getStartTimeDate()) ); }
	// public final String getStartTimeString() { return DateTimeFormat.getFormat("EEEE, dd MMMM, yyyy").format(getStartTimeDate()); }
    
    
}
