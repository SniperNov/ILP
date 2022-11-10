package uk.ac.ed.inf.aqmaps;
import java.io.BufferedWriter;
import java.io.File;  // Import the File class
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.http.HttpClient;
import com.mapbox.geojson.*;

public class App {	
	private static final HttpClient client = HttpClient.newHttpClient();
	//Translate the table from readings to air-quality level represent by colour in forms of RGB string
	public static String[] colourConvert(double battery,String reading) {
		String[] output = new String[2];
		String rgb ="";
		String marker_symbol = "";
		if(reading.equals("null")||reading.equals("NaN")||battery<10) {
			rgb = "#000000";
			marker_symbol = "cross";
			}
		else {		
			double x = Double.parseDouble(reading);
			if(x>=0 & x<32) {rgb = "#00ff00";marker_symbol = "lighthouse";}
			if(x>=32 & x<64) {rgb = "#40ff00";marker_symbol = "lighthouse";}
			if(x>=64 & x<96) {rgb = "#80ff00";marker_symbol = "lighthouse";}
			if(x>=96 & x<128) {rgb = "#c0ff00";marker_symbol = "lighthouse";}
			if(x>=128 & x<160) {rgb = "#ffc000";marker_symbol = "danger";}
			if(x>=160 & x<192) {rgb = "#ff8000";marker_symbol = "danger";}
			if(x>=192 & x<224) {rgb = "#ff4000";marker_symbol = "danger";}
			if(x>=224 & x<256) {rgb = "#ff0000";marker_symbol = "danger";}
			if(x==-1) rgb = "#aaaaaa";
		}
		
		//otherwise there must be an invalid reading
		if(rgb.length()==0) throw new IllegalArgumentException("invalidInput!");
		else {
			output[0]= rgb;
			output[1]=marker_symbol;
			return output;
		}
	}

    public static boolean createGeoJsonFile(String jsonString, String fileName) {
	        // Save file create success or not
	        boolean result = true;

	        // Create file saving path
	        String fullPath = fileName + ".geojson";

	        // Create geoJson file
	        try {
	            File file = new File(fullPath);
	            if (file.exists()) { // delete the old file if there is one (overwrite)
	                file.delete();
	            }
	            file.createNewFile();
	            Writer write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
	            write.write(jsonString);
	            write.flush();
	            write.close();
	        } catch (Exception e) {
	            result = false;
	            e.printStackTrace();
	        }
	        return result;
	    }
    
    public static Map<String,Point> readW3Wlist(List<Sensor> list, JsonServer server) throws IOException, InterruptedException{
		Map<String,Point> W3Wmap = new HashMap<String,Point>();
		for(Sensor s:list) {
			String str = s.getLocation();
			Point p = server.getW3W(str).getCoordinates().getP();
			W3Wmap.put(str, p);
		}
		return W3Wmap;
	}
   
    
    public static boolean OutputPath(List<Route> lr,String day,String month, String year, Map<String,Point> W3Wmap, List<Sensor> goal) throws IOException, InterruptedException {
   
    	String filename = "-"+day+"-"+month+"-"+year;
    	
    	//Creating the flight path text file
		BufferedWriter writer = new BufferedWriter(new FileWriter("flightpath"+filename+".txt"));
	    
    	List<Point> points = new ArrayList<>();
    	points.add(lr.get(0).getStart());
    	List<Sensor> waiting = new ArrayList<>();
    	var features = new ArrayList<Feature>();
    	System.out.println("Number of steps: "+ lr.size());
    	for(int i = 0;i<lr.size();i++) {
    		Route r =lr.get(i);
//    		points.add(r.getStart());
    		points.add(r.getEnd());
    		String output = (i+1) +","+r.getStart().longitude()+","+r.getStart().latitude()+","+r.getAngle()+","+r.getEnd().longitude()+","+r.getEnd().latitude()+",";
    		if(r.getS()==null) {
    			output += "null";
    		}else {
    			waiting.add(r.getS());
    			output += r.getS().getLocation();
    			Point p = W3Wmap.get(r.getS().getLocation());
    			Feature pf = Feature.fromGeometry(p);
    			pf.addStringProperty("location", r.getS().getLocation());
    			String reading = r.getS().getReading();
    			double battery = r.getS().getBattery();
    			pf.addStringProperty("rgb-string",colourConvert(battery,reading)[0]);			
    			pf.addStringProperty("marker-color", colourConvert(battery,reading)[0]);
    			pf.addStringProperty("marker-symbol", colourConvert(battery,reading)[1] );
    			features.add(pf);
    		} 		
    	        writer.write(output);
    	        writer.newLine();
    	    }
    	writer.close();
//    	List<Point> boundary = new ArrayList<>();
//    	boundary.add(Point.fromLngLat(-3.192473 , 55.946233));
//    	boundary.add(Point.fromLngLat(-3.184319, 55.946233));
//    	boundary.add(Point.fromLngLat(-3.184319 , 55.942617));
//    	boundary.add(Point.fromLngLat(-3.192473 , 55.942617));
//    	boundary.add(Point.fromLngLat(-3.192473 , 55.946233));
//    	LineString area = LineString.fromLngLats(boundary);
//    	Feature cfm = Feature.fromGeometry(area);
    	
//    	for(NoFlyZone zone:zones) {
//    		Feature pf = Feature.fromGeometry(zone.getRegion());
//    		pf.addStringProperty("fill", "#ff0000");
//    		pf.addNumberProperty("fill-opicity", 0.75);
//    		features.add(pf);
//    	}

    	LineString linestring = LineString.fromLngLats(points);
    	Feature lsf = Feature.fromGeometry(linestring);
    	
    	if(waiting.size()<goal.size()) {
    		for(Sensor s:goal) {
    			if(!waiting.contains(s)) {
    				Point p = W3Wmap.get(s.getLocation());
        			Feature pf = Feature.fromGeometry(p);
        			pf.addStringProperty("location", s.getLocation());
        			pf.addStringProperty("rgb-string","#aaaaaa");			
        			pf.addStringProperty("marker-color", "#aaaaaa");
        			pf.addStringProperty("marker-symbol", "no symbol" );
        			features.add(pf);
    			}
    		}
    	}
    	features.add(lsf);
//    	features.add(cfm);
    	FeatureCollection  fc = FeatureCollection.fromFeatures(features);
    	
    	boolean  marker = createGeoJsonFile(fc.toJson(),"readings"+filename);
    	return marker;
    }
    
    public static void main( String[] args ) throws InterruptedException, IOException
    {	
    
   		JsonServer server = new JsonServer(client,args[6]);  	
    	List<Sensor> ls = server.getSensors(args[0],args[1],args[2]);
    	Point startpoint = Point.fromLngLat(Double.parseDouble(args[4]),  Double.parseDouble(args[3]));
    	Map<String,Point> W3Wmap = readW3Wlist(ls,server);
    	List<NoFlyZone> nfz =server.getNoFlyZones();
    	Drone d = new Drone(ls,startpoint,nfz,W3Wmap);
    	
    	try {
    		if((OutputPath(d.flyPath(),args[0],args[1],args[2],W3Wmap,ls))) {
    			System.out.println("DONE!");
    		}else {
    			System.out.print("File Create Failed!");
    		}
    	}catch(java.lang.IllegalArgumentException e) {
    		System.out.print(e);
    	}
    	
//    	Sensor S1 = new Sensor();
//    	S1.setLocation("strut.river.surely");
//    	S1.setBattery(90);
//    	S1.setReading("23.333");
//    	Point p1 = W3Wmap.get(S1.location);
//    	Sensor S2 = new Sensor();
//    	S2.setLocation("hooked.shine.third");
//    	S2.setBattery(90);
//    	S2.setReading("23.333");
//    	Point p2 = W3Wmap.get(S2.location);
//    	Sensor S3 = new Sensor();
//    	S3.setLocation("acid.chair.butter");
//    	S3.setBattery(90);
//    	S3.setReading("23.333");
//    	Point p3 = W3Wmap.get(S3.location);
//    	Point p4 = Point.fromLngLat(-3.187045999999999,55.94416519237887);
//    	Point p5 = Point.fromLngLat(-3.1867640922137634,55.94406258633587);
//    	Point p6 = Point.fromLngLat(-3.1888719, 55.945268);  			
//    	Point p7 = Point.fromLngLat(-3.186296,55.945761);


//    	System.out.println(p2.longitude());
//    	System.out.println(p2.latitude());
//    	OutputPath(d.cost(p6,p7),"44","44","44",nfz,W3Wmap);
//    	OutputPath(d.NoBarrierMove(p6,p7),"44","44","44",nfz,W3Wmap);

//    	System.out.println(d.nobarrierOnTheWay(p6,p7));
//    	System.out.println(Math.sqrt( Math.pow(p6.longitude()-p7.longitude(),2) + Math.pow(p6.latitude()-p7.latitude(), 2)) );
//    	List<Sensor> sl = new ArrayList<>();
////    	sl.add(S1);
//    	sl.add(S2);
//    	sl.add(S3);
//    	OutputPath(d.tourValue(sl),"55","55","55",nfz,W3Wmap);
//    	OutputPath(d.cost(startpoint, p2),"33","33","33",nfz,W3Wmap);
  
    	
    	
    	
  	
    	
//    	List<Route> lr =d.twoOpt(ls);
//    	for(Route r:lr) {
//    		if(r.getS()!=null) {
//    			System.out.println(r.getS().location);
//    		}
//    	}
//    	List<Feature> fs = new ArrayList<>();
//    	for(NoFlyZone zone:d.zones) {
//    		Feature f = Feature.fromGeometry(zone.getRegion());
//    		fs.add(f);
//    	}
//    	FeatureCollection fc = FeatureCollection.fromFeatures(fs);
//    	if(createJsonFile(fc.toJson(), "NoFlyZones")) {
//			System.out.print("DONE!");
//		}else {
//			System.out.print("File Create Failed!");
//		}
    	
    	
    	
    	
//    	Point t = Point.fromLngLat(-3.1871804594993587,
//              55.94448750356385);
//    	System.out.print(d.nobarrierOnTheWay(a,t));
    	
    	//**Test NoBarrierOntheWay()**//   Pass
//    	d.nobarrierOnTheWay(b, a);
    	
    	
//    	**Test NoBarrierMove()**//   	Pass
    	
    	
    	
    	//**Test Cost()**//	 Pass
//    	Point A = Point.fromLngLat(55.945114,-3.192221);
//    	Point B = Point.fromLngLat(55.944575,-3.185236);    	
//    	d.cost(A, B);
//    	
    	
    }
}
