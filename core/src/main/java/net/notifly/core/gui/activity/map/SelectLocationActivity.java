package net.notifly.core.gui.activity.map;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import net.notifly.core.R;
import net.notifly.core.gui.activity.note.NewNoteActivity;
import net.notifly.core.util.GoogleMapsZoom;
import net.notifly.core.util.LocationHandler;

public class SelectLocationActivity extends Activity {
    GoogleMap map;
    Marker selectedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);
        setMap();
    }

    private void setMap() {
        CameraUpdate cameraTarget = getCameraTarget();

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map = mapFragment.getMap();
        map.setMyLocationEnabled(true);
        map.moveCamera(cameraTarget);
        selectedLocation = map.addMarker(new MarkerOptions()
                .position(map.getCameraPosition().target)
                .draggable(true));
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                selectedLocation.setPosition(latLng);
            }
        });
    }

    private CameraUpdate getCameraTarget() {
        Address address = getIntent().getParcelableExtra(NewNoteActivity.EXTRA_LOCATION);
        LatLng cameraTarget;
        float zoom = GoogleMapsZoom.CITY_LEVEL;

        if (address == null) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Location currentLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            cameraTarget = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            zoom = GoogleMapsZoom.NEIGHBORHOOD_LEVEL;
        } else {
            cameraTarget = new LatLng(address.getLatitude(), address.getLongitude());
        }

        return CameraUpdateFactory.newLatLngZoom(cameraTarget, zoom);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_select) {
            select();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void select() {
        LatLng position = selectedLocation.getPosition();
        Address address = new LocationHandler(this).getAddress(position.latitude, position.longitude);

        Intent intent = new Intent();
        intent.putExtra(NewNoteActivity.EXTRA_LOCATION, address);
        setResult(RESULT_OK, intent);
        finish();
    }
}
