package my.map;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class ImHere extends MapActivity {
    
	private MapController mapController;
	private final LocationListener locationListener = new LocationListener() {
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// Nie rób nic
		}
		
		@Override
		public void onProviderEnabled(String provider) {
			// Nie rób nic
		}
		
		@Override
		public void onProviderDisabled(String provider) {
			updateWithNewLocation(null);
		}
		
		@Override
		public void onLocationChanged(Location location) {
			updateWithNewLocation(location);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.main);
		
		MapView mapView = (MapView) findViewById(R.id.locationMapView);
		mapController = mapView.getController();
		mapView.setSatellite(true);
		mapView.setStreetView(true);
		mapView.setBuiltInZoomControls(true);
		
		List<Overlay> overlays = mapView.getOverlays();
		MyLocationOverlay myLocationOverlay = new MyLocationOverlay(this, mapView);
		myLocationOverlay.enableMyLocation();
		overlays.add(myLocationOverlay);
		
		LocationManager locationManager;
		String context = Context.LOCATION_SERVICE;
		locationManager = (LocationManager) getSystemService(context);
		
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(false);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		
		String provider = locationManager.getBestProvider(criteria, true);
		
		Location location = locationManager.getLastKnownLocation(provider);
		
		updateWithNewLocation(location);
		
		locationManager.requestLocationUpdates(provider, 2000, 10, locationListener);
	}
	
	private void updateWithNewLocation(Location location) {
		String latLongString;
		TextView myLocationText = (TextView) findViewById(R.id.myLocationText);
		String addressString = "No address found";
		if(location != null) {
			Double geoLat = location.getLatitude()*1E6;
			Double geoLon = location.getLongitude()*1E6;
			GeoPoint point = new GeoPoint(geoLat.intValue(), geoLon.intValue());
			
			mapController.animateTo(point);
			
			double lat = location.getLatitude();
			double lon = location.getLongitude();
			latLongString = "Lat: " + lat + "\nLong: " + lon;
			
			Geocoder geocoder = new Geocoder(this, Locale.getDefault());
			try {
				List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
				StringBuffer buffer = new StringBuffer();
				if(!addresses.isEmpty()) {
					Address address = addresses.get(0);
					for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
						buffer.append(address.getAddressLine(i)).append("\n");
					}
					
					buffer.append(address.getLocality()).append("\n");
					buffer.append(address.getPostalCode()).append("\n");
					buffer.append(address.getCountryName());
					addressString = buffer.toString();
				}
			} catch (Exception e) {
				addressString = e.getMessage();
			}
		} else {
			latLongString = "No location found";
		}
		myLocationText.setText("Your current position is:\n" +
				latLongString + "\n" + addressString);
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}