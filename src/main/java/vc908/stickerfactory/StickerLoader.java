package vc908.stickerfactory;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.lang.ref.WeakReference;

import vc908.stickerfactory.utils.Logger;
import vc908.stickerfactory.utils.NamesHelper;

/**
 * @author Dmitry Nezhydenko
 */
public class StickerLoader {
    private static final String TAG = StickerLoader.class.getSimpleName();
    private RequestManager requestManager;
    private WeakReference<ImageView> mImageViewWeakReference;

    private String contentId;
    private Drawable placeholderDrawable;

    public StickerLoader(Context context) {
        requestManager = Glide.with(context);
    }

    public StickerLoader(Activity activity) {
        requestManager = Glide.with(activity);
    }

    public StickerLoader(android.support.v4.app.Fragment fragment) {
        requestManager = Glide.with(fragment);
    }

    public StickerLoader loadSticker(String code) {
        this.contentId = NamesHelper.getContentIdFromCode(code);
        return this;
    }

    public void into(@NonNull ImageView iv) {
        mImageViewWeakReference = new WeakReference<>(iv);
        placeholderDrawable = ContextCompat.getDrawable(iv.getContext(), R.drawable.sp_sticker_placeholder);
        placeholderDrawable.setColorFilter(ContextCompat.getColor(iv.getContext(), R.color.sp_placeholder_color_filer), PorterDuff.Mode.SRC_IN);
        File file = StorageManager.getInstance().getImageFile(contentId);
        iv.setTag(R.id.sp_loader_key, this);
        if (file == null || !file.exists()) {
            iv.setImageDrawable(placeholderDrawable);
            NetworkManager.getInstance().downloadSticker(contentId)
                    .subscribe(
                            resultFile -> {
                                ImageView imageView = mImageViewWeakReference.get();
                                if (imageView != null) {
                                    load(imageView, resultFile);
                                }
                            },
                            th -> Logger.e(TAG, "Can't display sticker", th)

                    );
        } else {
            load(iv, file);
        }
    }

    private void load(@NonNull ImageView iv, @NonNull File file) {
        if (iv.getTag(R.id.sp_loader_key) == StickerLoader.this) {
            if (requestManager != null)
                requestManager.load(file)
                        .placeholder(placeholderDrawable)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .into(iv);
        } else {
            // check for corruption
            Object obj = iv.getTag(R.id.sp_loader_key);
            if (obj != null && !(obj instanceof StickerLoader)) {
                throw new RuntimeException("You can't use R.id.sp_loader_key for setting own tags.");
            }
        }
    }

}
