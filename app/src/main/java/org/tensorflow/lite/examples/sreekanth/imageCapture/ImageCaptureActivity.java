package org.tensorflow.lite.examples.sreekanth.imageCapture;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import org.tensorflow.lite.examples.sreekanth.R;
import org.tensorflow.lite.examples.sreekanth.bluetooth.BluetoothChatFragment;
import org.tensorflow.lite.examples.sreekanth.bluetooth.Model;
import org.tensorflow.lite.examples.sreekanth.bluetooth.Utils;
import org.tensorflow.lite.examples.sreekanth.common.activities.SampleActivityBase;
import org.tensorflow.lite.examples.sreekanth.common.logger.Log;
import org.tensorflow.lite.examples.sreekanth.common.logger.LogWrapper;
import org.tensorflow.lite.examples.sreekanth.common.logger.MessageOnlyLogFilter;
import org.tensorflow.lite.examples.sreekanth.listners.PictureCapturingListener;
import org.tensorflow.lite.examples.sreekanth.services.APictureCapturingService;
import org.tensorflow.lite.examples.sreekanth.services.PictureCapturingServiceImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ImageCaptureActivity extends SampleActivityBase implements PictureCapturingListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String[] requiredPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_CODE = 1;

    private ImageView uploadBackPhoto;

    //The capture service
    private APictureCapturingService pictureService;


    public static final String TAG = "MainActivity";

    // Whether the Log Fragment is currently shown
    private boolean mLogShown;








    //Bt Related

    RadioButton test, train;
    Spinner users;
    String storagePath, testpath, trainpath;


    ArrayList<Model> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_capture);
        checkPermissions();
        uploadBackPhoto = (ImageView) findViewById(R.id.backIV);
        // getting instance of the Service from PictureCapturingServiceImpl
        pictureService = PictureCapturingServiceImpl.getInstance(this);
        showToast("Starting capture!");
        pictureService.startCapturing(this);


        //Bt Related
        images= new ArrayList<>();

        storagePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/FaceRec";

        File f=new File(storagePath);
        if(!f.exists()){
            f.mkdir();
        }


        createFolders();


        test = (RadioButton)findViewById(R.id.rb_test);
        train = (RadioButton)findViewById(R.id.rb_train);
        users = (Spinner)findViewById(R.id.spn_users);


        test.setChecked(true);

        File ftest = new File(storagePath+"/Test");
        if(!ftest.exists()){
            ftest.mkdir();
            Utils.storagePath = ftest.getAbsolutePath();
        }
        else {
            Utils.storagePath = ftest.getAbsolutePath();
        }

        test.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    train.setChecked(false);
                    ;
                    File f = new File(storagePath+"/Test");
                    if(!f.exists()){
                        f.mkdir();
                        Utils.storagePath = f.getAbsolutePath();
                    }
                    else {
                        Utils.storagePath = f.getAbsolutePath();
                    }

                }
            }
        });
        train.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    test.setChecked(false);
                    File f = new File(storagePath+"/Train");
                    if(!f.exists()){

                        if (f.mkdir()){
                            trainpath = f.getAbsolutePath();
                            File userpath = new File(trainpath+"/"+getResources().getStringArray(R.array.users)[users.getSelectedItemPosition()]);
                            if (!userpath.exists()){
                                if(userpath.mkdir()){
                                    Utils.storagePath = userpath.getAbsolutePath();
                                }
                            }
                            else {
                                Utils.storagePath = userpath.getAbsolutePath();
                            }
                        }

                    }
                    else{
                        trainpath = f.getAbsolutePath();
                        // users.setSelection(0);
                        File userpath = new File(trainpath+"/"+getResources().getStringArray(R.array.users)[users.getSelectedItemPosition()]);
                        if (!userpath.exists()){
                            if(userpath.mkdir()){
                                Utils.storagePath = userpath.getAbsolutePath();
                            }
                        }
                        else {
                            Utils.storagePath = userpath.getAbsolutePath();
                        }
                    }
                }
            }
        });

        users.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (train.isChecked()){
                    File f = new File(trainpath+"/"+getResources().getStringArray(R.array.users)[position]);
                    if(!f.exists()){
                        f.mkdir();
                        Utils.storagePath = f.getAbsolutePath();
                    }
                    else{
                        Utils.storagePath = f.getAbsolutePath();
                    }
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            BluetoothChatFragment fragment = new BluetoothChatFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }

    }

    private void showToast(final String text) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * We've finished taking pictures from all phone's cameras
     */
    @Override
    public void onDoneCapturingAllPhotos(TreeMap<String, byte[]> picturesTaken) {
        if (picturesTaken != null && !picturesTaken.isEmpty()) {
            showToast("Done capturing all photos!");
            return;
        }
        showToast("No camera detected!");
    }

    /**
     * Displaying the pictures taken.
     */
    @Override
    public void onCaptureDone(String pictureUrl, byte[] pictureData) {
        if (pictureData != null && pictureUrl != null) {
            runOnUiThread(() -> {
                final Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
                final int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
                final Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
                if (pictureUrl.contains("0_pic.jpg")) {
                    uploadBackPhoto.setImageBitmap(scaled);
                    Utils.bitmap = scaled;

                }
            });
            showToast("Picture saved to " + pictureUrl);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_CODE: {
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    checkPermissions();
                }
            }
        }
    }

    /**
     * checking  permissions at Runtime.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        final List<String> neededPermissions = new ArrayList<>();
        for (final String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    permission) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(permission);
            }
        }
        if (!neededPermissions.isEmpty()) {
            requestPermissions(neededPermissions.toArray(new String[]{}),
                    MY_PERMISSIONS_REQUEST_ACCESS_CODE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {


        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_toggle_log:
                mLogShown = !mLogShown;
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Create a chain of targets that will receive log data */
    @Override
    public void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);

        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.

        //  msgFilter.setNext(logFragment.getLogView());

        Log.i(TAG, "Ready");
    }

    public void getTrainpath(){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+"Train";
        File f = new File(path);
        if (f.exists()){
            trainpath = f.getAbsolutePath();
        }
        else {
            f.mkdir();

            f.mkdirs();
        }
    }

    public void createFolders() {
        String test = Environment.getExternalStorageDirectory() + "/FaceRec/Test";
        String train = Environment.getExternalStorageDirectory() + "/FaceRec/Train";

        File ftest = new File(test);
        if (!ftest.exists()) {
            ftest.mkdir();
        } else {

        }

        File ftrain = new File(train);
        if (!ftrain.exists()) {
            ftrain.mkdir();
        } else {

        }

        String user1 = train + "/user1";
        String user2 = train + "/user2";
        String user3 = train + "/user3";

        File fuser1 = new File(user1);
        if (!fuser1.exists()) {
            fuser1.mkdir();
        } else {

        }

        File fuser2 = new File(user2);
        if (!fuser2.exists()) {
            fuser2.mkdir();
        } else {

        }


        File fuser3 = new File(user3);
        if (!fuser3.exists()) {
            fuser3.mkdir();
        } else {

        }
    }
    }

