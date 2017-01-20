package com.partyappfia.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
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
import com.partyappfia.R;
import com.partyappfia.adapters.PartyUserListAdapter;
import com.partyappfia.application.FiaApplication;
import com.partyappfia.config.Constants;
import com.partyappfia.dialogs.MultiSelectUserDialog;
import com.partyappfia.fragments.SublimePickerFragment;
import com.partyappfia.model.InvitedItem;
import com.partyappfia.model.PartyItem;
import com.partyappfia.model.UserInfo;
import com.partyappfia.pushnotifications.QBPushHelper;
import com.partyappfia.utils.BitmapHelper;
import com.partyappfia.utils.BusyHelper;
import com.partyappfia.utils.CircleImageView;
import com.partyappfia.utils.KeyboardHelper;
import com.partyappfia.utils.ListViewHelper;
import com.partyappfia.utils.MapHelper;
import com.partyappfia.utils.PlaceAutocompleteAdapter;
import com.partyappfia.utils.TimeHelper;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class PartyDetailActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String  INTENT_PARTY_DETAIL_TYPE    = "intent_party_detail_type";
    public static final String  INTENT_PARTY_DETAIL_RESUT   = "intent_party_detail_result";

    private TextView                tvTitle;
    private View                    lvDone;
    private TextView                tvDone;

    private ScrollView              svPane;

    private View                    lvDisplayPane;
    private TextView                tvPartyTitle;
    private TextView                tvComment;
    private TextView                tvTime;
    private TextView                tvAddress;

    private View                    lvInputPane;
    private EditText                evPartyTitle;
    private EditText                evComment;
    private AutoCompleteTextView    evAddress;
    private View                    lvExpireTime;
    private TextView                tvExpireTime;

    private TextView                tvOwnerLabel;
    private CircleImageView         ivAvatar;
    private TextView                tvUsername;
    private TextView                tvPhone;
    private RadioGroup              rgRating;
    private ImageView               ivAddUser;

    ///////////////////////////////////////////////////
    // ListView
    private ListView                mListView;
    private PartyUserListAdapter    mAdapter;
    private TextView                tvEmptyMessage;

    ///////////////////////////////////////////////////
    // Map
    private MapView                 mapView;
    private GoogleMap               map;
    private LatLng                  startLatLng, destLatLng;

    ///////////////////////////////////////////////////
    // Autocomplete
    private static final String TAG = PartyDetailActivity.class.getSimpleName();
    private static GoogleApiClient mGoogleApiClient = null;
    private PlaceAutocompleteAdapter mAutoCompleteAdapter;
    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));

    ////////////////////////////////////////////////////
    // Global
    private static PartyItem g_partyItem = null;
    public static void setPartyItem(PartyItem partyItem) {
        PartyDetailActivity.g_partyItem = partyItem;
    }

    private int partyDetailType = Constants.PARTY_DETAIL_TYPE_DETAIL;
    private boolean bMyParty = true;
    private ArrayList<UserInfo> arrAddedUsers   = new ArrayList<UserInfo>();
    private ArrayList<UserInfo> arrDeletedUsers = new ArrayList<UserInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_detail);

        partyDetailType     = getIntent().getIntExtra(INTENT_PARTY_DETAIL_TYPE, Constants.PARTY_DETAIL_TYPE_DETAIL);

        tvTitle             = (TextView) findViewById(R.id.tv_title);
        lvDone              = findViewById(R.id.lv_done);
        tvDone              = (TextView) findViewById(R.id.tv_done);

        svPane              = (ScrollView) findViewById(R.id.sv_pane);

        lvDisplayPane       = findViewById(R.id.lv_display_pane);
        tvPartyTitle        = (TextView) findViewById(R.id.tv_party_title);
        tvComment           = (TextView) findViewById(R.id.tv_comment);
        tvTime              = (TextView) findViewById(R.id.tv_time);
        tvAddress           = (TextView) findViewById(R.id.tv_address);

        lvInputPane         = findViewById(R.id.lv_input_pane);
        evPartyTitle        = (EditText) findViewById(R.id.ev_party_title);
        evComment           = (EditText) findViewById(R.id.ev_comment);
        evAddress           = (AutoCompleteTextView) findViewById(R.id.ev_address);
        lvExpireTime        = findViewById(R.id.lv_expire_time);
        tvExpireTime        = (TextView) findViewById(R.id.tv_expire_time);

        tvOwnerLabel        = (TextView) findViewById(R.id.tv_owner_label);
        ivAvatar            = (CircleImageView) findViewById(R.id.iv_avatar);
        tvUsername          = (TextView) findViewById(R.id.tv_username);
        tvPhone             = (TextView) findViewById(R.id.tv_phone);
        rgRating            = (RadioGroup) findViewById(R.id.rg_rating);

        ivAddUser           = (ImageView) findViewById(R.id.iv_add_user);

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        findViewById(R.id.iv_template).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                svPane.requestDisallowInterceptTouchEvent(true);
                mapView.dispatchTouchEvent(event);
                return true;
            }
        });

        mAdapter = new PartyUserListAdapter(this, new ArrayList<InvitedItem>());
        mListView = (ListView) findViewById(R.id.lst_users);
        mListView.setAdapter(mAdapter);

        tvEmptyMessage = (TextView) findViewById(R.id.tv_empty_message);

        if (mGoogleApiClient == null) {
            try {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .enableAutoManage(this, 0 /* clientId */, this)
                        .addApiIfAvailable(Places.GEO_DATA_API)
                        .addOnConnectionFailedListener(this)
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        evAddress.setOnItemClickListener(mAutocompleteClickListener);

        mAutoCompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_SYDNEY, null);
        evAddress.setAdapter(mAutoCompleteAdapter);

        lvExpireTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectTimeButtonClicked();
            }
        });

        ivAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddUserButtonClicked();
            }
        });

        findViewById(R.id.lv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        lvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDoneButtonClicked();
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initialize();
            }
        }, 50);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void onBackPressed() {
        if (g_partyItem != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(INTENT_PARTY_DETAIL_RESUT, g_partyItem.getId());
            setResult(RESULT_OK, resultIntent);
        }

        g_partyItem = null;
        finish();
        overridePendingTransition(R.anim.move_in_fade, R.anim.move_out_fade);
    }

    private void initialize() {
        UserInfo ownerUser = (g_partyItem != null ? g_partyItem.getOwnerUser() : FiaApplication.getMyUserInfo());
        bMyParty = ownerUser.isEqual(FiaApplication.getMyUserInfo());

        tvOwnerLabel.setText(bMyParty ? "Party owner(me)" : "Party owner");
        BitmapHelper.drawImageUrlFromPicasso(this, ownerUser.getPhotoUrl(), R.drawable.avatar_mark, ivAvatar);
        tvUsername.setText(ownerUser.getUsername());
        tvPhone.setText(ownerUser.getPhone());
        switch (ownerUser.getRating()) {
            case Constants.LEVEL_LOW:
                rgRating.check(R.id.rdo_low);
                break;
            case Constants.LEVEL_MIDDLE:
                rgRating.check(R.id.rdo_middle);
                break;
            case Constants.LEVEL_HIGH:
                rgRating.check(R.id.rdo_high);
                break;
            default:
                rgRating.clearCheck();
                break;
        }

        if (g_partyItem != null) {
            mAdapter.clear();
            for (InvitedItem item : g_partyItem.getInvitedItems()) {
                mAdapter.add(item);
            }
        }

        refreshControls();
        initMapView();
    }

    private void refreshControls() {
        switch (partyDetailType) {
            case Constants.PARTY_DETAIL_TYPE_DETAIL:
                tvTitle.setText("Party Detail");
                break;

            case Constants.PARTY_DETAIL_TYPE_CREATE:
                tvTitle.setText("Create Party");
                break;

            case Constants.PARTY_DETAIL_TYPE_EDIT:
                tvTitle.setText("Edit Party");
                break;
        }

        tvDone.setText(partyDetailType == Constants.PARTY_DETAIL_TYPE_DETAIL && bMyParty ? "EDIT" : "DONE");
        lvDone.setVisibility(partyDetailType == Constants.PARTY_DETAIL_TYPE_DETAIL && !bMyParty ? View.GONE : View.VISIBLE);

        if (g_partyItem != null) {
            tvPartyTitle.setText(g_partyItem.getTitle());
            tvComment.setText(g_partyItem.getComment());
            tvAddress.setText(g_partyItem.getAddress());
            tvTime.setVisibility(g_partyItem.getExpireTimeByLocal().isEmpty() ? View.GONE : View.VISIBLE);
            tvTime.setText(g_partyItem.getExpireTimeByLocal());

            evPartyTitle.setText(g_partyItem.getTitle());
            evComment.setText(g_partyItem.getComment());
            evAddress.setText(g_partyItem.getAddress());
            tvExpireTime.setText(g_partyItem.getExpireTimeByLocal());
        }

        tvAddress.setVisibility(tvAddress.getText().toString().isEmpty() ? View.GONE : View.VISIBLE);

        lvDisplayPane.setVisibility(partyDetailType == Constants.PARTY_DETAIL_TYPE_DETAIL ? View.VISIBLE : View.GONE);
        lvInputPane.setVisibility(partyDetailType == Constants.PARTY_DETAIL_TYPE_DETAIL ? View.GONE : View.VISIBLE);

        ivAddUser.setVisibility(partyDetailType == Constants.PARTY_DETAIL_TYPE_DETAIL ? View.GONE : View.VISIBLE);

        mAdapter.setLockSwipe(!bMyParty || partyDetailType == Constants.PARTY_DETAIL_TYPE_DETAIL);
        mAdapter.notifyDataSetChanged();
        ListViewHelper.setListViewHeightBasedOnItems(mListView);
        mListView.setVisibility(mAdapter.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmptyMessage.setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void initMapView() {
        try {
            // Gets to GoogleMap from the MapView and does initialization stuff
            map = mapView.getMap();
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.setMyLocationEnabled(true);

            // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
            MapsInitializer.initialize(this);

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
                            if (g_partyItem != null) {
                                destLatLng = g_partyItem.getLatLng();
                            }

                            if (bFirstChangedMyLocation)
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 12.0f));

                            displayMapMarkers(bFirstChangedMyLocation);
                        }
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

        if (destLatLng != null) {
            map.clear();

            int mapMarkerResId = bMyParty ? R.drawable.map_marker_default : R.drawable.map_marker_invited;

            arrPoints.add(destLatLng);
            String title = (tvTitle.getText().toString().isEmpty() ? "No Title" : tvTitle.getText().toString());
            String snippet = tvAddress.getText().toString() + "\n" + MapHelper.getDistanceByString(startLatLng, destLatLng);
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(destLatLng)
                    .title(title)
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.fromResource(mapMarkerResId)));
            marker.showInfoWindow();

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
    }

    public boolean isMyParty() {
        return bMyParty;
    }

    SublimePickerFragment.Callback mFragmentCallback = new SublimePickerFragment.Callback() {
        @Override
        public void onCancelled() {

        }

        @Override
        public void onDateTimeRecurrenceSet(SelectedDate selectedDate,
                                            int hourOfDay, int minute,
                                            SublimeRecurrencePicker.RecurrenceOption recurrenceOption,
                                            String recurrenceRule) {

            Calendar cal = selectedDate.getFirstDate();
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            cal.set(Calendar.MINUTE, minute);
            Date date = cal.getTime();

            SimpleDateFormat localFormat = new SimpleDateFormat(Constants.DATE_FORMAT_FOR_DISPLAY_PARTY);
            localFormat.setTimeZone(TimeZone.getDefault());
            String formattedDate = localFormat.format(date);
            tvExpireTime.setText(formattedDate);
            tvTime.setText(formattedDate);
        }
    };

    private void onSelectTimeButtonClicked() {
        SublimePickerFragment pickerFrag = new SublimePickerFragment();
        pickerFrag.setCallback(mFragmentCallback);

        // Options
        SublimeOptions options = new SublimeOptions();
        options.setDisplayOptions(SublimeOptions.ACTIVATE_DATE_PICKER | SublimeOptions.ACTIVATE_TIME_PICKER);
        Pair<Boolean, SublimeOptions> optionsPair = new Pair<>(Boolean.TRUE, options);

        // Valid options
        Bundle bundle = new Bundle();
        bundle.putParcelable("SUBLIME_OPTIONS", optionsPair.second);
        pickerFrag.setArguments(bundle);

        pickerFrag.show(getSupportFragmentManager(), "SUBLIME_PICKER");
    }

    private void onAddUserButtonClicked() {
        new MultiSelectUserDialog(this, true, new MultiSelectUserDialog.onOKButtonListener() {
            @Override
            public void onOKButton(ArrayList<UserInfo> selectedUsers) {
                if (selectedUsers == null)
                    return;

                ArrayList<InvitedItem> arrInvitingItems = mAdapter.getList();
                for (UserInfo userInfo : selectedUsers) {
                    if (arrInvitingItems != null) {
                        boolean bExist = false;
                        for (InvitedItem invitingItem : arrInvitingItems) {
                            if (invitingItem.getUser().isEqual(userInfo)) {
                                bExist = true;
                                break;
                            }
                        }
                        if (bExist)
                            continue;
                    }

                    InvitedItem invitedItem = new InvitedItem(userInfo, Constants.PARTY_STATE_INVITED, "");
                    mAdapter.add(invitedItem);

                    boolean bAddable = true;
                    if (g_partyItem != null) {
                        for (InvitedItem item : g_partyItem.getInvitedItems()) {
                            if (invitedItem.getUser().isEqual(item.getUser())) {
                                bAddable = false;
                                break;
                            }
                        }
                    }
                    if (bAddable) {
                        for (UserInfo user : arrDeletedUsers) {
                            if (user.isEqual(invitedItem.getUser())) {
                                arrDeletedUsers.remove(user);
                                break;
                            }
                        }
                        arrAddedUsers.add(invitedItem.getUser());
                    }
                }
                refreshControls();
            }
        }).show();
    }

    public void onDeleteUserButtonClicked(InvitedItem invitedItem) {
        if (!bMyParty || partyDetailType == Constants.PARTY_DETAIL_TYPE_DETAIL)
            return;

        boolean bDeleted = false;
        for (UserInfo userInfo : arrAddedUsers) {
            if (userInfo.isEqual(invitedItem.getUser())) {
                arrAddedUsers.remove(userInfo);
                bDeleted = true;
                break;
            }
        }
        if (!bDeleted) {
            arrDeletedUsers.add(invitedItem.getUser());
        }

        mAdapter.remove(invitedItem);
        refreshControls();
    }

    private void onDoneButtonClicked() {
        KeyboardHelper.hideKeyboard(this);

        String title        = evPartyTitle.getText().toString();
        String comment      = evComment.getText().toString();
        String address      = evAddress.getText().toString();
        String expireTime   = tvExpireTime.getText().toString();

        if (partyDetailType != Constants.PARTY_DETAIL_TYPE_DETAIL) {

            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter title", Toast.LENGTH_LONG).show();
                return;
            }

            if (comment.isEmpty()) {
                Toast.makeText(this, "Please enter comment", Toast.LENGTH_LONG).show();
                return;
            }

            if (destLatLng == null) {
                Toast.makeText(this, "Party place is not confirmed. Map cannot show the place of this party", Toast.LENGTH_LONG).show();
            }

            if (g_partyItem == null)
                g_partyItem = new PartyItem();

            g_partyItem.setTitle(title);
            g_partyItem.setComment(comment);
            g_partyItem.setAddress(address);
            g_partyItem.setLatLng(destLatLng);
            g_partyItem.setExpireTimeFromLocal(expireTime);
            g_partyItem.setOwnerUser(FiaApplication.getMyUserInfo());
        }

        ArrayList<InvitedItem> arrInvitingItems = mAdapter.getList();
        if (arrInvitingItems != null) {
            for (InvitedItem item : arrInvitingItems) {
                boolean bExist = false;
                if (g_partyItem.getInvitedItems() != null) {
                    for (InvitedItem savedItem : g_partyItem.getInvitedItems()) {
                        if (savedItem.getUser().isEqual(item.getUser())) {
                            bExist = true;
                            break;
                        }
                    }
                }
                if (!bExist)
                    g_partyItem.getInvitedItems().add(item);
            }
        }

        if (partyDetailType == Constants.PARTY_DETAIL_TYPE_DETAIL) {
            if (bMyParty) {
                partyDetailType = Constants.PARTY_DETAIL_TYPE_EDIT;
                refreshControls();
            }
            return;
        }

        QBCustomObject object = new QBCustomObject();
        object.setClassName("Party");
        if (partyDetailType == Constants.PARTY_DETAIL_TYPE_EDIT)
            object.setCustomObjectId(g_partyItem.getId());

        object.putString("title",               g_partyItem.getTitle());
        object.putString("comment",             g_partyItem.getComment());
        object.putString("address",             g_partyItem.getAddress());
        object.putString("latLng",              g_partyItem.getLatLngString());
        object.putString("expireTime",          g_partyItem.getExpireTime());
        object.putString("ownerUserId",         g_partyItem.getOwnerUser().getId());
        object.putArray("invitedUserIds",       g_partyItem.getInvitedUserIds());
        object.putArray("invitedStates",        g_partyItem.getInvitedStates());
        object.putArray("invitedCheckTimes",    g_partyItem.getInvitedCheckTimes());

        if (partyDetailType == Constants.PARTY_DETAIL_TYPE_CREATE) {
            BusyHelper.show(this, "Creating party...");
            QBCustomObjects.createObject(object, new QBEntityCallback<QBCustomObject>() {
                @Override
                public void onSuccess(final QBCustomObject createdObject, Bundle params) {
                    BusyHelper.hide();

                    g_partyItem.setId(createdObject.getCustomObjectId());
                    FiaApplication.getMyParties().add(g_partyItem);
                    processNotifications();
                    onBackPressed();
                }

                @Override
                public void onError(QBResponseException errors) {
                    BusyHelper.hide();
                    Log.d("Creating party - ", errors.getMessage());
                    Toast.makeText(PartyDetailActivity.this, R.string.connection_failed, Toast.LENGTH_LONG).show();
                }
            });
        } else if (partyDetailType == Constants.PARTY_DETAIL_TYPE_EDIT) {
            BusyHelper.show(this, "Updating party...");
            QBCustomObjects.updateObject(object, new QBEntityCallback<QBCustomObject>() {
                @Override
                public void onSuccess(final QBCustomObject createdObject, Bundle params) {
                    BusyHelper.hide();

                    for (PartyItem item : FiaApplication.getMyParties()) {
                        if (item.isEqual(g_partyItem)) {
                            item.clone(g_partyItem);
                            break;
                        }
                    }

                    processNotifications();

                    partyDetailType = Constants.PARTY_DETAIL_TYPE_DETAIL;
                    refreshControls();
                }

                @Override
                public void onError(QBResponseException errors) {
                    BusyHelper.hide();
                    Log.d("Updating party - ", errors.getMessage());
                    Toast.makeText(PartyDetailActivity.this, R.string.connection_failed, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void processNotifications() {
        ArrayList<QBCustomObject> objects = new ArrayList<QBCustomObject>();

        for (UserInfo userInfo : arrAddedUsers) {
            QBCustomObject object = new QBCustomObject();
            object.setClassName("Notification");

            String message = String.format("Welcome! %s invited you to the party - \"%s\". %s is waiting for your answer with accepting or rejecting.", g_partyItem.getOwnerUser().getUsername(), g_partyItem.getTitle(), g_partyItem.getOwnerUser().getUsername());
            object.putInteger("notificationType",   Constants.NOTIFICATION_TYPE_PARTY_INVITED);
            object.putString("message",             message);
            object.putString("senderUserId",        g_partyItem.getOwnerUser().getId());
            object.putString("receiverUserId",      userInfo.getId());
            object.putString("time",                TimeHelper.getTimeStamp());
            object.putString("partyId",             g_partyItem.getId());

            objects.add(object);
            QBPushHelper.sendPushNotification(userInfo, message);
        }

        for (UserInfo userInfo : arrDeletedUsers) {
            QBCustomObject object = new QBCustomObject();
            object.setClassName("Notification");

            String message = String.format("Sorry, %s rejected you in the party - \"%s\"", g_partyItem.getOwnerUser().getUsername(), g_partyItem.getTitle());
            object.putInteger("notificationType",   Constants.NOTIFICATION_TYPE_PARTY_REJECTED);
            object.putString("message",             message);
            object.putString("senderUserId",        g_partyItem.getOwnerUser().getId());
            object.putString("receiverUserId",      userInfo.getId());
            object.putString("time",                TimeHelper.getTimeStamp());
            object.putString("partyId",             g_partyItem.getId());

            objects.add(object);
            QBPushHelper.sendPushNotification(userInfo, message);
        }

        if (!objects.isEmpty())
            QBCustomObjects.createObjects(objects, null);
    }

    public void onSelectedUser(UserInfo user) {
        ProfileActivity.setUserInfo(user);
        startActivity(new Intent(this, ProfileActivity.class));
        overridePendingTransition(R.anim.move_in_fade, R.anim.move_out_fade);
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */

            try {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(evAddress.getWindowToken(), 0);

                final AutocompletePrediction item = mAutoCompleteAdapter.getItem(position);
                final String placeId = item.getPlaceId();
                final CharSequence primaryText = item.getPrimaryText(null);

                Log.i(TAG, "Autocomplete item selected: " + primaryText);

                /*
                 Issue a request to the Places Geo Data API to retrieve a Place object with additional
                 details about the place.
                  */
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

                Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }

            try {
                // Get the Place object from the buffer.
                final Place place = places.get(0);
                Log.i(TAG, "Place details received: " + place.getName());

                String address = place.getAddress().toString();
                destLatLng = place.getLatLng();
                tvAddress.setVisibility(address.isEmpty() ? View.GONE : View.VISIBLE);
                evAddress.setAdapter(null);
                evAddress.setText(address);
                evAddress.setAdapter(mAutoCompleteAdapter);
                tvAddress.setText(address);
                displayMapMarkers(false);

                places.release();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this, "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
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

        Bundle gmapstate = new Bundle();
        mapView.onSaveInstanceState(gmapstate);
        outState.putParcelable("gmap_state", gmapstate);
//        mapView.onSaveInstanceState(outState);

        // Only if you need to restore open/close state when
        // the orientation is changed
        if (mAdapter != null) {
            mAdapter.saveStates(outState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Only if you need to restore open/close state when
        // the orientation is changed
        if (mAdapter != null) {
            mAdapter.restoreStates(savedInstanceState);
        }
    }

}
