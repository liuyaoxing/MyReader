package gson.preferencehost;

import com.google.gson.Gson;

public class HostStylerFactory {

	public static void main(String[] args) {
		HostStyler style = new HostStyler();
		
		System.out.println(new Gson().toJson(style));
	}
}
