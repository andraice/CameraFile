package pe.upc.android.camerify;

import android.Manifest;
import android.Manifest.permission;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;

import android.net.Uri;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton fab;
    Switch captureSwitch;
    private static final int CAMERIFY_PERMISSIONS_REQUEST = 100;
    private boolean cameraAvailable = false;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;

    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int MEDIA_TYPE_VIDEO = 2;

    private Uri fileUri;
    private Uri lastOutputMediaFileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        captureSwitch = (Switch) findViewById(R.id.captureSwitch);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureMedia();
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        validatePermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean permissionsGranted()
    {
        boolean grantedCameraPermission =
                (ContextCompat.checkSelfPermission(this, permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED
                );
        boolean grantedStoragePermission =
                (ContextCompat.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED
                );
        Log.d("Camerily", "Permission for CAMERA:" +
                String.valueOf(grantedCameraPermission));

        Log.d("Camerily", "Permission for STORAGE:" +
                String.valueOf(grantedStoragePermission));

        return (grantedCameraPermission && grantedStoragePermission);
    }

    private void validatePermissions()
    {
        if(permissionsGranted())
        {
            cameraAvailable= true;
        }
        ActivityCompat.requestPermissions(this,
                new String[] {
                        permission.WRITE_EXTERNAL_STORAGE,
                        permission.CAMERA
                }, CAMERIFY_PERMISSIONS_REQUEST);
    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResusts
    ){
        switch (requestCode)
        {
            case CAMERIFY_PERMISSIONS_REQUEST: {
                cameraAvailable = (
                                (grantResusts.length > 0 &&
                                grantResusts[0] == PackageManager.PERMISSION_GRANTED) &&
                                (grantResusts.length > 0 &&
                                grantResusts[1] == PackageManager.PERMISSION_GRANTED)
                   );
                updatePermissionsDependentFeatures();
            }
        }
    }

    private void updatePermissionsDependentFeatures()
    {
        captureSwitch.setEnabled(cameraAvailable);
        fab.setEnabled(cameraAvailable);

    }

    private void captureMedia(){
        if(captureSwitch.isChecked()){
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            lastOutputMediaFileUri = fileUri;
            startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
        }else{
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            lastOutputMediaFileUri = fileUri;
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    private Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE ){
            if(resultCode == RESULT_OK){
                Log.d("Camerify", "ResultCode: RESULT_OK");
                String fileName = lastOutputMediaFileUri.getPath();

                Toast.makeText(this, "Image saved to: " +
                        fileName, Toast.LENGTH_LONG).show();
            }else if (resultCode == RESULT_CANCELED){
                Log.d("Camerify", "ResultCode: RESULT_CANCELED");
            }else{
                Log.d("Camerify", "ResultCode: " + Integer.toString(resultCode));
            }
        }

        if(requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE ){
            if(resultCode == RESULT_OK){
                Log.d("Camerify", "ResultCode: RESULT_OK");
                String fileName = lastOutputMediaFileUri.getPath();

                Toast.makeText(this, "Video saved to: " +
                        fileName, Toast.LENGTH_LONG).show();
            }else if (resultCode == RESULT_CANCELED){
                Log.d("Camerify", "ResultCode: RESULT_CANCELED");
            }else{
                Log.d("Camerify", "ResultCode: " + Integer.toString(resultCode));
            }
        }
    }

    private static File getOutputMediaFile(int type){
        File mediaStorageDir =
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdirs()) {
                Log.d("Camerify", "Failed to create directoery");
                return null;
            }
        }
        else{
            Log.d("Camerify", "Directory found");
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File mediaFile;

        if(type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(
                    mediaStorageDir.getPath() +
                            File.separator +
                            "IMG_" + timeStamp + ".jpg");
        }
        else if(type == MEDIA_TYPE_VIDEO){
            mediaFile = new File(
                    mediaStorageDir.getPath() +
                            File.separator +
                            "VID_" + timeStamp + ".mp4");
        }
        else{
            return null;
        }

        try{
            Log.d("Camerify", mediaFile.getCanonicalPath());
        }catch (IOException e){
            e.printStackTrace();
        }
        return mediaFile;
    }
}
