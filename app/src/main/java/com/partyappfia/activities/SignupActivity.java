package com.partyappfia.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.partyappfia.R;
import com.partyappfia.application.FiaApplication;
import com.partyappfia.config.Constants;
import com.partyappfia.dialogs.CameraKindDialog;
import com.partyappfia.model.UserInfo;
import com.partyappfia.utils.BitmapHelper;
import com.partyappfia.utils.BusyHelper;
import com.partyappfia.utils.PhoneHelper;
import com.partyappfia.utils.PrefManager;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.io.File;
import java.util.List;

public class SignupActivity extends AppCompatActivity {

    private Uri mFileUri = null;
    private Uri mFileCropUri = null;

    private ImageView   ivAvatar;
    private EditText    evUsername;
    private EditText    evPhone;
    private EditText    evPassword;
    private EditText    evConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ivAvatar    = (ImageView) findViewById(R.id.iv_avatar);
        evUsername  = (EditText) findViewById(R.id.ev_username);
        evPhone     = (EditText) findViewById(R.id.ev_phone);
        evPassword  = (EditText) findViewById(R.id.ev_password);
        evConfirm   = (EditText) findViewById(R.id.ev_confirm);

        evPhone.setText("+" + PhoneHelper.GetCountryZipCode(this));

        findViewById(R.id.lv_avatar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CameraKindDialog(SignupActivity.this, new CameraKindDialog.onCameraKindListener() {
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

        findViewById(R.id.lv_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(R.anim.move_in_right, R.anim.move_out_right);
        finish();
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
            ivAvatar.setImageURI(mFileCropUri);
        }
    }

    private void signUp() {
        final String username   = evUsername.getText().toString();
        final String phone      = evPhone.getText().toString();
        final String password   = evPassword.getText().toString();
        final String confirm    = evConfirm.getText().toString();

        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter user name", Toast.LENGTH_LONG).show();
            return;
        }

        if (username.length() < 3) {
            Toast.makeText(this, "Please enter password more than 3 characters", Toast.LENGTH_LONG).show();
            return;
        }

        if (phone.isEmpty()) {
            Toast.makeText(this, "Please enter phone number", Toast.LENGTH_LONG).show();
            return;
        }

        if (!phone.contains("+")) {
            Toast.makeText(this, "Please enter the correct phone number with the country code", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.length() < 8) {
            Toast.makeText(this, "Please enter password more than 8 characters", Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "Password is not correct. Please try again", Toast.LENGTH_LONG).show();
            return;
        }

        BusyHelper.show(this, "Signing up...");
        QBAuth.createSession(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {

                QBUser user = new QBUser(username, password);
                QBUsers.signUpSignInTask(user, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(final QBUser user, Bundle args) {
                        PrefManager.savePrefString(Constants.PREF_USERNAME, username);
                        PrefManager.savePrefString(Constants.PREF_PASSWORD, password);

                        if (mFileCropUri != null) {
                            FiaApplication.uploadPhotoUrl(mFileCropUri.getPath(), false, new FiaApplication.onUploadedDoneListener() {
                                @Override
                                public void onUploadedDone(String url) {
                                    registerUserInfo(username, phone, url);
                                }
                            });
                        } else {
                            registerUserInfo(username, phone, "");
                        }
                    }

                    @Override
                    public void onError(QBResponseException errors) {
                        BusyHelper.hide();
                        Log.d("Signup - ", errors.getMessage());
                        if (errors.getHttpStatusCode() == 422) { // QBResponseStatusCodeValidationFailed
                            Toast.makeText(SignupActivity.this, "There is exist same user. Please try again", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(SignupActivity.this, R.string.connection_failed, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {
                BusyHelper.hide();
                Log.d("Signup Auth - ", e.getMessage());
                Toast.makeText(SignupActivity.this, R.string.connection_failed, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void registerUserInfo(String username, String phone, String photoUrl) {
        QBCustomObject object = new QBCustomObject();
        object.setClassName("UserInfo");

        object.putString("username",    username);
        object.putString("phone",       phone);
        object.putString("photoUrl",    photoUrl);
        object.putInteger("deviceType", Constants.DEVICE_ANDROID);

        QBCustomObjects.createObject(object, new QBEntityCallback<QBCustomObject>() {
            @Override
            public void onSuccess(final QBCustomObject createdObject, Bundle params) {
                FiaApplication.getApp().readDataFromServer(SignupActivity.this);
            }

            @Override
            public void onError(QBResponseException errors) {
                BusyHelper.hide();
                Log.d("Signup3 - ", errors.getMessage());
                Toast.makeText(SignupActivity.this, R.string.connection_failed, Toast.LENGTH_LONG).show();
            }
        });
    }
}
