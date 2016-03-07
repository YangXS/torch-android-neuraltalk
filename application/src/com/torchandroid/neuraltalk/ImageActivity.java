package com.torchandroid.neuraltalk;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Color;

import com.torchandroid.neuraltalk.lua.LuaManager;
import com.torchandroid.neuraltalk.util.Util;

public class ImageActivity extends Activity {

	final private String TAG = "ImageActivity";

	private final int REQUEST_CODE_IMAGE = 100;
	private final int REQUEST_CODE_CAMERA = 200;
	private final int MSG_START_IMAGE_CAPTIONING = 0;
  // Boolean is used to inactivate all the buttons while captioning.
  // TODO: Use handlerthread to handle different cases gracefully.
  private AtomicBoolean isCaptioning = new AtomicBoolean(false);

	private LuaManager mLuaManager;
	private Button mSelectImageButton;
	private Button mStartCaptionButton;
	private Button mButtonCamera;
	private ImageView mImageView;
	private Uri mImageUri;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_image_selection);

		mSelectImageButton = (Button) findViewById(R.id.select_image_button);
		mSelectImageButton.setOnClickListener(mSelectImageButtonClickListener);

		mStartCaptionButton = (Button) findViewById(R.id.start_caption_button);

		mButtonCamera = (Button) findViewById(R.id.button_camera);
		mButtonCamera.setOnClickListener(mCamerabuttonClickListener);

		mLuaManager = LuaManager.getLuaManager(this);
		mImageView = (ImageView) findViewById(R.id.image);

	}

	/**
	 * send intent to pick an image
	 */
	OnClickListener mSelectImageButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
      if (isCaptioning.get()) {
        return;
      }
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setType("image/*");
			startActivityForResult(intent, REQUEST_CODE_IMAGE);
		}
	};

	/**
	 * get selected image uri
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CODE_IMAGE) {
				mImageUri = data.getData();
        if (mImageUri != null && mImageUri.getPath() != null) {
            File selectedImgFile = new File(getRealPathFromURI(mImageUri));
            Log.d(TAG, "Image path : " + mImageUri.getPath());
            Bitmap bitmap = BitmapFactory.decodeFile(selectedImgFile.getPath());
            createAndSendMessage(bitmap);
        }
			} else if (requestCode == REQUEST_CODE_CAMERA) {
				  Bundle extras = data.getExtras();
				  // get bitmap from returned intent
				  Bitmap bitmap = (Bitmap) extras.get("data");
				  createAndSendMessage(bitmap);
			 }
		}
	}

	/**
	 * get file path from content Uri
	 * 
	 * @param contentUri
	 * @return file path by string
	 */
	private String getRealPathFromURI(Uri contentUri) {

		String filePath;
		String scheme = contentUri.getScheme();

		if (scheme.equals("file")
				&& contentUri.getEncodedAuthority().equals(
						"org.openintents.filemanager")) {
			// Get the path
			filePath = contentUri.getPath();
			return filePath;
		} else if (scheme.equals("content")) {
			String[] projection = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(contentUri, projection,
					null, null, null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}

		return contentUri.getPath();
	}

// This has to be run on the background thread to avoid ANR.
  private void processMessage(Message msg) {
    runOnUiThread(new Runnable() {
        @Override
        public void run() {
            mStartCaptionButton.setText(R.string.wait_for_caption);
        }
    });

    Log.d(TAG, "inside process message and the thread id is: " +
        Thread.currentThread().getId() + " the thread name is: " +
        Thread.currentThread().getName());

    final Bitmap imageBitmap = (Bitmap) msg.obj;

    final String result = mLuaManager.getImageCaptioningResult(
        imageBitmap.getWidth(),
        imageBitmap.getHeight(),
        Util.getImageRGBA(imageBitmap));
    runOnUiThread(new Runnable() {
        @Override
        public void run() {
            mStartCaptionButton.setBackgroundColor(Color.argb(50, 0, 255, 0));
            mStartCaptionButton.setText(result);
        }
    });
  }

  private void createAndSendMessage(Bitmap bitmap) {
    if (bitmap != null) {
      mStartCaptionButton.setBackgroundColor(Color.argb(50, 0, 0, 255));
      mImageView.setImageBitmap(bitmap);
      Log.d(TAG, "bitmap size : " + bitmap.getWidth() + ","
          + bitmap.getHeight());

      final Message imageMessage = new Message();
      imageMessage.what = MSG_START_IMAGE_CAPTIONING;
      imageMessage.obj = bitmap;
      Thread captionThread = new Thread(new Runnable(){
          @Override
          public void run() {
              isCaptioning.set(true);
              try {
                processMessage(imageMessage);
              } catch (Exception e) {
                e.printStackTrace();
              }
              isCaptioning.set(false);
          }
      });
      captionThread.start();
    } else {
      Log.e(TAG, "bitmap null");
    }
  }

	View.OnClickListener mCamerabuttonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
      if (isCaptioning.get()) {
        return;
      }
			Intent intent = new Intent();
			intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(intent, REQUEST_CODE_CAMERA);
		}
	};

}
