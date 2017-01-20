package com.partyappfia.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.partyappfia.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.wasabeef.picasso.transformations.BlurTransformation;
import jp.wasabeef.picasso.transformations.ColorFilterTransformation;

public class BitmapHelper {

	public static void drawImageUrlFromPicasso(Context context, String url, ImageView imageView) {
		try {
			if (url != null) {
                File file = new File(url);
                if (file != null && file.exists())
                    Picasso.with(context).load(new File(url)).into(imageView);
                else
                    Picasso.with(context).load(url).into(imageView);
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public static void drawImageUrlFromPicasso(Context context, String url, ImageView imageView, boolean blur, int colorFilter) {
        try {
            if (url != null) {
                File file = new File(url);
                if (blur) {
                    int blurRadius = 0;
                    //.transform(new BlurTransformation(context, blurRadius))
                    if (file != null && file.exists())
                        Picasso.with(context).load(new File(url)).transform(new ColorFilterTransformation(colorFilter)).into(imageView);
                    else
                        Picasso.with(context).load(url).transform(new ColorFilterTransformation(colorFilter)).into(imageView);
                } else {
                    drawImageUrlFromPicasso(context, url, imageView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	public static void drawImageUrlFromPicasso(Context context, String url, int placeholderResId, ImageView imageView) {
		try {
			if (url != null && !url.isEmpty()) {
                File file = new File(url);
                if (file != null && file.exists())
                    Picasso.with(context).load(new File(url)).placeholder(placeholderResId).into(imageView);
                else
                    Picasso.with(context).load(url).placeholder(placeholderResId).into(imageView);
            }
		} catch (Exception e) {
			e.printStackTrace();
            imageView.setImageResource(placeholderResId);
		}
	}

	public static File getOutputMediaFile(boolean bVideo) {
		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		File mediaFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + (bVideo ? "VIDEO_" : "IMG_") + timeStamp + (bVideo ? ".mp4" : ".jpg"));
		return mediaFile;
	}

	public static Bitmap RGB565toARGB888(Bitmap img) throws Exception {
		int numPixels = img.getWidth() * img.getHeight();
		int[] pixels = new int[numPixels];

		// Get JPEG pixels. Each int is the color values for one pixel.
		img.getPixels(pixels, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());

		// Create a Bitmap of the appropriate format.
		Bitmap result = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);

		// Set RGB pixels.
		result.setPixels(pixels, 0, result.getWidth(), 0, 0, result.getWidth(), result.getHeight());
		return result;
	}

	public static boolean saveVideoThumbnail(String szVideoFilePath, String savedThumbnialFilePath) {
		Bitmap bmThumbnail = getVideoThumbnail(szVideoFilePath);
		if (bmThumbnail == null)
			return false;

		return saveBitmapToFilePath(bmThumbnail, savedThumbnialFilePath);
	}

	public static Bitmap getVideoThumbnail(String szVideoFilePath) {
		return ThumbnailUtils.createVideoThumbnail(szVideoFilePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
	}

	public static boolean saveBitmapToFilePath(Bitmap bitmap, String filePath) {
		if (bitmap == null)
			return false;

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filePath);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
			// PNG is a lossless format, the compression factor (100) is ignored
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return false;
	}
}
