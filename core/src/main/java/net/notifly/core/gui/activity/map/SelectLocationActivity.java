package net.notifly.core.gui.activity.map;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;

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

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.SystemService;

@EActivity(R.layout.activity_select_location)
@OptionsMenu(R.menu.select_location)
public class SelectLocationActivity extends Activity {
    GoogleMap map;
    Marker selectedLocation;

    @FragmentById(R.id.map)
    MapFragment mapFragment;
    @SystemService
    LocationManager locationManager;
    @Bean
    LocationHandler LocationHandler;
    @Extra(NewNoteActivity.EXTRA_LOCATION)
    Address address;

    @AfterViews
    void setMap() {
        CameraUpdate cameraTarget = getCameraTarget();
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
        LatLng cameraTarget;
        float zoom = GoogleMapsZoom.CITY_LEVEL;

        if (address == null) {
            Location currentLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            cameraTarget = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            zoom = GoogleMapsZoom.NEIGHBORHOOD_LEVEL;
        } else {
            cameraTarget = new LatLng(address.getLatitude(), address.getLongitude());
        }

        return CameraUpdateFactory.newLatLngZoom(cameraTarget, zoom);
    }

    @OptionsItem(R.id.action_select)
    void select() {
        LatLng position = selectedLocation.getPosition();
        Address address = LocationHandler.getAddress(position.latitude, position.longitude);

        Intent intent = new Intent();
        intent.putExtra(NewNoteActivity.EXTRA_LOCATION, address);
        setResult(RESULT_OK, intent);
        finish();
    }
}
