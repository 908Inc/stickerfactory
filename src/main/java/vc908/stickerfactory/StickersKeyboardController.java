package vc908.stickerfactory;

import android.animation.Animator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
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
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ImageViewTarget;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import vc908.stickerfactory.events.KeyboardVisibilityChangedEvent;
import vc908.stickerfactory.model.SearchResultItem;
import vc908.stickerfactory.ui.fragment.StickersFragment;
import vc908.stickerfactory.ui.view.BadgedStickersButton;
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
    private WeakReference<Context> contextReference;
    private WeakReference<View> contentContainer;
    private WeakReference<View> stickersFrame;
    private WeakReference<StickersKeyboardLayout> stickersKeyboardLayout;
    private WeakReference<BadgedStickersButton> stickersButton;
    private WeakReference<EditText> chatEdit;
    private WeakReference<StickersFragment> stickersFragment;
    private WeakReference<RecyclerView> suggestContainer;
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

    private StickersKeyboardController(Context contextReference) {
        this.contextReference = new WeakReference<>(contextReference);
        TypedValue tv = new TypedValue();
        if (contextReference.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, contextReference.getResources().getDisplayMetrics());
        } else {
            actionBarHeight = Utils.dp(48, contextReference);
        }
    }

    private void setContentContainer(View contentContainer) {
        this.contentContainer = new WeakReference<>(contentContainer);
    }

    private void setStickersFrame(View stickersFrame) {
        this.stickersFrame = new WeakReference<>(stickersFrame);
    }

    private void setStickersKeyboardLayout(StickersKeyboardLayout stickersKeyboardLayout) {
        this.stickersKeyboardLayout = new WeakReference<>(stickersKeyboardLayout);
        stickersKeyboardLayout.setKeyboardVisibilityChangeListener(keyboardVisibilityChangeListener);
    }

    private void setChatEdit(EditText chatEdit) {
        this.chatEdit = new WeakReference<>(chatEdit);
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


    private void setStickersButton(BadgedStickersButton stickersButton) {
        this.stickersButton = new WeakReference<>(stickersButton);
        if (contextReference != null && contextReference.get() != null) {
            stickersButton.setOnClickListener(v -> {
                        if (keyboardVisibilityChangeIntentListener != null) {
                            keyboardVisibilityChangeIntentListener.onKeyboardVisibilityChangeIntent();
                        }
                        if (isStickersFrameVisible) {
                            showKeyboard();
                        } else {
                            if (stickersKeyboardLayout != null && stickersKeyboardLayout.get() != null)
                                if (stickersKeyboardLayout.get().isKeyboardVisible()) {
                                    if (contextReference != null && contextReference.get() != null) {
                                        hideKeyboard(contextReference.get(), () -> setStickersFrameVisible(true));
                                    }
                                } else {
                                    setStickersFrameVisible(true);
                                }
                        }
                    }
            );
        }
    }

    private void setStickersFragment(StickersFragment stickersFragment) {
        this.stickersFragment = new WeakReference<>(stickersFragment);
        stickersFragment.setOnEmojiBackspaceClickListener(() -> {
                    KeyEvent event = new KeyEvent(
                            0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                    if (chatEdit != null && chatEdit.get() != null) {
                        chatEdit.get().dispatchKeyEvent(event);
                    }
                }
        );
        stickersFragment.setExternalSearchEditClickListener(searchEditClickListener);
    }

    private View.OnClickListener searchEditClickListener = v -> {
        if (keyboardVisibilityChangeIntentListener != null) {
            keyboardVisibilityChangeIntentListener.onKeyboardVisibilityChangeIntent();
        }
    };

    private void hideKeyboard(@NonNull Context context, @NonNull StickersKeyboardLayout.KeyboardHideCallback callback) {
        if (stickersKeyboardLayout != null && stickersKeyboardLayout.get() != null) {
            if (stickersKeyboardLayout.get().isKeyboardVisible()) {
                keyboardHideCallback = callback;
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (!imm.isActive()) {
                    return;
                }
                if (stickersFrame != null && stickersFrame.get() != null) {
                    imm.hideSoftInputFromWindow(stickersFrame.get().getWindowToken(), 0);
                }
            } else {
                callback.onKeyboardHide();
            }
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
        if (contextReference != null && contextReference.get() != null
                && isFragmentViewCreated()) {
            if (stickersFragment.get().isSearchActive() && isVisible) {
                if (contentContainer != null && contentContainer.get() != null) {
                    updateStickersFrameParams(contentContainer.get().getHeight());
                }
                stickersFragment.get().setTabsVisible(false);
                stickersFragment.get().setSwipeEnabled(false);
                stickersFragment.get().setSearchHeight(actionBarHeight);
                return;

            } else {
                stickersFragment.get().setTabsVisible(true);
                stickersFragment.get().setSwipeEnabled(true);
                stickersFragment.get().setSearchHeight((int) contextReference.get().getResources().getDimension(R.dimen.material_48));
                updateStickersFrameParams();
            }
            if (isVisible) {
                setStickersFrameVisible(false);
            } else {
                if (stickersButton != null && stickersButton.get() != null) {
                    if (isStickersFrameVisible) {
                        stickersButton.get().setImageResource(keyboardIcon);
                    } else {
                        stickersButton.get().setImageResource(stickersIcon);
                    }
                }
            }
        }
        if (!isVisible && keyboardHideCallback != null) {
            keyboardHideCallback.onKeyboardHide();
            keyboardHideCallback = null;
        }
    }

    private void setStickersFrameVisible(boolean isVisible) {
        if (stickersButton != null && stickersButton.get() != null
                && stickersFrame != null && stickersFrame.get() != null) {
            if (isVisible) {
                stickersButton.get().setImageResource(keyboardIcon);
            } else {
                stickersButton.get().setImageResource(stickersIcon);
            }
            stickersFrame.get().setVisibility(isVisible ? View.VISIBLE : View.GONE);
            isStickersFrameVisible = isVisible;
            updateStickersFrameParams();
            final int padding = isVisible ? KeyboardUtils.getKeyboardHeight(contextReference.get()) : 0;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                stickersFrame.get().post(() -> setContentBottomPadding(padding));
            } else {
                setContentBottomPadding(padding);
            }
            if (externalKeyboardVisibilityChangeListener != null) {
                externalKeyboardVisibilityChangeListener.onStickersKeyboardVisibilityChanged(isVisible);
            }
        }
    }

    private void setSuggestContainer(RecyclerView suggestContainer) {
        this.suggestContainer = new WeakReference<>(suggestContainer);
        if (contextReference != null && contextReference.get() != null) {
            suggestContainer.setBackgroundColor(ContextCompat.getColor(contextReference.get(), R.color.sp_suggest_container_bg));
            suggestContainer.setVisibility(View.GONE);
            if (adapter == null) {
                adapter = new SuggestedStickersAdapter(contextReference.get(), suggestClickListener);
                suggestContainer.setLayoutManager(new LinearLayoutManager(contextReference.get(), LinearLayoutManager.HORIZONTAL, false));
                suggestContainer.setAdapter(adapter);
            }
        }
    }

    private void updateStickersFrameParams() {
        if (stickersFrame != null && stickersFrame.get() != null
                && contextReference != null && contextReference.get() != null
                && stickersFrame.get().getHeight() != KeyboardUtils.getKeyboardHeight(contextReference.get())) {
            updateStickersFrameParams(KeyboardUtils.getKeyboardHeight(contextReference.get()));
        }
    }

    private void updateStickersFrameParams(int height) {
        if (stickersFrame != null
                && stickersFrame.get() != null
                && stickersFrame.get().getLayoutParams().height != height) {
            stickersFrame.get().getLayoutParams().height = height;
            stickersFrame.get().requestLayout();
        }
    }

    private void setContentBottomPadding(int padding) {
        if (contentContainer != null && contentContainer.get() != null) {
            contentContainer.get().setPadding(0, 0, 0, padding);
        }
    }

    private void showKeyboard() {
        if (contextReference != null && contextReference.get() != null
                && chatEdit != null && chatEdit.get() != null) {
            chatEdit.get().requestFocus();
            ((InputMethodManager) contextReference.get().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .showSoftInput(chatEdit.get(), InputMethodManager.SHOW_IMPLICIT);
        }
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
        return stickersFragment != null && stickersFragment.get() != null && stickersFragment.get().getView() != null;
    }

    public void processTabShowIntent() {
        if (isFragmentViewCreated()) {
            if (!isStickersFrameVisible) {
                setStickersFrameVisible(true);
            }
            if (stickersFragment.get().isAdded()) {
                stickersFragment.get().selectTabIfNeed();
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
        if (suggestContainer != null && suggestContainer.get() != null) {
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
        }
    };

    private interface SuggestStickerClickListener {
        void onSuggestStickerClicked(String contentId);
    }

    private SuggestStickerClickListener suggestClickListener = contentId -> {
        if (isFragmentViewCreated()) {
            currentSuggestSegment = "";
            setSuggestsVisible(false);
            stickersFragment.get().processSuggestStickerClick(contentId);
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
                if (suggestContainer != null && suggestContainer.get() != null) {
                    suggestContainer.get().scrollToPosition(0);
                }
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (contextReference != null && contextReference.get() != null) {
                ImageView iv = new SquareHeightImageView(contextReference.get());
                RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT);
                iv.setLayoutParams(lp);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                iv.setPadding(padding, padding, padding, padding);
                return new ViewHolder(iv);
            } else {
                return null;
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            if (viewHolder != null && contextReference != null && contextReference.get() != null) {
                viewHolder.contentId = data.get(position).getContentId();
                Uri uri = getFileUri(viewHolder.contentId, position);
                if (uri != null) {
                    Glide.with(contextReference.get())
                            .load(uri)
                            .placeholder(android.R.color.transparent)
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(new ImageViewTarget<GlideDrawable>(viewHolder.iv) {
                                @Override
                                public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                                    super.onResourceReady(resource, glideAnimation);
                                    getView().setOnTouchListener(imageTouchListener);
                                }

                                @Override
                                protected void setResource(GlideDrawable resource) {
                                    getView().setImageDrawable(resource);
                                }
                            });
                } else {
                    viewHolder.iv.setImageResource(android.R.color.transparent);
                }
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
                if (contextReference != null && contextReference.get() != null) {
                    NetworkManager.getInstance().downloadImage(
                            data.get(position).getImage().get(Utils.getDensityName(contextReference.get())), contentId)
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
        if (suggestContainer != null && suggestContainer.get() != null) {
            if (suggestContainer.get().getVisibility() == View.GONE && !isVisible
                    || suggestContainer.get().getVisibility() == View.VISIBLE && isVisible) {
                return;
            }
            suggestContainer.get()
                    .animate()
                    .alpha(isVisible ? 1 : 0)
                    .setDuration(200)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            suggestContainer.get().setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (!isVisible && suggestContainer != null && suggestContainer.get() != null) {
                                suggestContainer.get().setVisibility(View.GONE);
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
    }


    public static final class Builder {
        private final Context context;
        private View contentContainer;
        private View stickersFrame;
        private StickersKeyboardLayout stickersKeyboardLayout;
        private EditText chatEdit;
        private BadgedStickersButton stickersButton;
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

        public Builder setStickersButton(@NonNull BadgedStickersButton stickersButton) {
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