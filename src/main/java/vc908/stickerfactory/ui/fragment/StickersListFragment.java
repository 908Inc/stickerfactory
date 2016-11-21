package vc908.stickerfactory.ui.fragment;

import android.app.Dialog;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import vc908.stickerfactory.R;
import vc908.stickerfactory.SplitManager;
import vc908.stickerfactory.StickersManager;
import vc908.stickerfactory.StorageManager;
import vc908.stickerfactory.TasksManager;
import vc908.stickerfactory.model.StickersPack;
import vc908.stickerfactory.provider.stickers.StickersColumns;
import vc908.stickerfactory.provider.stickers.StickersCursor;
import vc908.stickerfactory.ui.OnStickerFileSelectedListener;
import vc908.stickerfactory.ui.OnStickerSelectedListener;
import vc908.stickerfactory.ui.adapter.CursorRecyclerViewAdapter;
import vc908.stickerfactory.ui.view.SquareImageView;
import vc908.stickerfactory.utils.KeyboardUtils;
import vc908.stickerfactory.utils.NamesHelper;
import vc908.stickerfactory.utils.Utils;

/**
 * @author Dmitry Nezhydenko
 */
public class StickersListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = StickersListFragment.class.getSimpleName();
    public static final String ARGUMENT_PACK = "argument_pack";
    protected List<OnStickerSelectedListener> stickerSelectedListeners = new ArrayList<>();
    protected OnStickerFileSelectedListener stickerFileSelectedListener;
    protected StickersAdapter adapter;

    protected RecyclerView rv;
    private String packName;
    protected View progress;
    private View layout;
    private int maxStickerWidth;

    private int currentLoaderId;
    private StickerPreviewDialog currentStickerPreviewDialog;
    protected StickerPreviewDelegate stickerPreviewDelegate;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getLoaderId() > 0) {
            getActivity().getSupportLoaderManager().initLoader(getLoaderId(), null, this);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            packName = getArguments().getString(ARGUMENT_PACK);
        }
        maxStickerWidth = (int) getContext().getResources().getDimension(R.dimen.sp_list_max_sticker_width);
        stickerPreviewDelegate = new StickerPreviewDelegate() {
            @Override
            public void showStickerPreview(String stickerName, int clickedViewSize) {
                showStickerPreviewDialog(stickerName, clickedViewSize);
            }

            @Override
            public void hideStickerPreview() {
                hideStickerPreviewDialog();
            }

            @Override
            public boolean isPackPreviewVisible() {
                return currentStickerPreviewDialog != null;
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (layout == null) {
            layout = inflater.inflate(getLayoutId(), container, false);
            rv = (RecyclerView) layout.findViewById(R.id.recycler_view);
            if (adapter != null) {
                rv.setAdapter(adapter);
            } else {
                // set empty adapter to avoid errors
                rv.setAdapter(emptyAdapter);
            }
            progress = layout.findViewById(R.id.progress);
            int padding = getResources().getDimensionPixelSize(R.dimen.material_8);
            // calculate stickers columns count
            String splitGroup = StorageManager.getInstance().getUserSplitGroup();
            int columnsCount = (int) Math.ceil(Utils.getScreenWidthInPx(getContext())
                    / ((float) (SplitManager.isStickerCellSmallSize() ? maxStickerWidth / 2 : maxStickerWidth)
                    + padding * 2));

            GridLayoutManager lm = (new GridLayoutManager(getContext(), columnsCount));
            rv.setLayoutManager(lm);
        }
        return layout;
    }


    private void showStickerPreviewDialog(String contentId, int clickedViewSize) {
        if (!TextUtils.isEmpty(contentId)) {
            currentStickerPreviewDialog = new StickerPreviewDialog();
            Bundle data = new Bundle();
            data.putString(StickerPreviewDialog.ARG_CONTENT_ID, contentId);
            data.putInt(StickerPreviewDialog.ARG_CLICKED_VIEW_WIDTH, clickedViewSize);
            currentStickerPreviewDialog.setArguments(data);
            currentStickerPreviewDialog.show(getFragmentManager(), contentId);
        }
    }

    private void hideStickerPreviewDialog() {
        if (currentStickerPreviewDialog != null) {
            currentStickerPreviewDialog.dismiss();
            currentStickerPreviewDialog = null;
        }
    }

    public static class StickerPreviewDialog extends DialogFragment {
        public static final String ARG_CONTENT_ID = "arg_content_id";
        public static final String ARG_CLICKED_VIEW_WIDTH = "arg_clicked_view_width";

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            View layout = View.inflate(getContext(), R.layout.sp_preview_image, null);
            ImageView imageView = (ImageView) layout.findViewById(R.id.sp_preview_image);
            imageView.setAlpha(0f);
            imageView.setScaleX(0.5f);
            imageView.setScaleY(0.5f);
            imageView.animate().scaleX(1).scaleY(1).alpha(1).setDuration(200).setStartDelay(100).start();
            dialog.setContentView(layout);
            int clickedViewSize = getArguments().getInt(ARG_CLICKED_VIEW_WIDTH);
            String contentId = getArguments().getString(ARG_CONTENT_ID);
            if (!TextUtils.isEmpty(contentId)) {
                Uri uri = Uri.fromFile(StorageManager.getInstance().getImageFile(contentId));
                Glide.with(getContext())
                        .load(uri)
                        .dontAnimate()
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .thumbnail(Glide.with(getContext())
                                .load(uri)
                                .dontAnimate()
                                .override(clickedViewSize, clickedViewSize))
                        .into(imageView);
            }
            return dialog;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        layout = null;
    }

    protected int getLayoutId() {
        return R.layout.sp_fragment_stickers_list;
    }

    public void addStickerSelectedListener(OnStickerSelectedListener stickerSelectedListener) {
        this.stickerSelectedListeners.add(stickerSelectedListener);
    }

    public void setStickerFileSelectedListener(OnStickerFileSelectedListener listener) {
        stickerFileSelectedListener = listener;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getContext(),
                StickersColumns.CONTENT_URI,
                new String[]{StickersColumns._ID, StickersColumns.CONTENT_ID},
                StickersColumns.PACK + "=?",
                new String[]{packName},
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (getContext() == null) {
            if (adapter != null) {
                adapter.changeCursor(null);
            }
            return;
        }
        progress.setVisibility(View.GONE);
        if (adapter == null) {
            adapter = createStickersAdapter(cursor);
            adapter.setStickerPreviewEnabled(isStickerPreviewEnabled());
            rv.setAdapter(adapter);
        } else {
            adapter.changeCursor(cursor);
        }
        if (cursor.getCount() == 0) {
            TasksManager.getInstance().addPackPurchaseTask(packName, StickersPack.PurchaseType.FREE, false);
            progress.setVisibility(View.VISIBLE);
        }
    }

    protected boolean isStickerPreviewEnabled() {
        return StickersManager.isStickerPreviewEnabled;
    }

    protected StickersAdapter createStickersAdapter(Cursor cursor) {
        return new StickersAdapter(this, cursor, stickerSelectedListeners, stickerFileSelectedListener, stickerPreviewDelegate);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (adapter != null) {
            adapter.changeCursor(null);
        }
    }

    protected int getLoaderId() {
        if (currentLoaderId == 0) {
            currentLoaderId = Utils.atomicInteger.incrementAndGet();
        }
        return currentLoaderId;
    }

    protected interface StickerPreviewDelegate {
        void showStickerPreview(String stickerName, int clickedViewSize);

        void hideStickerPreview();

        boolean isPackPreviewVisible();
    }


    protected static class StickersAdapter extends CursorRecyclerViewAdapter<StickersAdapter.ViewHolder> {

        private Drawable placeholderDrawable;
        private MySimpleGestureListener longPressGestureListener;
        private Fragment mAdapterFragment;
        private final int padding;
        private final List<OnStickerSelectedListener> mStickerSelectedListeners;
        private final OnStickerFileSelectedListener mStickerFileSelectedListener;
        private StickerPreviewDelegate stickerPreviewDelegate;
        private PorterDuffColorFilter selectedItemFilterColor;
        private boolean isStickerPreviewEnabled = true;
        private int halfStickerWidth;

        public StickersAdapter(Fragment fragment, Cursor cursor, List<OnStickerSelectedListener> stickerSelectedListeners, OnStickerFileSelectedListener stickerFileSelectedListener, StickerPreviewDelegate stickerPreviewDelegate) {
            super(cursor);
            mAdapterFragment = fragment;
            mStickerSelectedListeners = stickerSelectedListeners;
            mStickerFileSelectedListener = stickerFileSelectedListener;
            padding = fragment.getContext().getResources().getDimensionPixelSize(R.dimen.material_8);
            selectedItemFilterColor = new PorterDuffColorFilter(0xffdddddd, PorterDuff.Mode.MULTIPLY);
            this.stickerPreviewDelegate = stickerPreviewDelegate;
            longPressGestureListener = new MySimpleGestureListener(stickerPreviewDelegate);
            placeholderDrawable = ContextCompat.getDrawable(fragment.getContext(), R.drawable.sp_sticker_placeholder);
            placeholderDrawable.setColorFilter(ContextCompat.getColor(fragment.getContext(), R.color.sp_placeholder_color_filer), PorterDuff.Mode.SRC_IN);
            halfStickerWidth = (int) mAdapterFragment.getResources().getDimension(R.dimen.sp_list_max_sticker_width) / 2;

        }

        public void setStickerPreviewEnabled(boolean isEnabled) {
            isStickerPreviewEnabled = isEnabled;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView iv;
            if (SplitManager.isStickerCellSmallSize()) {
                iv = new ImageView(mAdapterFragment.getContext());
                GridLayoutManager.LayoutParams lp = new GridLayoutManager.LayoutParams(halfStickerWidth, halfStickerWidth);
                iv.setLayoutParams(lp);
            } else {
                iv = new SquareImageView(mAdapterFragment.getContext());
            }

            iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            iv.setPadding(padding, padding, padding, padding);
            return new ViewHolder(iv);
        }

        @Override
        public int getItemCount() {
            return getCursor().getCount();
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
            viewHolder.contentId = getContentId(cursor);
            Uri uri = getFileUri(viewHolder.contentId);
            if (uri != null) {
                Glide.with(mAdapterFragment)
                        .load(uri)
                        .placeholder(android.R.color.transparent)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(new ImageViewTarget<GlideDrawable>(viewHolder.iv) {
                            @Override
                            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                super.onResourceReady(resource, glideAnimation);
                                getView().setOnTouchListener(imageTouchListener);
                                getView().setTag(R.id.content, viewHolder.contentId);
                            }

                            @Override
                            protected void setResource(GlideDrawable resource) {
                                getView().setImageDrawable(resource);
                            }
                        });
            } else {
                viewHolder.iv.setImageDrawable(null);
            }
        }

        protected String getContentId(Cursor cursor) {
            return new StickersCursor(cursor).getContentId();
        }

        @Nullable
        protected Uri getFileUri(String contentId) {
            return Uri.fromFile(StorageManager.getInstance().getImageFile(contentId));
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView iv;
            private String contentId;

            public ViewHolder(View itemView) {
                super(itemView);
                this.iv = (ImageView) itemView;
                iv.setOnClickListener(v -> {
                    if (mStickerSelectedListeners != null && mStickerSelectedListeners.size() > 0) {
                        for (OnStickerSelectedListener listener : mStickerSelectedListeners) {
                            listener.onStickerSelected(NamesHelper.getStickerCode(contentId));
                        }
                    }
                    if (mStickerFileSelectedListener != null) {
                        mStickerFileSelectedListener.onStickerFileSelected(StorageManager.getInstance().getImageFile(contentId));
                    }
                    KeyboardUtils.hideKeyboard(v.getContext(), v);
                });
            }
        }

        private static final long LONG_PRESS_TIME = 300;
        final Handler handler = new Handler();
        Runnable longPressDetectorRunnable = () -> {
            if (longPressGestureListener != null && isStickerPreviewEnabled) {
                longPressGestureListener.onLongPress();
            }
        };

        View.OnTouchListener imageTouchListener = (v, event) -> {
            if (longPressGestureListener != null) {
                longPressGestureListener.setCurrentView(v);
            }
            if (v instanceof ImageView) {
                ImageView touchedImageView = (ImageView) v;
                if (touchedImageView.getDrawable() != null) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_POINTER_DOWN:
                            touchedImageView.getDrawable().setColorFilter(selectedItemFilterColor);
                            handler.postDelayed(longPressDetectorRunnable, LONG_PRESS_TIME);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_POINTER_UP:
                            if (stickerPreviewDelegate != null) {
                                stickerPreviewDelegate.hideStickerPreview();
                            }
                            touchedImageView.getDrawable().setColorFilter(null);
                            handler.removeCallbacks(longPressDetectorRunnable);
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            touchedImageView.getDrawable().setColorFilter(null);
                            handler.removeCallbacks(longPressDetectorRunnable);
                            break;
                        default:
                    }
                }
            }
            if (stickerPreviewDelegate != null && stickerPreviewDelegate.isPackPreviewVisible()) {
                if (v.getParent() != null) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        };


        private static class MySimpleGestureListener {
            private View currentView;
            private StickerPreviewDelegate stickerPreviewDelegate;

            public MySimpleGestureListener(StickerPreviewDelegate delegate) {
                this.stickerPreviewDelegate = delegate;
            }

            public void setCurrentView(View currentView) {
                this.currentView = currentView;
            }

            public void onLongPress() {
                if (currentView != null && stickerPreviewDelegate != null) {
                    String contentId = (String) currentView.getTag(R.id.content);
                    if (!TextUtils.isEmpty(contentId)) {
                        stickerPreviewDelegate.showStickerPreview(contentId, currentView.getWidth());
                    }
                }
            }
        }
    }

    private RecyclerView.Adapter emptyAdapter = new RecyclerView.Adapter() {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    };
}
