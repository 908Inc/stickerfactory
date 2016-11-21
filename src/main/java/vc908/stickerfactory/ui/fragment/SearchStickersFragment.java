package vc908.stickerfactory.ui.fragment;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import vc908.stickerfactory.NetworkManager;
import vc908.stickerfactory.R;
import vc908.stickerfactory.StorageManager;
import vc908.stickerfactory.events.KeyboardVisibilityChangedEvent;
import vc908.stickerfactory.model.SearchResultItem;
import vc908.stickerfactory.ui.OnStickerFileSelectedListener;
import vc908.stickerfactory.ui.OnStickerSelectedListener;
import vc908.stickerfactory.utils.KeyboardUtils;
import vc908.stickerfactory.utils.Logger;
import vc908.stickerfactory.utils.Utils;

/**
 * Recent stickers list
 *
 * @author Dmitry Nezhydenko
 */
public class SearchStickersFragment extends StickersListFragment {
    private String currentText = "";
    private String TAG = SearchStickersFragment.class.getSimpleName();
    private Handler mHandler;
    private Map<String, String> stickers = new LinkedHashMap<>();
    private TextView searchEdit;
    private ProgressBar searchProgress;
    private ImageView clearButton;
    private WeakReference<ImageView> backButtonReference;
    private View.OnClickListener searchExternalClickListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.sp_fragment_search_stickers;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = super.onCreateView(inflater, container, savedInstanceState);
        searchEdit = (TextView) layout.findViewById(R.id.sp_search_edit);
        searchEdit.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        searchEdit.setOnClickListener(v -> {
            if (searchExternalClickListener != null) {
                searchExternalClickListener.onClick(searchEdit);
            }
        });
        searchEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (searchExternalClickListener != null && hasFocus) {
                searchExternalClickListener.onClick(searchEdit);
            }
        });
        searchProgress = (ProgressBar) layout.findViewById(R.id.sp_search_progress);
        Utils.setColorFilter(getContext(), searchProgress.getIndeterminateDrawable(), R.color.sp_search_fragment_icons);
        clearButton = (ImageView) layout.findViewById(R.id.sp_search_clear);
        clearButton.setOnClickListener(v -> searchEdit.setText(""));
        Utils.setColorFilter(getContext(), clearButton, R.color.sp_search_fragment_icons);
        ImageView backButton = (ImageView) layout.findViewById(R.id.sp_search_back);
        backButton.setOnClickListener(v -> KeyboardUtils.hideKeyboard(getContext(), v));
        Utils.setColorFilter(getContext(), backButton, R.color.sp_search_fragment_icons);
        backButtonReference = new WeakReference<>(backButton);
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mHandler.removeCallbacks(searchRunnable);
                currentText = String.valueOf(s);
                mHandler.postDelayed(searchRunnable, 500);
                updateClearButtonVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mHandler.postDelayed(searchRunnable, 0);
        return layout;
    }

    public void setExternalSearchEditClickListener(View.OnClickListener searchExternalClickListener) {
        this.searchExternalClickListener = searchExternalClickListener;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(KeyboardVisibilityChangedEvent event) {
        if (backButtonReference != null && backButtonReference.get() != null) {
            backButtonReference.get().setVisibility(event.isVisible() ? View.VISIBLE : View.GONE);
            if (!event.isVisible()) {
                getActivity().getWindow().getDecorView().clearFocus();
            }
        }
    }

    private Runnable searchRunnable = () -> {
        if (isVisible()) {
            searchProgress.setVisibility(View.VISIBLE);
            clearButton.setVisibility(View.GONE);
            NetworkManager.getInstance().requestSearch(currentText, true, false).subscribe(
                    result -> {
                        onLoadFinished(null, createCursor(result.getData()));
                        searchProgress.setVisibility(View.INVISIBLE);
                        updateClearButtonVisibility();
                    },
                    th -> {
                        Logger.e(TAG, "Can't complete search request", th);
                        searchProgress.setVisibility(View.INVISIBLE);
                        updateClearButtonVisibility();
                    });
        }
    };

    private void updateClearButtonVisibility() {
        clearButton.setVisibility(TextUtils.isEmpty(searchEdit.getText()) ? View.GONE : View.VISIBLE);
    }

    @Override
    protected boolean isStickerPreviewEnabled() {
        return false;
    }


    private Cursor createCursor(List<SearchResultItem> data) {
        MatrixCursor cursor = new MatrixCursor(new String[]{"_id"});
        stickers.clear();
        if (getContext() != null) {
            for (SearchResultItem item : data) {
                String contentId = item.getContentId();
                StorageManager.getInstance().storeContentPackName(contentId, item.getPack());
                String link = item.getImage().get(Utils.getDensityName(getContext()));
                cursor.addRow(new Object[]{contentId});
                stickers.put(contentId, link);
            }
        }
        return cursor;
    }

    @Override
    protected int getLoaderId() {
        // disable loader
        return 0;
    }

    @Override
    protected StickersAdapter createStickersAdapter(Cursor cursor) {
        return new StickersAdapter(this, cursor, stickerSelectedListeners, stickerFileSelectedListener, stickerPreviewDelegate);
    }

    public boolean isSearchFieldActive() {
        return searchEdit != null && searchEdit.hasFocus();
    }

    public void setSearchHeight(int height) {
        if (searchEdit.getHeight() != height) {
            searchEdit.getLayoutParams().height = height;
            searchEdit.requestLayout();
        }
    }

    private class StickersAdapter extends StickersListFragment.StickersAdapter {

        public StickersAdapter(Fragment fragment, Cursor cursor, List<OnStickerSelectedListener> stickerSelectedListeners, OnStickerFileSelectedListener stickerFileSelectedListener, StickersListFragment.StickerPreviewDelegate stickerPreviewDelegate) {
            super(fragment, cursor, stickerSelectedListeners, stickerFileSelectedListener, stickerPreviewDelegate);
        }

        protected String getContentId(Cursor cursor) {
            return cursor.getString(0);
        }

        protected Uri getFileUri(String contentId) {
            File file = StorageManager.getInstance().getImageFile(contentId);
            if (file.exists()) {
                return Uri.fromFile(file);
            } else {
                NetworkManager.getInstance().downloadImage(stickers.get(contentId), contentId)
                        .subscribe(
                                result -> {
                                    if (result) {
                                        notifyItemLoaded(contentId);
                                    }
                                },
                                e -> Logger.e(TAG, e)
                        );
            }
            return null;
        }

        private void notifyItemLoaded(String contentId) {
            int position = 0;
            for (String key : stickers.keySet()) {
                if (key.equals(contentId)) {
                    notifyItemChanged(position);
                    break;
                }
                position++;
            }
        }

    }
}
