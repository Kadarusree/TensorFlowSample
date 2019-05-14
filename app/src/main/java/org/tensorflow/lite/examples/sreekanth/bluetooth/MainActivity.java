/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package org.tensorflow.lite.examples.sreekanth.bluetooth;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.FragmentTransaction;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.examples.sreekanth.R;
import org.tensorflow.lite.examples.sreekanth.common.activities.SampleActivityBase;
import org.tensorflow.lite.examples.sreekanth.common.logger.Log;
import org.tensorflow.lite.examples.sreekanth.common.logger.LogWrapper;
import org.tensorflow.lite.examples.sreekanth.common.logger.MessageOnlyLogFilter;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;




/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class MainActivity extends SampleActivityBase {

    public static final String TAG = "MainActivity";

    // Whether the Log Fragment is currently shown
    private boolean mLogShown;





    private KeyStore keyStore;
    // Variable used for storing the key in the Android Keystore container
    private static final String KEY_NAME = "androidHive";
    private Cipher cipher;
    private TextView textView;


    RadioButton test, train;
    Spinner users;

    String storagePath, testpath, trainpath;


    ArrayList<Model> images;


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    //    intitFP()    ;


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


    @TargetApi(Build.VERSION_CODES.M)
    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }

        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get KeyGenerator instance", e);
        }

        try {
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
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

public void createFolders(){
    String test = Environment.getExternalStorageDirectory()+"/FaceRec/Test";
    String train = Environment.getExternalStorageDirectory()+"/FaceRec/Train";

    File ftest = new File(test);
    if(!ftest.exists()){
        ftest.mkdir();
    }
    else {

    }

    File ftrain = new File(train);
    if(!ftrain.exists()){
        ftrain.mkdir();
    }
    else {

    }

    String user1= train+"/user1";
    String user2= train+"/user2";
    String user3= train+"/user3";

    File fuser1 = new File(user1);
    if(!fuser1.exists()){
        fuser1.mkdir();
    }
    else {

    }

    File fuser2 = new File(user2);
    if(!fuser2.exists()){
        fuser2.mkdir();
    }
    else {

    }


    File fuser3 = new File(user3);
    if(!fuser3.exists()){
        fuser3.mkdir();
    }
    else {

    }

}

}
