package uk.ac.ed.inf.aqmaps;


import com.mapbox.geojson.Point;

public class W3W {
	private String country;
	private Square square;
	private String name;
	private MyPoint coordinates;	
	private String words;
	private String language;
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Square getSquare() {
		return square;
	}

	public void setSquare(Square square) {
		this.square = square;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MyPoint getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(MyPoint coordinates) {
		this.coordinates = coordinates;
	}

	public String getWords() {
		return words;
	}

	public void setWords(String words) {
		this.words = words;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getMap() {
		return map;
	}

	public void setMap(String map) {
		this.map = map;
	}

	public String map;
	
	public class MyPoint{
		public double lng;
		public double lat;
		public Point p;
		
		public Point getP() {
			Point p = Point.fromLngLat(lng, lat);
			return p;
		}
	}
	
	public class Square{
		public MyPoint southwest;
		public MyPoint northeast;
	}
}
