package com.partyappfia.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.partyappfia.R;
import com.partyappfia.application.FiaApplication;
import com.partyappfia.config.Constants;
import com.partyappfia.dialogs.CameraKindDialog;
import com.partyappfia.dialogs.InputDialog;
import com.partyappfia.dialogs.RatingDialog;
import com.partyappfia.model.UserInfo;
import com.partyappfia.utils.BitmapHelper;
import com.partyappfia.utils.BusyHelper;
import com.partyappfia.utils.CircleImageView;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;

import java.io.File;


public class ProfileActivity extends AppCompatActivity {

    private TextView        tvTitle;
    private CircleImageView ivAvatar;
    private ImageView       ivAvatarBlurred;
    private TextView        tvUsername;
    private TextView        tvPhone;
    private RadioGroup      rgRating;
    private TextView        tvRating;
    private TextView        tvCreatedParties;
    private TextView        tvInvitedParties;
    private View            lvChat;
    private View            lvEditPhone;
    private View            lvCallPhone;
    private View            lvEditRating;

    private boolean         bMyProfile = true;

    private Uri             mFileUri = null;
    private Uri             mFileCropUri = null;

    private static UserInfo g_userInfo = null;

    public static void setUserInfo(UserInfo userInfo) {
        ProfileActivity.g_userInfo = userInfo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        bMyProfile          = (g_userInfo.isEqual(FiaApplication.getMyUserInfo()));

        tvTitle             = (TextView) findViewById(R.id.tv_title);
        ivAvatar            = (CircleImageView) findViewById(R.id.iv_avatar);
        ivAvatarBlurred     = (ImageView) findViewById(R.id.iv_avatar_blurred);
        tvUsername          = (TextView) findViewById(R.id.tv_username);
        tvPhone             = (TextView) findViewById(R.id.tv_phone);
        rgRating            = (RadioGroup) findViewById(R.id.rg_rating);
        tvRating            = (TextView) findViewById(R.id.tv_rating);
        tvCreatedParties    = (TextView) findViewById(R.id.tv_created_parties);
        tvInvitedParties    =(TextView) findViewById(R.id.tv_invited_parties);
        lvChat              = findViewById(R.id.lv_chat);
        lvEditPhone         = findViewById(R.id.lv_edit_phone);
        lvCallPhone         = findViewById(R.id.lv_call_phone);
        lvEditRating        = findViewById(R.id.lv_edit_rating);

        findViewById(R.id.lv_chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatActivity.setPartnerUser(g_userInfo);
                startActivity(new Intent(ProfileActivity.this, ChatActivity.class));
                overridePendingTransition(R.anim.move_in_fade, R.anim.move_out_fade);
                finish();
            }
        });

        findViewById(R.id.lv_avatar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bMyProfile)
                    return;

                new CameraKindDialog(ProfileActivity.this, new CameraKindDialog.onCameraKindListener() {
                    @Override
                    public void onCameraKind(int kind) {
                        if (kind == CameraKindDialog.KIND_PHOTO) {
                            openCamera();
                        } else if (kind == CameraKindDialog.KIND_GALLERY) {
                            openGallery();
                        }
                    }
                }).show();
            }
        });

        findViewById(R.id.lv_edit_phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new InputDialog(ProfileActivity.this, g_userInfo.getPhone(), new InputDialog.onOKButtonListener() {
                    @Override
                    public void onOKButton(String szInputed) {
                        g_userInfo.setPhone(szInputed);
                        if (bMyProfile)
                            FiaApplication.getMyUserInfo().setPhone(szInputed);
                        tvPhone.setText(szInputed);
                        updateMyUserInfo();
                    }
                }).show();
            }
        });

        findViewById(R.id.lv_call_phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callPhone(g_userInfo.getPhone());
            }
        });

        findViewById(R.id.lv_edit_rating).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RatingDialog(ProfileActivity.this, new RatingDialog.onOKButtonListener() {
                    @Override
                    public void onOKButton(int rate) {
                        if (rate == Constants.LEVEL_NONE)
                            return;

                        g_userInfo.addRating(rate);
                        if (bMyProfile)
                            FiaApplication.getMyUserInfo().addRating(rate);
                        refreshRatingInfo();
                        updateMyUserInfo();
                    }
                }).show();
            }
        });

        findViewById(R.id.lv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        initialize();
    }

    private void initialize() {
        tvTitle.setText(bMyProfile ? "My Profile" : "Profile");
        lvChat.setVisibility(bMyProfile ? View.GONE : View.VISIBLE);
        lvEditPhone.setVisibility(bMyProfile ? View.VISIBLE : View.GONE);
        lvCallPhone.setVisibility(bMyProfile ? View.GONE : View.VISIBLE);
        lvEditRating.setVisibility(bMyProfile ? View.GONE : View.VISIBLE);

        BitmapHelper.drawImageUrlFromPicasso(this, g_userInfo.getPhotoUrl(), R.drawable.avatar_mark, ivAvatar);
        BitmapHelper.drawImageUrlFromPicasso(this, g_userInfo.getPhotoUrl(), ivAvatarBlurred, true, getResources().getColor(R.color.colorOrangeFilter));
        tvUsername.setText(g_userInfo.getUsername());
        tvPhone.setText(g_userInfo.getPhone());
        refreshRatingInfo();
        tvCreatedParties.setText(String.format("%d", FiaApplication.getMyParties().size()));
        tvInvitedParties.setText(String.format("%d", FiaApplication.getAcceptedParties().size()));
    }

    private void refreshRatingInfo() {
        switch (g_userInfo.getRating()) {
            case Constants.LEVEL_LOW:
                rgRating.check(R.id.rdo_low);
                tvRating.setText("(Low)");
                break;
            case Constants.LEVEL_MIDDLE:
                rgRating.check(R.id.rdo_middle);
                tvRating.setText("(Middle)");
                break;
            case Constants.LEVEL_HIGH:
                rgRating.check(R.id.rdo_high);
                tvRating.setText("(High)");
                break;
            default:
                rgRating.clearCheck();
                tvRating.setText("(None)");
        }
    }

    private void updateMyUserInfo() {
        QBCustomObject userInfoObject = new QBCustomObject();
        userInfoObject.setClassName("UserInfo");
        userInfoObject.setCustomObjectId(g_userInfo.getId());
        userInfoObject.putString("phone", g_userInfo.getPhone());
        userInfoObject.putArray("rates", g_userInfo.getRates());

        BusyHelper.show(this, "Updating profile...");
        QBCustomObjects.updateObject(userInfoObject, null, new QBEntityCallback<QBCustomObject>() {
            @Override
            public void onSuccess(QBCustomObject object, Bundle params) {
                BusyHelper.hide();
                Log.d("Update MyProfile", "success");
            }

            @Override
            public void onError(QBResponseException errors) {
                BusyHelper.hide();
                Log.e("Update MyProfile - ", errors.getMessage());
                Toast.makeText(ProfileActivity.this, R.string.connection_failed, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.move_in_fade, R.anim.move_out_fade);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("FileUri", mFileUri);
        outState.putParcelable("FileCropUri", mFileCropUri);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mFileUri = (Uri)savedInstanceState.getParcelable("FileUri");
            mFileCropUri = (Uri)savedInstanceState.getParcelable("FileCropUri");
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    public void openCamera() {
        try {
            File outputFile = BitmapHelper.getOutputMediaFile(false);
            if (outputFile == null) {
                Toast.makeText(this, "Cannot create image file from camera", Toast.LENGTH_LONG).show();
                return;
            }
            mFileUri = Uri.fromFile(outputFile);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra("return-data", true/*return_data*/);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
            cameraIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

            startActivityForResult(cameraIntent, Constants.REQUEST_CAMERA);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Your device doesn't support capturing images", Toast.LENGTH_LONG).show();
        }
    }

    public void openGallery() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, null);
            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(intent, Constants.REQUEST_GALLERY);
        }
        catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Your device doesn't support capturing images", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Crop image
     */
    private void cropImage(Uri picUri) {
        if (picUri == null)
            return;

        try {
            File outputFile = BitmapHelper.getOutputMediaFile(false);
            if (outputFile == null) {
                Toast.makeText(this, "Cannot create crop image file", Toast.LENGTH_LONG).show();
                return;
            }

            //create new Intent
            Intent intent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            intent.setDataAndType(picUri, "image/*");
            // set crop
            intent.putExtra("crop", "true");

            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("scaleUpIfNeeded", true);
            intent.putExtra("outputX", 256); // max value
            intent.putExtra("outputY", 256);
            intent.putExtra("setWallpaper", false);
            intent.putExtra("return-data", false);

            mFileCropUri = Uri.fromFile(outputFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileCropUri);

            startActivityForResult(intent, Constants.REQUEST_CROP);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Your device doesn't support cropping images", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == Constants.REQUEST_CAMERA) {
            cropImage(mFileUri);
        } else if (requestCode == Constants.REQUEST_GALLERY) {
            cropImage(data.getData());
        } else if (requestCode == Constants.REQUEST_CROP) {
            BitmapHelper.drawImageUrlFromPicasso(this, mFileCropUri.getPath(), R.drawable.avatar_mark, ivAvatar);
            BitmapHelper.drawImageUrlFromPicasso(this, mFileCropUri.getPath(), ivAvatarBlurred, true, getResources().getColor(R.color.colorOrangeFilter));
            if (bMyProfile && mFileCropUri != null) {
                BusyHelper.show(this, "Uploading avatar...");
                FiaApplication.uploadPhotoUrl(mFileCropUri.getPath(), true, new FiaApplication.onUploadedDoneListener() {
                    @Override
                    public void onUploadedDone(String url) {
                        BusyHelper.hide();
                    }
                });
            }
        }
    }

    public void callPhone(String phoneNumber) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            String uriString = String.format("tel:%s", phoneNumber);
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse(uriString));
            startActivity(callIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
