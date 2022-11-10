package uk.ac.ed.inf.aqmaps;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Polygon;
public class JsonServer {

	private HttpClient client;
//	private String filename;
	private String port;
	public JsonServer(HttpClient client, String port) {
		this.client = client;
//		this.filename = filename;
		this.port = port;	
	}
	
    public List<NoFlyZone> getNoFlyZones() throws IOException, InterruptedException {
    	String urlString = "http://localhost:"+port+"/buildings/no-fly-zones.geojson";
    	var request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
    	try {
    		HttpResponse<String> response = client.send(request,BodyHandlers.ofString());
    		List<Feature> nfz= (ArrayList<Feature>)FeatureCollection.fromJson(response.body()).features();
    		List<NoFlyZone> allnfz = new ArrayList<>();
    		for(int i = 0;i < nfz.size(); i++) {
    			Feature feature = nfz.get(i);
    			String name = feature.getStringProperty("name");
    			Polygon poly = (Polygon) feature.geometry();
    			NoFlyZone zone = new NoFlyZone();
    			zone.setCoordinates( poly.coordinates());
    			zone.setName(name);
    			allnfz.add(zone);     
    		}	
    		return allnfz;    
    	}catch( java.net.ConnectException e) {
			System.out.print("Invalid port!!");
		}
		return null;
    }
    
    public W3W getW3W (String words) throws IOException, InterruptedException{

    	String[] Words = words.split("[.]");
    	String urlString = "http://localhost:"+port+"/words/" + Words[0]+"/"+ Words[1] +"/"+Words[2] + "/details.json";
    	var request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
		try{
			var response = client.send(request, BodyHandlers.ofString());
			var content = response.body();	
			W3W location = new Gson().fromJson(content, W3W.class);
			return location;
		}catch( java.net.ConnectException e) {
			System.out.print("Invalid port!!");
		}
		return null;
    }
    
    public List<Sensor> getSensors(String day, String month, String year) throws InterruptedException, IOException  {
    	String uriString =  "http://localhost:"+port+"/maps/"+year+"/"+month+"/"+day+"/air-quality-data.json";
		var request = HttpRequest.newBuilder().uri(URI.create(uriString)).build();
		try{
			try{
			var response = client.send(request, BodyHandlers.ofString());
			var content = response.body();
			Type listType =new TypeToken<ArrayList<Sensor>>() {}.getType();
			ArrayList<Sensor> sensorslist = new Gson().fromJson(content, listType);
			return sensorslist;
		}catch( java.net.ConnectException e) {
			System.out.print("Invalid port!!");
		}
    }catch(java.lang.IllegalStateException e2) {
    	System.out.print("Invalid date!!");
		}
		return null;
    }
}
