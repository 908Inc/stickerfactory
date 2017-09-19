package vc908.stickerfactory;

import android.animation.Animator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import vc908.stickerfactory.events.KeyboardVisibilityChangedEvent;
import vc908.stickerfactory.model.SearchResultItem;
import vc908.stickerfactory.ui.OnShopButtonClickedListener;
import vc908.stickerfactory.ui.fragment.StickersFragment;
import vc908.stickerfactory.ui.view.BadgedButton;
import vc908.stickerfactory.ui.view.SquareHeightImageView;
import vc908.stickerfactory.ui.view.StickersKeyboardLayout;
import vc908.stickerfactory.utils.KeyboardUtils;
import vc908.stickerfactory.utils.Logger;
import vc908.stickerfactory.utils.Utils;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class StickersKeyboardController {
    private String TAG = StickersKeyboardController.class.getSimpleName();
    private Handler mHandler = new Handler();
    private Context context;
    private View contentContainer;
    private View stickersFrame;
    private StickersKeyboardLayout stickersKeyboardLayout;
    private BadgedButton stickersButton;
    private EditText chatEdit;
    private StickersFragment stickersFragment;
    private RecyclerView suggestContainer;
    private StickersKeyboardLayout.KeyboardHideCallback keyboardHideCallback;
    private KeyboardVisibilityChangeListener externalKeyboardVisibilityChangeListener;
    private KeyboardVisibilityChangeIntentListener keyboardVisibilityChangeIntentListener;
    private int actionBarHeight;
    private boolean isStickersFrameVisible;
    private String currentSuggestSegment = "";
    private
    @DrawableRes
    int stickersIcon = R.drawable.sp_ic_stickers;
    private
    @DrawableRes
    int keyboardIcon = R.drawable.sp_ic_keyboard;

    public interface KeyboardVisibilityChangeListener {
        void onTextKeyboardVisibilityChanged(boolean isVisible);

        void onStickersKeyboardVisibilityChanged(boolean isVisible);
    }

    public interface KeyboardVisibilityChangeIntentListener {
        void onKeyboardVisibilityChangeIntent();
    }

    private StickersKeyboardController(Context context) {
        this.context = context;
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        } else {
            actionBarHeight = Utils.dp(48, context);
        }
    }

    private void setContentContainer(View contentContainer) {
        this.contentContainer = contentContainer;
    }

    private void setStickersFrame(View stickersFrame) {
        this.stickersFrame = stickersFrame;
    }

    private void setStickersKeyboardLayout(StickersKeyboardLayout stickersKeyboardLayout) {
        this.stickersKeyboardLayout = stickersKeyboardLayout;
        stickersKeyboardLayout.setKeyboardVisibilityChangeListener(keyboardVisibilityChangeListener);
    }

    private void setChatEdit(EditText chatEdit) {
        this.chatEdit = chatEdit;
        chatEdit.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        chatEdit.addTextChangedListener(textWatcher);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onEditTextChanged(String.valueOf(s));
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };


    private void setStickersButton(BadgedButton stickersButton) {
        this.stickersButton = stickersButton;
        stickersButton.setOnClickListener(v -> {
                    if (keyboardVisibilityChangeIntentListener != null) {
                        keyboardVisibilityChangeIntentListener.onKeyboardVisibilityChangeIntent();
                    }
                    if (isStickersFrameVisible) {
                        showKeyboard();
                    } else {
                        if (stickersKeyboardLayout.isKeyboardVisible()) {
                            hideKeyboard(context, () -> setStickersFrameVisible(true));
                        } else {
                            setStickersFrameVisible(true);
                        }
                    }
                }
        );
    }

    private void setStickersFragment(StickersFragment stickersFragment) {
        this.stickersFragment = stickersFragment;
        stickersFragment.setOnEmojiBackspaceClickListener(() -> {
                    KeyEvent event = new KeyEvent(
                            0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                    chatEdit.dispatchKeyEvent(event);
                }
        );
        stickersFragment.addOnShopButtonCickedListener(shopButtonClickListener);
        stickersFragment.setExternalSearchEditClickListener(searchEditClickListener);
    }

    private OnShopButtonClickedListener shopButtonClickListener = new OnShopButtonClickedListener() {
        @Override
        public void onShopButtonClicked() {
            stickersButton.setIsMarked(false);
        }
    };

    private View.OnClickListener searchEditClickListener = v -> {
        if (keyboardVisibilityChangeIntentListener != null) {
            keyboardVisibilityChangeIntentListener.onKeyboardVisibilityChangeIntent();
        }
    };

    private void hideKeyboard(@NonNull Context context, @NonNull StickersKeyboardLayout.KeyboardHideCallback callback) {
        if (stickersKeyboardLayout.isKeyboardVisible()) {
            keyboardHideCallback = callback;
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (!imm.isActive()) {
                return;
            }
            imm.hideSoftInputFromWindow(stickersFrame.getWindowToken(), 0);
        } else {
            callback.onKeyboardHide();
        }
    }

    private KeyboardVisibilityChangeListener keyboardVisibilityChangeListener = new KeyboardVisibilityChangeListener() {
        @Override
        public void onTextKeyboardVisibilityChanged(boolean isVisible) {
            onKeyboardVisibilityChanged(isVisible);
        }

        @Override
        public void onStickersKeyboardVisibilityChanged(boolean isVisible) {
            if (externalKeyboardVisibilityChangeListener != null) {
                externalKeyboardVisibilityChangeListener.onStickersKeyboardVisibilityChanged(isVisible);
            }
        }
    };

    private void onKeyboardVisibilityChanged(boolean isVisible) {
        if (externalKeyboardVisibilityChangeListener != null) {
            externalKeyboardVisibilityChangeListener.onTextKeyboardVisibilityChanged(isVisible);
        }
        EventBus.getDefault().post(new KeyboardVisibilityChangedEvent(isVisible));
        if (isFragmentViewCreated()) {
            if (stickersFragment.isSearchActive() && isVisible) {
                updateStickersFrameParams(contentContainer.getHeight());
                stickersFragment.setTabsVisible(false);
                stickersFragment.setSwipeEnabled(false);
                stickersFragment.setSearchHeight(actionBarHeight);
                return;

            } else {
                stickersFragment.setTabsVisible(true);
                stickersFragment.setSwipeEnabled(true);
                stickersFragment.setSearchHeight((int) context.getResources().getDimension(R.dimen.material_48));
                updateStickersFrameParams();
            }
            if (isVisible) {
                setStickersFrameVisible(false);
            } else {
                if (isStickersFrameVisible) {
                    stickersButton.setImageResource(keyboardIcon);
                } else {
                    stickersButton.setImageResource(stickersIcon);
                }
            }
        }
        if (!isVisible && keyboardHideCallback != null) {
            keyboardHideCallback.onKeyboardHide();
            keyboardHideCallback = null;
        }
    }

    private void setStickersFrameVisible(boolean isVisible) {
        if (isVisible) {
            stickersButton.setImageResource(keyboardIcon);
        } else {
            stickersButton.setImageResource(stickersIcon);
        }
        stickersFrame.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        isStickersFrameVisible = isVisible;
        updateStickersFrameParams();
        final int padding = isVisible ? KeyboardUtils.getKeyboardHeight(context) : 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            stickersFrame.post(() -> setContentBottomPadding(padding));
        } else {
            setContentBottomPadding(padding);
        }
        if (externalKeyboardVisibilityChangeListener != null) {
            externalKeyboardVisibilityChangeListener.onStickersKeyboardVisibilityChanged(isVisible);
        }
    }

    private void setSuggestContainer(RecyclerView suggestContainer) {
        this.suggestContainer = suggestContainer;
        suggestContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.sp_suggest_container_bg));
        suggestContainer.setVisibility(View.GONE);
        if (adapter == null) {
            adapter = new SuggestedStickersAdapter(context, suggestClickListener);
            suggestContainer.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            suggestContainer.setAdapter(adapter);
        }
    }

    private void updateStickersFrameParams() {
        if (stickersFrame != null && context != null && stickersFrame.getHeight() != KeyboardUtils.getKeyboardHeight(context)) {
            updateStickersFrameParams(KeyboardUtils.getKeyboardHeight(context));
        }
    }

    private void updateStickersFrameParams(int height) {
        if (stickersFrame != null
                && stickersFrame.getLayoutParams().height != height) {
            stickersFrame.getLayoutParams().height = height;
            stickersFrame.requestLayout();
        }
    }

    private void setContentBottomPadding(int padding) {
        contentContainer.setPadding(0, 0, 0, padding);
    }

    private void showKeyboard() {
        chatEdit.requestFocus();
        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(chatEdit, InputMethodManager.SHOW_IMPLICIT);
    }

    public boolean hideStickersKeyboard() {
        if (isStickersFrameVisible) {
            setStickersFrameVisible(false);
            return true;
        } else {
            return false;
        }
    }

    private boolean isFragmentViewCreated() {
        return stickersFragment.getView() != null;
    }

    public void processTabShowIntent() {
        if (isFragmentViewCreated()) {
            if (!isStickersFrameVisible) {
                setStickersFrameVisible(true);
            }
            if (stickersFragment.isAdded()) {
                stickersFragment.selectTabIfNeed();
            }
        }
    }

    public void setKeyboardVisibilityChangeListener(@NonNull KeyboardVisibilityChangeListener listener) {
        this.externalKeyboardVisibilityChangeListener = listener;
    }

    public void setKeyboardVisibilityChangeIntentListener(@NonNull KeyboardVisibilityChangeIntentListener keyboardVisibilityChangeIntentListener) {
        this.keyboardVisibilityChangeIntentListener = keyboardVisibilityChangeIntentListener;
    }


    private void onEditTextChanged(String text) {
        mHandler.removeCallbacks(searchRunnable);
        String[] parts = text.trim().split(" ");
        if (parts.length > 0) {
            currentSuggestSegment = parts[parts.length - 1];
            if (!TextUtils.isEmpty(currentSuggestSegment)) {
                // use runnable for delay handling
                mHandler.postDelayed(searchRunnable, 100);
            } else {
                setSuggestsVisible(false);
            }
        }
    }


    private void setKeyboardIcon(@DrawableRes int keyboardIcon) {
        this.keyboardIcon = keyboardIcon;
    }

    private void setStickersIcon(@DrawableRes int stickersIcon) {
        this.stickersIcon = stickersIcon;
    }

    private SuggestedStickersAdapter adapter;
    private Runnable searchRunnable = () -> {
        NetworkManager.getInstance().requestSearch(currentSuggestSegment, false, true).subscribe(
                result -> {
                    if (result.getData() != null && result.getData().size() > 0) {
                        adapter.setData(result.getData());
                        setSuggestsVisible(true);
                        // store pack names
                        for (SearchResultItem item : result.getData()) {
                            StorageManager.getInstance().storeContentPackName(item.getContentId(), item.getPack());
                        }
                    } else {
                        setSuggestsVisible(false);
                    }
                },
                th -> {
                    Logger.e(TAG, "Can't complete suggest search request", th);
                    setSuggestsVisible(false);
                });
    };

    private interface SuggestStickerClickListener {
        void onSuggestStickerClicked(String contentId);
    }

    private SuggestStickerClickListener suggestClickListener = contentId -> {
        if (isFragmentViewCreated()) {
            currentSuggestSegment = "";
            setSuggestsVisible(false);
            stickersFragment.processSuggestStickerClick(contentId);
        }
    };

    private class SuggestedStickersAdapter extends RecyclerView.Adapter<SuggestedStickersAdapter.ViewHolder> {
        private final int padding;
        private final SuggestStickerClickListener clickListener;
        private PorterDuffColorFilter selectedItemFilterColor;
        private String TAG = SuggestedStickersAdapter.class.getSimpleName();
        private List<SearchResultItem> data = new ArrayList<>();

        public SuggestedStickersAdapter(Context context, SuggestStickerClickListener clickListener) {
            this.clickListener = clickListener;
            padding = context.getResources().getDimensionPixelSize(R.dimen.material_8);
            selectedItemFilterColor = new PorterDuffColorFilter(0xffdddddd, PorterDuff.Mode.MULTIPLY);
        }

        public void setData(List<SearchResultItem> data) {
            if (data != null && !data.equals(this.data)) {
                this.data = data;
                notifyDataSetChanged();
                suggestContainer.scrollToPosition(0);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView iv = new SquareHeightImageView(context);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT);
            iv.setLayoutParams(lp);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            iv.setPadding(padding, padding, padding, padding);
            return new ViewHolder(iv);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            viewHolder.contentId = data.get(position).getContentId();
            Uri uri = getFileUri(viewHolder.contentId, position);
            if (uri != null) {
                Glide.with(context)
                        .load(uri)
                        .apply(new RequestOptions()
                                .placeholder(android.R.color.transparent)
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                        .into(new ImageViewTarget<Drawable>(viewHolder.iv) {
                            @Override
                            protected void setResource(@Nullable Drawable resource) {
                                getView().setImageDrawable(resource);
                            }

                            @Override
                            public void onResourceReady(Drawable resource,
                                                        @Nullable Transition<? super Drawable> transition) {
                                super.onResourceReady(resource, transition);
                                getView().setOnTouchListener(imageTouchListener);
                            }
                        });
            } else {
                viewHolder.iv.setImageResource(android.R.color.transparent);
            }
        }

        View.OnTouchListener imageTouchListener = (v, event) -> {
            if (v instanceof ImageView) {
                ImageView touchedImageView = (ImageView) v;
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        touchedImageView.getDrawable().setColorFilter(selectedItemFilterColor);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        touchedImageView.getDrawable().setColorFilter(null);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        touchedImageView.getDrawable().setColorFilter(null);
                        break;
                    default:
                }
            }
            return false;
        };

        private void notifyItemLoaded(String contentId) {
            int position = 0;
            for (SearchResultItem item : data) {
                if (item.getContentId().equals(contentId)) {
                    notifyItemChanged(position);
                    break;
                }
                position++;
            }
        }

        protected Uri getFileUri(String contentId, int position) {
            File file = StorageManager.getInstance().getImageFile(contentId);
            if (file.exists()) {
                return Uri.fromFile(file);
            } else {
                if (context != null && context != null) {
                    NetworkManager.getInstance().downloadImage(
                            data.get(position).getImage().get(Utils.getDensityName(context)), contentId)
                            .subscribe(
                                    result -> {
                                        if (result) {
                                            notifyItemLoaded(contentId);
                                        }
                                    },
                                    e -> Logger.e(TAG, e)
                            );
                }
            }
            return null;
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView iv;
            private String contentId;

            public ViewHolder(View itemView) {
                super(itemView);
                this.iv = (ImageView) itemView;
                iv.setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onSuggestStickerClicked(contentId);
                    }
                });
            }
        }

    }

    private void setSuggestsVisible(boolean isVisible) {
        if (suggestContainer.getVisibility() == View.GONE && !isVisible
                || suggestContainer.getVisibility() == View.VISIBLE && isVisible) {
            return;
        }
        suggestContainer.animate()
                .alpha(isVisible ? 1 : 0)
                .setDuration(200)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        suggestContainer.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!isVisible) {
                            suggestContainer.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();
    }


    public static final class Builder {
        private final Context context;
        private View contentContainer;
        private View stickersFrame;
        private StickersKeyboardLayout stickersKeyboardLayout;
        private EditText chatEdit;
        private BadgedButton stickersButton;
        private StickersFragment stickersFragment;
        private RecyclerView suggestContainer;
        private int stickersIcon;
        private int keyboardIcon;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setContentContainer(@NonNull View contentContainer) {
            this.contentContainer = contentContainer;
            return this;
        }

        public Builder setStickersFrame(@NonNull View stickersFrame) {
            this.stickersFrame = stickersFrame;
            return this;
        }

        public Builder setStickersKeyboardLayout(@NonNull StickersKeyboardLayout stickersKeyboardLayout) {
            this.stickersKeyboardLayout = stickersKeyboardLayout;
            return this;
        }

        public Builder setChatEdit(@NonNull EditText chatEdit) {
            this.chatEdit = chatEdit;
            return this;
        }

        public Builder setStickersButton(@NonNull BadgedButton stickersButton) {
            this.stickersButton = stickersButton;
            return this;
        }

        public Builder setStickersFragment(@NonNull StickersFragment stickersFragment) {
            this.stickersFragment = stickersFragment;
            return this;
        }

        public Builder setSuggestContainer(RecyclerView suggestContainer) {
            this.suggestContainer = suggestContainer;
            return this;
        }

        public Builder setStickersIcon(@DrawableRes int stickersIcon) {
            this.stickersIcon = stickersIcon;
            return this;
        }

        public Builder setKeyboardIcon(@DrawableRes int keyboardIcon) {
            this.keyboardIcon = keyboardIcon;
            return this;
        }

        public StickersKeyboardController build() {
            StickersKeyboardController controller = new StickersKeyboardController(context);
            if (contentContainer != null) {
                controller.setContentContainer(contentContainer);
            }
            if (stickersFrame != null) {
                controller.setStickersFrame(stickersFrame);
            }
            if (stickersKeyboardLayout != null) {
                controller.setStickersKeyboardLayout(stickersKeyboardLayout);
            }
            if (chatEdit != null) {
                controller.setChatEdit(chatEdit);
            }
            if (stickersButton != null) {
                controller.setStickersButton(stickersButton);
            }
            if (stickersFragment != null) {
                controller.setStickersFragment(stickersFragment);
            }
            if (suggestContainer != null) {
                controller.setSuggestContainer(suggestContainer);
            }
            if (stickersIcon > 0) {
                controller.setStickersIcon(stickersIcon);
            }
            if (keyboardIcon > 0) {
                controller.setKeyboardIcon(keyboardIcon);
            }
            return controller;
        }
    }
}