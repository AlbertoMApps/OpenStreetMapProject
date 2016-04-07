package development.alberto.com.osmdroid;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.DirectedLocationOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private MapView myOpenMapView;
    private LocationManager locationManager;
    private IMapController mapController;
    private GeoPoint currentLocation;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //Introduction
        myGeolocation(); //get my current location by gps
        myOpenMapView = (MapView) findViewById(R.id.openmapview);
        myOpenMapView.setBuiltInZoomControls(true);
        myOpenMapView.setMultiTouchControls(true);
        //GeoPoint startPoint = new GeoPoint(51.52, -0.1163);
        GeoPoint startPoint = currentLocation;
        mapController = myOpenMapView.getController();
        mapController.setZoom(10);
        mapController.setCenter(startPoint);
        //Markers option
        //0. Using the Marker overlay
        Marker startMarker = new Marker(myOpenMapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("Start point");
        //startMarker.setIcon(getResources().getDrawable(R.drawable.marker_kml_point).mutate());
        //startMarker.setImage(getResources().getDrawable(R.drawable.ic_launcher));
        //startMarker.setInfoWindow(new MarkerInfoWindow(R.layout.bonuspack_bubble_black, map));
        startMarker.setDraggable(true);
        startMarker.setOnMarkerDragListener(new OnMarkerDragListenerDrawer());//inner class for the maker options
        myOpenMapView.getOverlays().add(startMarker);

        //1. "Hello, Routing World"
        RoadManager roadManager = new OSRMRoadManager(this);
        //2. Playing with the RoadManager
        //roadManager roadManager = new MapQuestRoadManager("YOUR_API_KEY");
        //roadManager.addRequestOption("routeType=bicycle");
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(startPoint);
        GeoPoint endPoint = new GeoPoint(48.4, -1.9);
        Marker endMarker = new Marker(myOpenMapView);
        endMarker.setTitle("End point");
        endMarker.setPosition(endPoint);
        waypoints.add(endPoint);
        Road road = roadManager.getRoad(waypoints); //roadManager
        if (road.mStatus != Road.STATUS_OK) {
            Toast.makeText(this, "Error when loading the road - status=" + road.mStatus, Toast.LENGTH_SHORT).show();
        }

        Polyline roadOverlay = RoadManager.buildRoadOverlay(road, this);
        myOpenMapView.getOverlays().add(roadOverlay);
        myOpenMapView.getOverlays().add(endMarker);


        //Adding new list of overlays...***
        ArrayList<OverlayItem> anotherOverlayItemArray;
        anotherOverlayItemArray = new ArrayList<OverlayItem>();

        anotherOverlayItemArray.add(new OverlayItem("0, 0", "0, 0", new GeoPoint(0, 0)));
        anotherOverlayItemArray.add(new OverlayItem("US", "US", new GeoPoint(38.883333, -77.016667)));
        anotherOverlayItemArray.add(new OverlayItem("China", "China", new GeoPoint(39.916667, 116.383333)));
        anotherOverlayItemArray.add(new OverlayItem("United Kingdom", "United Kingdom", new GeoPoint(51.5, -0.116667)));
        anotherOverlayItemArray.add(new OverlayItem("Germany", "Germany", new GeoPoint(52.516667, 13.383333)));
        anotherOverlayItemArray.add(new OverlayItem("Korea", "Korea", new GeoPoint(38.316667, 127.233333)));
        anotherOverlayItemArray.add(new OverlayItem("India", "India", new GeoPoint(28.613333, 77.208333)));
        anotherOverlayItemArray.add(new OverlayItem("Russia", "Russia", new GeoPoint(55.75, 37.616667)));
        anotherOverlayItemArray.add(new OverlayItem("France", "France", new GeoPoint(48.856667, 2.350833)));
        anotherOverlayItemArray.add(new OverlayItem("Canada", "Canada", new GeoPoint(45.4, -75.666667)));

        ItemizedOverlayWithFocus<OverlayItem> anotherItemizedIconOverlay = new ItemizedOverlayWithFocus<OverlayItem>(this, anotherOverlayItemArray, myOnItemGestureListener);
        myOpenMapView.getOverlays().add(anotherItemizedIconOverlay);
    }

    private void myGeolocation() {

        MyLocationListener locationListener = new MyLocationListener();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        } //check-permissions for gps
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longt = location.getLongitude();
        double lat = location.getLatitude();
        if( location != null ) {
            currentLocation = new GeoPoint(lat, longt);
        }
    }

    //Touching and displaying on touch event***
    ItemizedIconOverlay.OnItemGestureListener<OverlayItem> myOnItemGestureListener = new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {

        @Override
        public boolean onItemLongPress(int arg0, OverlayItem arg1) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean onItemSingleTapUp(int index, OverlayItem item) {
            Toast.makeText(MainActivity.this, item.getTitle() + "\n :"
                            + item.getPoint().getLatitude() + " : "
                            + item.getPoint().getLongitude(), Toast.LENGTH_LONG).show();

            return true;
        }

    };

    //0. Using the Marker and Polyline overlays - advanced options
    class OnMarkerDragListenerDrawer implements Marker.OnMarkerDragListener {
        ArrayList<GeoPoint> mTrace;
        Polyline mPolyline;

        OnMarkerDragListenerDrawer() {
            mTrace = new ArrayList<GeoPoint>(100);
            mPolyline = new Polyline(myOpenMapView.getContext());
            mPolyline.setColor(0xAA0000FF);
            mPolyline.setWidth(2.0f);
            mPolyline.setGeodesic(true);
            myOpenMapView.getOverlays().add(mPolyline);
        }

        @Override public void onMarkerDrag(Marker marker) {
            //mTrace.add(marker.getPosition());
        }

        @Override public void onMarkerDragEnd(Marker marker) {
            mTrace.add(marker.getPosition());
            mPolyline.setPoints(mTrace);
            myOpenMapView.invalidate();
        }

        @Override public void onMarkerDragStart(Marker marker) {
            //mTrace.add(marker.getPosition());
        }
    }

    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            currentLocation = new GeoPoint(location); //You need this one to update your location on change
//            displayMyCurrentLocationOverlay(); //I am not using this overlay to display the results in the map...
        }

        private void displayMyCurrentLocationOverlay() {
//            if( currentLocation != null) {
//                OverlayItem currentLocationOverlay = null;
//                OverlayItem myCurrentLocationOverlayItem = null;
//                if( currentLocationOverlay == null ) {
//                    currentLocationOverlay = new ArrayItemizedOverlay(myLocationMarker);
//                    myCurrentLocationOverlayItem = new OverlayItem(currentLocation, "My Location", "My Location!");
//                    currentLocationOverlay.addItem(myCurrentLocationOverlayItem);
//                    myOpenMapView.getOverlays().add(currentLocationOverlay);
//                } else {
//                    myCurrentLocationOverlayItem.setMarker(currentLocation);
//                    currentLocationrentLocationOverlay.requestRedraw();
//                }
//                myOpenMapView.getController().setCenter(currentLocation);
//            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }


}