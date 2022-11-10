package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

public class Route {
	private Sensor endS;	
	private Point Start;
	private Point End;
	private int Angle;
	public int getAngle() {
		return Angle;
	}
	public void setAngle(int angle) {
		this.Angle = angle;
	}
	public Point getStart() {
		return Start;
	}
	public void setStart(Point start) {
		Start = start;
	}
	public Point getEnd() {
		return End;
	}
	public void setEnd(Point end) {
		End = end;
	}
	public Sensor getS() {
		return endS;
	}
	public void setS(Sensor s) {
		endS = s;
	}
}
