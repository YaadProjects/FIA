package com.partyappfia.fragments;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.partyappfia.R;
import com.partyappfia.activities.PartyDetailActivity;
import com.partyappfia.application.FiaApplication;
import com.partyappfia.config.Constants;
import com.partyappfia.model.PartyItem;
import com.partyappfia.utils.MapHelper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Great Summit on 1/14/2016.
 */
public class MapFragment extends BaseFragment {

    private static final int REQUEST_PARTY = 600;

    private MapView     mapView;
    private GoogleMap   map;
    private LatLng      startLatLng;

    private View        lvHintPane;

    private Map<Marker, PartyItem> mapMarkerData = new HashMap<Marker, PartyItem>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lvHintPane = view.findViewById(R.id.lv_hint_pane);
        mapView = (MapView) view.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        initialize();
    }

    public void initialize() {
        initMapView();
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public void initMapView() {
        try {
            // Gets to GoogleMap from the MapView and does initialization stuff
            map = mapView.getMap();
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.setMyLocationEnabled(true);

            // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
            MapsInitializer.initialize(getHomeActivity());

            // Check if we were successful in obtaining the map.
            if (map != null) {
                map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

                    @Override
                    public void onMyLocationChange(Location location) {
                        // TODO Auto-generated method stub
                        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

                        boolean bFirstChangedMyLocation = (startLatLng == null);
                        if (startLatLng == null || MapHelper.getDistanceByMeter(startLatLng, loc) > 100) {
                            startLatLng = loc;

                            if (bFirstChangedMyLocation) {
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 12.0f));

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            lvHintPane.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.move_out_fade));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, 10000);
                            }

                            displayMapMarkers(bFirstChangedMyLocation);
                        }
                    }
                });

//                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//                    @Override
//                    public boolean onMarkerClick(Marker marker) {
//                        gotoPartyDetail(marker);
//                        return false;
//                    }
//                });

                map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        gotoPartyDetail(marker);
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayMapMarkers(boolean bChangeCamera) {
        ArrayList<LatLng> arrPoints = new ArrayList<LatLng>();
        arrPoints.add(startLatLng);

        map.clear();
        mapMarkerData.clear();

        for (PartyItem partyItem : FiaApplication.getMyParties()) {
            if (partyItem.getLatLng() == null)
                continue;
            arrPoints.add(partyItem.getLatLng());
            addMarker(partyItem, true);
        }

        for (PartyItem partyItem : FiaApplication.getAcceptedParties()) {
            if (partyItem.getLatLng() == null)
                continue;
            arrPoints.add(partyItem.getLatLng());
            addMarker(partyItem, false);
        }

        if (bChangeCamera) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng latLng : arrPoints)
                builder.include(latLng);
            LatLngBounds bounds = builder.build();

            int padding = 100; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

            map.moveCamera(cu);
            map.animateCamera(cu);
        }
    }

    private void addMarker(PartyItem partyItem, boolean bMyParty) {
        LatLng destLatLng = partyItem.getLatLng();
        Marker marker = map.addMarker(new MarkerOptions()
                .position(destLatLng)
                .title(String.format("%s(owner - %s)", partyItem.getTitle(), partyItem.getOwnerUser().getUsername()))
                .snippet(partyItem.getAddress() + "\n" + MapHelper.getDistanceByString(startLatLng, destLatLng))
                .icon(BitmapDescriptorFactory.fromResource(bMyParty ? R.drawable.map_marker_default : R.drawable.map_marker_invited)));
        marker.showInfoWindow();
        mapMarkerData.put(marker, partyItem);
    }

    private void gotoPartyDetail(Marker marker) {
//        if (!marker.isInfoWindowShown())
//            return;

        PartyItem partyItem = mapMarkerData.get(marker);
        if (partyItem == null)
            return;

        PartyDetailActivity.setPartyItem(partyItem);
        Intent intent = new Intent(getHomeActivity(), PartyDetailActivity.class);
        intent.putExtra(PartyDetailActivity.INTENT_PARTY_DETAIL_TYPE, Constants.PARTY_DETAIL_TYPE_DETAIL);
        startActivityForResult(intent, REQUEST_PARTY);
        getHomeActivity().overridePendingTransition(R.anim.move_in_fade, R.anim.move_out_fade);
    }
}
