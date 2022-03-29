package abbas.samih.mlfirebasetest.FullExample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;

import abbas.samih.mlfirebasetest.R;

public class BaseActivity extends AppCompatActivity {
    public static final int RC_STORAGE_PERMS1 = 101;
    public static final int RC_SELECT_PICTURE = 103;
    public static final String ACTION_BAR_TITLE = "action_bar_title";
    public File imageFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getIntent().getStringExtra(ACTION_BAR_TITLE));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

//If “gallery_action” is selected, then...//

            case R.id.action_gallery:

//...check we have the WRITE_STORAGE permission//

                checkStoragePermission(RC_STORAGE_PERMS1);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RC_STORAGE_PERMS1:

//If the permission request is granted, then...//

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

//...call selectPicture//

                    selectPicture();

//If the permission request is denied, then...//

                } else {

//...display the “permission_request” string//

                    MyHelper.needPermission(this, requestCode, R.string.permission_request);
                }
                break;

        }
    }

//Check whether the user has granted the WRITE_STORAGE permission//

    public void checkStoragePermission(int requestCode) {
        switch (requestCode) {
            case RC_STORAGE_PERMS1:
                int hasWriteExternalStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

//If we have access to external storage...//

                if (hasWriteExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {

//...call selectPicture, which launches an Activity where the user can select an image//

                    selectPicture();

//If permission hasn’t been granted, then...//

                } else {

//...request the permission//

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
                }
                break;

        }
    }

    protected void selectPicture() {
        imageFile = MyHelper.createTempFile(imageFile);
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RC_SELECT_PICTURE);
    }

}