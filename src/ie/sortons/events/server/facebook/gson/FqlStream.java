package ie.sortons.events.server.facebook.gson;



import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import lombok.Getter;
import lombok.NoArgsConstructor;


/*
{ "data": [
      {
         "post_id": "139957459378369_128060617343105",
         "actor_id": 600747004,
         "target_id": 139957459378369,
         "message": "Deal going over this weekend only. perfect for all at UCD\nhttp://dublin.ratemyarea.com/deals/490",
         "attachment": {
            "media": [
               {
                  "href": "http://dublin.ratemyarea.com/deals/490",
                  "alt": "",
                  "type": "link",
                  "src": "http://external.ak.fbcdn.net/safe_image.php?d=AQBM_0OZq_De_NEG&w=90&h=90&url=http\u00253A\u00252F\u00252Fimages.ratemyarea.com.s3.amazonaws.com\u00252Fassets\u00252F0295\u00252F6894\u00252Fsubway_large_thumb.png\u00253Fpicture\u00253D2956894"
               }
            ]
         }
      }, 
*/


@Getter @NoArgsConstructor
public class FqlStream {

	private FqlStreamItem[] data; 
 
	public String toString(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	
	
	
	@Getter @NoArgsConstructor
	public static class FqlStreamItem {
	/*	
	    
	    	{
	         "post_id": "139957459378369_128060617343105",
	         "actor_id": 600747004,
	         "target_id": 139957459378369,
	         "message": "Deal going over this weekend only. perfect for all at UCD\nhttp://dublin.ratemyarea.com/deals/490",
	         "attachment": {
	            "media": [
	               {
	                  "href": "http://dublin.ratemyarea.com/deals/490",
	                  "alt": "",
	                  "type": "link",
	                  "src": "http://external.ak.fbcdn.net/safe_image.php?d=AQBM_0OZq_De_NEG&w=90&h=90&url=http\u00253A\u00252F\u00252Fimages.ratemyarea.com.s3.amazonaws.com\u00252Fassets\u00252F0295\u00252F6894\u00252Fsubway_large_thumb.png\u00253Fpicture\u00253D2956894"
	               }
	            ]
	         }
	    	}
	*/
		
		private String post_id;
		private String actor_id;
		private String target_id;
		private String message;
		
		private FqlStreamItemAttachment attachment;
		

		public String toString(){
			Gson gson = new Gson();
			return gson.toJson(this);
		}

	}
	

	public static class FqlStreamItemAttachmentAdapter implements JsonDeserializer<FqlStreamItemAttachment> {
			
		@Override
		public FqlStreamItemAttachment deserialize(JsonElement json, Type type, JsonDeserializationContext context) 
				throws JsonParseException {
		
			FqlStreamItemAttachment attachment = null;
			
			// I think the term to describe this is "polymorphic"
			if (json.isJsonArray()) {
				
	            attachment = null;
	            
	        } else if (json.isJsonObject()) {
	        	
	        	// TODO: this is done wrong.
	        	// It should be something like:
	        	// attachment = (FqlStreamItemAttachment) context.deserialize(json, FqlStreamItemAttachment.class);
	        	// but that has this Adapter attached so it recurses inside here and Internal Server Error occurs.
	        	// I'm not sure how to actually use the FqlStreamItemAttachment class in here without starting a new Gson object
	        	
	        	Gson gson = new Gson();
	        	attachment = (FqlStreamItemAttachment) gson.fromJson(json, FqlStreamItemAttachment.class);
	        	
	        } else {
	            throw new RuntimeException("Unexpected JSON type: " + json.getClass());
	        }
			return attachment;
			
		}
	
	}
	

	
	
	@Getter @NoArgsConstructor
	public static class FqlStreamItemAttachment {

		private FqlStreamItemAttachmentMediaItem[] media; 
	  

		public String toString(){
			Gson gson = new Gson();
			return gson.toJson(this);
		}
		
	}
	
	@Getter @NoArgsConstructor
	public static class FqlStreamItemAttachmentMediaItem {
		
		private String href;
		private String alt;
		private String type;
		private String src;
		
		private FqlStreamItemAttachmentMediaItemPhoto photo;
		
		public String toString(){
			Gson gson = new Gson();
			return gson.toJson(this);
		}
	}
	
	
	@Getter @NoArgsConstructor
	public static class FqlStreamItemAttachmentMediaItemPhoto {

		private String aid;
		private String pid;
		private String fbid;
		private String owner;
		private String index;
		private String width;
		private String height;
		
		public FqlStreamItemAttachmentMediaItemPhotoImage[] images;
		
	}
	
	

	@Getter @NoArgsConstructor
	public static class FqlStreamItemAttachmentMediaItemPhotoImage {
		
		private String src;
		private String width;
		private String height;
		
	}
}
