package uk.ac.ed.inf.aqmaps;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.mapbox.geojson.*;


public class Drone {
	private final List<NoFlyZone> zones;
	private final double step = 0.0003;
	private final Point startingpoint;
	private final List<Sensor> goal;
	private final Map<String, Point> W3Wmap;
	private int Battery;
	
	public Drone(List<Sensor> g,Point star,List<NoFlyZone> nfz, Map<String, Point> W3Wmap) throws IOException, InterruptedException {
		super();
		this.W3Wmap = W3Wmap;
		this.startingpoint = star;
		this.goal = g;
		this.zones = nfz;
		this.Battery = 150;
	}
	private boolean hasBattery() {
		return Battery>=0;
	}
	private void charging() {
		this.Battery = 150;
	}
//**************************************************************************************
//***************Estimate whether there are barriers on the way***********************
//**************************************************************************************
	public boolean noBarrier(Point start, Point destination) {
		boolean nobarrier = true;
		var x3 = start.longitude();
		var y3 = start.latitude();
		var x4 = destination.longitude();
		var y4 = destination.latitude();
		for(NoFlyZone z:zones) {
			var boundaries = z.getBoundaries();			
			for (LineString b:boundaries) { // For Each Boundary, estimate whether it intersects with the starting-goal segment
				var x1 = b.coordinates().get(0).longitude();
				var y1 = b.coordinates().get(0).latitude();
				var x2 = b.coordinates().get(1).longitude();
				var y2 = b.coordinates().get(1).latitude();
				if(Line2D.linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4)) {
					return false;
				}
			}			
		}
		return nobarrier;
	}
	
	
//**************************************************************************************
//**************** Estimate whether a point is in any No-Fly-Zone **********************
//**************************************************************************************
	public boolean pnpoly(Point A) {
		boolean c = false;

		for(NoFlyZone z:zones) {
			var area = z.getCoordinates().get(0);
			int n = z.getCoordinates().get(0).size();
			for (int i = 0, j = n - 1; i < n; j = i++) {
				if (((area.get(i).latitude() > A.latitude()) != (area.get(j).latitude() > A.latitude())) 
						&& (A.longitude() < (area.get(j).longitude() - area.get(i).longitude()) * 
								(A.latitude() - area.get(i).latitude()) / (area.get(j).latitude() - area.get(i).latitude()) 
									+ area.get(i).longitude())){
						c = !c;
					}		
			}
		}
		return c;
	}
//**************************************************************************************
//************ Estimate whether a point is out of the Confinement Area *****************
//**************************************************************************************
	public boolean inConfArea(Point A) {
		if(A.longitude()<-3.192473||A.longitude()>-3.184319||A.latitude()>55.946233||A.latitude()<55.942617) {
			return false;
		}else {
			return true;
		}
		
	}
	
	
//**************************************************************************************
//********Plan the path Fly if no barrier on the straight line from point to point **********
//**************************************************************************************
	public List<Route> flyStraight(Point A, Point B){		
		List<Route> routes = new ArrayList<>();
		List<Point> points = new ArrayList<>();
		points.add(A);
		Point Start = A;
		double roundfactor = 0;
		while(true) {
			if(!hasBattery()) {
				return routes;
			}
//			System.out.println("Now i am in while true loop!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//			System.out.println(Start.longitude());
//			System.out.println(Start.latitude());
//			System.out.println(B.longitude());
//			System.out.println(B.latitude());
			double x = (B.longitude()-Start.longitude()); //Change in longitude
			double y = (B.latitude()-Start.latitude()); //Change in latitude
//			double theorydistance = Math.sqrt(Math.pow(x,2) + Math.pow(y,2)); //Straight line distance
			double rawangle = Math.toDegrees((Math.atan2(y,x)));
//			System.out.println("rawangle:"+rawangle);
			Route r = new Route();
			int angle;
			if (rawangle>=0) {
				angle = ((int)Math.round(rawangle/10))*10;
			}else {
				angle = ((int)Math.round(36+rawangle/10))*10;
			}
//			System.out.println("angle:"+angle);
			angle += roundfactor;
			if(rawangle-angle<0) {
				roundfactor = -10;
			}
			if(rawangle-angle>0) {
				roundfactor = 10;
			}
//			System.out.println("roundfactor:"+roundfactor);
			Point End = Point.fromLngLat(Start.longitude()+step*Math.cos(Math.toRadians(angle)), Start.latitude()+step*Math.sin(Math.toRadians(angle)));
			
			int[] penalty = new int [] {10,-10,20,-20,30,-30,40,-40,50,-50,60,-60,70,-70,80,-80,90,-90,100,-100,110,-110,120,-120,130,-130,140,-140,150,-150,160,-160,170,-170,180,-180};
			int finalangle = angle;
			for(int pen:penalty) {
				if(!pnpoly(End)&&inConfArea(End)&&noBarrier(Start,End)) {
					break;
				}else {
					finalangle = angle + pen;
//					System.out.println("finalangle after add pen:"+finalangle);
					End = Point.fromLngLat(Start.longitude()+step*Math.cos(Math.toRadians(finalangle)), Start.latitude()+step*Math.sin(Math.toRadians(finalangle)));

				}
			}
			r.setStart(Start);
			r.setEnd(End);
			r.setS(null);
//			System.out.println("finalangle:"+finalangle);
			r.setAngle(finalangle);
			routes.add(r);
			Battery -=1;
			points.add(End);
//			LineString ls = LineString.fromLngLats(points);
//			Feature f = Feature.fromGeometry(ls);
//			FeatureCollection fc = FeatureCollection.fromFeature(f);
//			System.out.println(fc.toJson());
//			System.out.println("theorydistance:"+theorydistance);
			double theorydistance = Math.sqrt(Math.pow(B.longitude()-End.longitude(),2) + Math.pow(B.latitude()-End.latitude(),2)); //Straight line distance
			if (theorydistance < 0.0002) {
				break;
			}else {
				Start = End;
			}			
		}
		//~~~~~~~~~~~~~~~~~~~~~~
//		System.out.println(" I am now in cost mark2: Reached the destination!");
		//~~~~~~~~~~~~~~~~~~~~~~
		return routes;
	}
	
	
	
	
	
	
	
//**************************************************************************************
//*********************************Helper 2********************************************
//**************************************************************************************
	public boolean isVisited(List<Point> visited, Point A) {
		for(Point visit:visited) {
			if (Math.pow((A.longitude()-visit.longitude()),2)+Math.pow((A.latitude()-visit.latitude()),2) <= 0.0001*0.0001) {
				return true;
			}
		}
		return false;
	}
	
//**************************************************************************************
//**************************************************************************************
//**************************************************************************************
	public List<Route> cost(Point A, Point B)  {
		ArrayList<ArrayList<Point>> tree = new ArrayList<>(); //Avoid No-Fly Buildings by using BFS
		ArrayList<Point> root = new ArrayList<>();
		root.add(A);
		tree.add(root);
			
		List<Point> visited = new ArrayList<Point>(); //the visited point address book
		visited.add(A);
		List<Route> routes = new ArrayList<>(); //the final routes from A to the Sensor
		int[] Anglelist = new int[] {0,10,-10,20,-20,30,-30,40,-40,50,-50,60,-60,70,-70,80,-80,90,-90,100,-100,110,-110,120,-120,130,-130,140,-140,150,-150,160,-160,170,-170,180,-180};
		while (true) {
			
			ArrayList<Point> routeNow = tree.get(0); //The route we are looking at in BFS order 
			Point now = routeNow.get(routeNow.size()-1); // the leaf of the node, whether there is barrier from this leaf's position to the Sensor
			
			if (noBarrier(now,B)) {
				if(routeNow.size()!=1) {
				//Save the steps which is take for avoid barrier
					for(int i = 0; i < routeNow.size()-1; i++) {
						Route r = new Route();
						Point p1 = routeNow.get(i);
						Point p2 = routeNow.get(i+1);
						double x = p2.longitude()-p1.longitude();
						double y = p2.latitude()-p1.latitude();
						int angle =((int)Math.round(Math.toDegrees((Math.atan2(y,x))/10)))*10;
						if(angle<0) {
							angle +=360;
						}
						r.setStart(p1);
						r.setEnd(p2);
						r.setAngle(angle);
						r.setS(null);
						routes.add(r);
						Battery-=1;
						if(!hasBattery()) {
							return routes;
						}
					}
				}
				routes.addAll(flyStraight(now,B));				

				return routes;
				}
			// if not, expand the leaf, check whether its children can take 
			double x = (B.longitude()-now.longitude()); //Change in longitude
			double y = (B.latitude()-now.latitude()); //Change in latitude
			int tangent = (int) Math.atan2(y, x);
			for(int angle:Anglelist) {
				double changeinx = step*Math.cos(Math.toRadians(tangent+angle));
				double changeiny = step*Math.sin(Math.toRadians(tangent+angle));
				Point next = Point.fromLngLat(now.longitude() + changeinx, now.latitude() + changeiny);
				if(isVisited(visited,next)||pnpoly(next)||!noBarrier(now,next)||!inConfArea(next)) {
//					
					continue;
				}
//				
				else {
					visited.add(next);
					ArrayList<Point> newList = new ArrayList<>();
					newList.addAll(routeNow);
					newList.add(next);
					tree.add(newList);
				}
			}
			
			//delete the expanded leaf
			tree.remove(0);
			
			}

	}
	
//**************************************************************************************
//*********************************Helper 2********************************************
//**************************************************************************************	
	public List<Route> tourValue(List<Sensor> permutation) throws IOException, InterruptedException {
		charging();
		List<Route> routes = new ArrayList<Route>();		
		//calculate the cost from sensor to sensor
		Point first = (Point) W3Wmap.get(permutation.get(0).getLocation());
		
		routes.addAll(cost(startingpoint,first));
		if(!hasBattery()) {
			return null;
		}
		//Need to change the 
		Route lastStepInFirst = routes.get(routes.size()-1);
		lastStepInFirst.setS(permutation.get(0));
		routes.remove(routes.size()-1);
		routes.add(lastStepInFirst);
		
		
		for(int i=0;i<permutation.size()-1;i++){		
			Point Start = routes.get(routes.size()-1).getEnd();
			Point stop2 = (Point) W3Wmap.get(permutation.get(i+1).getLocation());
			routes.addAll(cost(Start,stop2));
			Route lastSteptoTarget = routes.get(routes.size()-1);
			lastSteptoTarget.setS(permutation.get(i+1));
			routes.remove(routes.size()-1);
			routes.add(lastSteptoTarget);	
			if(!hasBattery()) {
				return null;
			}
			
		}
		Point last = routes.get(routes.size()-1).getEnd();
		routes.addAll(cost(last,startingpoint));
		return routes;
	}
	
//**************************************************************************************
//**************************************************************************************
//**************************************************************************************
	public List<Sensor> reverseHeuristic(List<Sensor> sss,int i,int j) throws IOException, InterruptedException {
			List<Sensor> reverse = new ArrayList<>();
			// same permutation from start to sensor_i
			if(i!=0) {
				for(int q =0;q<i;q++) {
					reverse.add(sss.get(q));
				}
			}
			// reverse permutation from the i_th one to the j_th one(included)
			for(int q = j;q>=i;q--) {
				reverse.add(sss.get(q));
			}
			//same permutation from j_th till the last
			if(j!=sss.size()-1) {
				for(int q = j+1;q<sss.size();q++) {
					reverse.add(sss.get(q));
				}
			}
			List<Route> before = tourValue(sss);
			List<Route> after = tourValue(reverse);
			if(before==null) {
				return reverse;
			}
			if(after ==null){
				return sss;
			}
			if(before.size() <=after.size()) {
				return sss;
			}else {
				return reverse;
			}
	}
	
//**************************************************************************************
//********************** The entrance of the Algorithm *********************************
//**************************************************************************************	
	public List<Route> flyPath() throws IOException, InterruptedException{
		System.out.println("Please wait! My drone is working on it!");
		System.out.println("You know, TwoOpt takes some time......");

		if(goal.size()<1) {
    		throw new IllegalArgumentException("Are you sure there is No Work assigned to my little drone?");
    	}
		List<Sensor> permutation = roughPath(goal);		
		for(int j =1;j<permutation.size();j++) {
			for(int i = 0; i < j;i++) {
				permutation = reverseHeuristic(permutation,i,j);		
			}
		}
		List<Route> rs = tourValue(permutation);
		if(rs==null) {
			goal.remove(0);
			return flyPath();
		}
		return rs;
		
	}
	
//**************************************************************************************
//**************Pre arranging the permutation (For number of step optimizing)***********************************
//**************************************************************************************	
	public List<Sensor> roughPath(List<Sensor> list) throws IOException, InterruptedException{
		List<Sensor> unvisited = new ArrayList<>();
		List<Sensor> returned = new ArrayList<>();
		unvisited.addAll(list);
		Point begin = startingpoint;
		Sensor next = new Sensor();
		for(int i = 0; i<list.size();i++) {
			double distance = 150.0;
			for(Sensor unv:unvisited) {
				Point p = (Point) W3Wmap.get(unv.getLocation());
				double x = (p.longitude()-begin.longitude()); //Change in longitude
				double y = (p.latitude()-begin.latitude());
				double d = Math.sqrt(x*x+y*y);
				if(d<=distance) {
					next = unv;
					distance = d;
				}
			}
			returned.add(next);
			unvisited.remove(next);
			begin = (Point) W3Wmap.get(next.getLocation());
		}
		return returned;	
	}
	
}
