package ie.sortons.events.shared;

import java.util.ArrayList;

public class FbDataArray<T> {

	private ArrayList<T> data;
	
	public ArrayList<T> getData() {
		return data;
	}
	
	private Error error;
	
	public Error getError(){
		return error;
	}
	
	public class Error {
		private String message;
		private String type;
		private String code;
		
		public String getMessage() {
			return message;					
		}
		
		public String getType() {
			return type;
		}
		
		public String getCode() {
			return code;
		}
	}
}
