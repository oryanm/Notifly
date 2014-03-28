package net.notifly.core.gui.activity.map;

import android.app.Activity;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import net.notifly.core.R;

public class SelectLocationActivity extends Activity {
    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);
        setMap();
    }

    private void setMap() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location currentLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        LatLng cameraTarget = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map = mapFragment.getMap();
        map.setMyLocationEnabled(true);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraTarget, 10));
        Marker marker = map.addMarker(new MarkerOptions()
                .position(cameraTarget)
                .draggable(true));
        map.setOnMapClickListener(new MapListener(marker));
    }

    class MapListener implements GoogleMap.OnMapClickListener {
        Marker marker;

        MapListener(Marker marker) {
            this.marker = marker;
        }

        @Override
        public void onMapClick(LatLng latLng) {
            marker.setPosition(latLng);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
