package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.List;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.geojson.LineString;
public class NoFlyZone {
//	List<List<List<Double>>> coordinates;
	private List<List<Point>> coordinates;
	private String name;
	
	public List<LineString> getBoundaries(){ 
		var lines = coordinates.get(0);
		var ls = new ArrayList<LineString>();
		for(int i = 0; i < lines.size()-1;i++) {
			List<Point> points = new ArrayList<>();
			points.add(lines.get(i));
			points.add(lines.get(i+1));
			LineString line = LineString.fromLngLats(points);
			ls.add(line);
		}
		return ls;
				
	}
	
	public List<List<Point>> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(List<List<Point>> coordinates) {
		this.coordinates = coordinates;
	}

	public Polygon getRegion() {
		Polygon poly = Polygon.fromLngLats(List.of(coordinates.get(0)));
		return poly;
	}
//	
//	public void setP(List<List<List<Double>>> coordinates) {
//		this.coordinates = coordinates;
//	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}	
	
}
