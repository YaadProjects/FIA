package com.partyappfia.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.partyappfia.R;
import com.partyappfia.application.FiaApplication;
import com.partyappfia.config.Constants;
import com.partyappfia.core.ChatService;
import com.partyappfia.fragments.BaseFragment;
import com.partyappfia.fragments.HomeFragment;
import com.partyappfia.fragments.MapFragment;
import com.partyappfia.fragments.MessageFragment;
import com.partyappfia.fragments.PartyFragment;
import com.partyappfia.pushnotifications.PlayServicesHelper;
import com.partyappfia.utils.BitmapHelper;
import com.partyappfia.utils.BusyHelper;
import com.partyappfia.utils.CircleImageView;
import com.partyappfia.utils.KeyboardHelper;
import com.partyappfia.utils.PrefManager;
import com.quickblox.users.QBUsers;


public class HomeActivity extends BaseActivity {

    public static final String INTENT_FROM_PUSH = "intent_from_push";

    public static final int REQUEST_PROFILE     = 200;

    public static final int GOTOPAGE_HOME       = 0;
    public static final int GOTOPAGE_PARTY      = 1;
    public static final int GOTOPAGE_MAP        = 2;
    public static final int GOTOPAGE_MESSAGE    = 3;
    public static final int PAGE_COUNT          = 4;

    private static final int[] TAB_IDS = {
            R.id.tab_home,
            R.id.tab_party,
            R.id.tab_map,
            R.id.tab_message
    };

    private static final int[] TAB_IV_IDS = {
            R.id.iv_home,
            R.id.iv_party,
            R.id.iv_map,
            R.id.iv_message
    };

    private static final int[] TAB_TV_IDS = {
            R.id.tv_home,
            R.id.tv_party,
            R.id.tv_map,
            R.id.tv_message
    };

    private static final int[] TAB_ICON_RESIDS = {
            R.drawable.home,
            R.drawable.party,
            R.drawable.map,
            R.drawable.message
    };

    private static final int[] TAB_ICON_SELECTED_RESIDS = {
            R.drawable.home_selected,
            R.drawable.party_selected,
            R.drawable.map_selected,
            R.drawable.message_selected
    };

    private DrawerLayout    dlMain;
    private NavigationView  navView;

    private ImageView       ivAvatar;
    private TextView        tvUsername;

    private View            lvToggleDrawer;

    private int mCurrentPageIndex = -1;
    private int mPreviousPageIndex = GOTOPAGE_HOME;
    private BaseFragment mCurrentFragment = null;
    private boolean doubleBackToExitPressedOnce = false;

    private PlayServicesHelper playServicesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        doGCMRegister();

        dlMain      = (DrawerLayout) findViewById(R.id.drawer_layout);
        navView     = (NavigationView) findViewById(R.id.nav_view);

        ivAvatar    = (CircleImageView) navView.findViewById(R.id.iv_avatar);
        tvUsername  = (TextView) navView.findViewById(R.id.tv_username);

        lvToggleDrawer = findViewById(R.id.lv_toggle_drawer);

        defineButtonClickEnvents();

        initialize();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        showNotificationActrivityFromPush();
    }

    private void showNotificationActrivityFromPush() {
        if (PrefManager.readPrefBoolean(Constants.PREF_FROM_PUSH)) {
            PrefManager.savePrefBoolean(Constants.PREF_FROM_PUSH, false);
            startActivity(new Intent(HomeActivity.this, NotificationActivity.class));
            overridePendingTransition(R.anim.move_in_fade, R.anim.move_out_fade);
        }
    }

    public void doGCMRegister() {

        playServicesHelper = new PlayServicesHelper(this);

//        Intent gcmRegisterIntent = new Intent(this, GcmRegisterIntentService.class);
//        startService(gcmRegisterIntent);
    }

    private void initialize() {
        BitmapHelper.drawImageUrlFromPicasso(this, FiaApplication.getMyUserInfo().getPhotoUrl(), R.drawable.avatar_mark, ivAvatar);
        tvUsername.setText(FiaApplication.getMyUserInfo().getUsername());
        gotoPage(GOTOPAGE_HOME);
    }

    private void defineButtonClickEnvents() {
        lvToggleDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dlMain.isDrawerOpen(GravityCompat.START))
                    dlMain.closeDrawer(GravityCompat.START);
                else
                    dlMain.openDrawer(GravityCompat.START);
            }
        });

        navView.findViewById(R.id.nav_notification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlMain.closeDrawer(GravityCompat.START);
                startActivity(new Intent(HomeActivity.this, NotificationActivity.class));
                overridePendingTransition(R.anim.move_in_fade, R.anim.move_out_fade);
            }
        });

        navView.findViewById(R.id.nav_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlMain.closeDrawer(GravityCompat.START);
                ProfileActivity.setUserInfo(FiaApplication.getMyUserInfo());
                startActivityForResult(new Intent(HomeActivity.this, ProfileActivity.class), REQUEST_PROFILE);
                overridePendingTransition(R.anim.move_in_fade, R.anim.move_out_fade);
            }
        });

        navView.findViewById(R.id.nav_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.logout_message)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                signOut();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        for (int i = 0; i < PAGE_COUNT; i++) {
            final int finalI = i;
            findViewById(TAB_IDS[i]).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gotoPage(finalI);
                }
            });
        }
    }

    private void signOut() {
        BusyHelper.show(this, "Logging out...");
        ChatService.getInstance().logout(new ChatService.onSuccessListener() {
            @Override
            public void onSuccess(boolean bSuccess) {
                if (bSuccess != true)
                    return;

                try {
                    QBUsers.signOut();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                BusyHelper.hide();
                PrefManager.savePrefBoolean(Constants.PREF_AUTOLOGIN, false);
                PrefManager.savePrefString(Constants.PREF_USERNAME, "");
                PrefManager.savePrefString(Constants.PREF_PASSWORD, "");
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                HomeActivity.this.overridePendingTransition(R.anim.move_in_right, R.anim.move_out_right);
                HomeActivity.this.finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (dlMain.isDrawerOpen(GravityCompat.START)) {
            dlMain.closeDrawer(GravityCompat.START);
            return;
        }

        if (doubleBackToExitPressedOnce) {
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.quit_message, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public void gotoPage(int nPageIndex) {
        if (mCurrentPageIndex == nPageIndex)
            return;

        mPreviousPageIndex = mCurrentPageIndex;
        mCurrentPageIndex = nPageIndex;
        mCurrentFragment = createFragment(nPageIndex);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mPreviousPageIndex >= 0) {
//            if (nPrevPageIndex >= nPageIndex)
//                transaction.setCustomAnimations(R.anim.move_in_right, R.anim.move_out_right);
//            else
                transaction.setCustomAnimations(R.anim.move_in_fade, R.anim.move_out_fade);
        }

        transaction.replace(R.id.frag_Container, mCurrentFragment);
        transaction.commit();
        getSupportFragmentManager().executePendingTransactions();

        refreshControls();
        KeyboardHelper.hideKeyboard(this);
    }

    private BaseFragment createFragment(int nPageIndex) {
        BaseFragment newFragment = null;

        switch (nPageIndex) {
            case GOTOPAGE_HOME:
                newFragment = new HomeFragment();
                break;
            case GOTOPAGE_PARTY:
                newFragment = new PartyFragment();
                break;
            case GOTOPAGE_MAP:
                newFragment = new MapFragment();
                break;
            case GOTOPAGE_MESSAGE:
                newFragment = new MessageFragment();
                break;
        }

        return newFragment;
    }

    public void refreshControls() {
        if (mCurrentPageIndex < 0 || mCurrentPageIndex > PAGE_COUNT)
            return;

        for (int i = 0; i < PAGE_COUNT; i++) {
            ImageView ivNavItem = (ImageView) findViewById(TAB_IV_IDS[i]);
            TextView tvNavItem  = (TextView) findViewById(TAB_TV_IDS[i]);
            ivNavItem.setImageResource(mCurrentPageIndex == i ? TAB_ICON_SELECTED_RESIDS[i] : TAB_ICON_RESIDS[i]);
            tvNavItem.setTextColor(mCurrentPageIndex == i ? getResources().getColor(R.color.colorOrange) : Color.WHITE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PROFILE) {
            BitmapHelper.drawImageUrlFromPicasso(this, FiaApplication.getMyUserInfo().getPhotoUrl(), R.drawable.avatar_mark, ivAvatar);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        showNotificationActrivityFromPush();
        playServicesHelper.checkPlayServices();
    }
}
