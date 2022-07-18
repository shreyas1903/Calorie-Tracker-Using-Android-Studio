package edu.upenn.cis350.group1.calorietracker;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public abstract class CalorieTrackerActivity extends AppCompatActivity{

    // class variables necessary for photo saving
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 0;
    String mCurrentPhotoPath;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tracker_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_today :
                toDaily(getCurrentFocus());
                break;
            case R.id.menu_calendar :
                toCalendar(getCurrentFocus());
                break;
            case R.id.menu_settings :
                toSettings(getCurrentFocus());
                break;
            case R.id.menu_weight :
                toWeight(getCurrentFocus());
                break;
            case R.id.menu_progress :
                toProgress(getCurrentFocus());
                break;
            case R.id.menu_photo :
                toPhoto(getCurrentFocus());
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    public void toDaily(View v) {
        Intent intent = new Intent(this, DailyActivity.class);
        startActivity(intent);

        //closes prior activity
        if (v != null) {
            if (!(v.getContext() instanceof DailyActivity)) {
                finish();
            }
        }
        else {
            finish();
        }
    }

    public void toCalendar(View v) {
        Intent intent = new Intent(this, CalendarActivity.class);
        startActivity(intent);

        //closes prior activity
        if (v != null) {
            if (!(v.getContext() instanceof DailyActivity)) {
                finish();
            }
        }
        else {
            finish();
        }
    }

    public void toSettings(View v) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);

        //closes prior activity
        if (v != null) {
            if (!(v.getContext() instanceof DailyActivity)) {
                finish();
            }
        }
        else {
            finish();
        }
    }

    public void toWeight(View v) {
        Intent intent = new Intent(this, WeightTrackingActivity.class);
        startActivity(intent);

        //closes prior activity
        if (v != null) {
            if (!(v.getContext() instanceof DailyActivity)) {
                finish();
            }
        }
        else {
            finish();
        }
    }

    public void toProgress(View v) {
        Intent intent = new Intent(this, ProgressActivity.class);
        startActivity(intent);

        //closes prior activity
        if (v != null) {
            if (!(v.getContext() instanceof DailyActivity)) {
                finish();
            }
        }
        else {
            finish();
        }
    }

    public void toPhoto(View v) {
        // Allows user to take photo only if there exists a camera in phone hardware
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                photoFile = null;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                galleryAddPic();
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), imageFileName);;

        // Save a file path to enable saving
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        // Check if permission is granted first, if not prompt user, otherwise add image
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);

        } else {
            addImageToGallery(mCurrentPhotoPath, getApplicationContext());
        }
    }

    private void addImageToGallery(final String filePath, final Context context) {
        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    addImageToGallery(mCurrentPhotoPath, getApplicationContext());

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
