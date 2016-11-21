package vc908.stickerfactory.ui.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import vc908.stickerfactory.R;
import vc908.stickerfactory.StickersManager;
import vc908.stickerfactory.StorageManager;
import vc908.stickerfactory.analytics.AnalyticsManager;
import vc908.stickerfactory.analytics.IAnalytics;
import vc908.stickerfactory.events.PackTabImageDownloadedEvent;
import vc908.stickerfactory.events.PacksLoadedEvent;
import vc908.stickerfactory.provider.packs.PacksColumns;
import vc908.stickerfactory.provider.packs.PacksCursor;
import vc908.stickerfactory.provider.packs.Status;
import vc908.stickerfactory.ui.OnEmojiBackspaceClickListener;
import vc908.stickerfactory.ui.OnShopButtonClickedListener;
import vc908.stickerfactory.ui.OnStickerFileSelectedListener;
import vc908.stickerfactory.ui.OnStickerSelectedListener;
import vc908.stickerfactory.ui.SimpleStickerSelectedLister;
import vc908.stickerfactory.ui.activity.CollectionsActivity;
import vc908.stickerfactory.ui.view.BadgedShopIcon;
import vc908.stickerfactory.ui.view.BadgedStickersTabIcon;
import vc908.stickerfactory.ui.view.SquareImageView;
import vc908.stickerfactory.ui.view.SwipeToggleViewPager;
import vc908.stickerfactory.utils.CompatUtils;
import vc908.stickerfactory.utils.NamesHelper;
import vc908.stickerfactory.utils.Utils;


/**
 * Fragment with stickers lists
 *
 * @author Dmitry Nezhydenko
 */

public class StickersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAB_EMOJI = "emoji_tab";
    public static final String TAB_RECENT = "recent_tab";
    private static final int PACKS_LOADER_ID = Utils.atomicInteger.incrementAndGet();
    private static final String TAG = StickersFragment.class.getSimpleName();
    public static final String SEARCH_TAB_KEY = "search_tab";

    private TabLayout mSlidingTabLayout;
    private int tabSize;
    private int tabPadding;
    private OnStickerSelectedListener stickerSelectedListener;
    private OnStickerFileSelectedListener onStickerFileSelectedListener;
    private OnEmojiBackspaceClickListener emojiBackspaceClickListener;
    private List<String> stickerTabs = new ArrayList<>();
    private PagerAdapterWithImages mPagerAdapter;

    private View contentView;
    private List<String> firstTabs = new ArrayList<>();
    private SwipeToggleViewPager mViewPager;
    private BadgedShopIcon shopView;
    private String packToSelect;
    private OnShopButtonClickedListener onShopButtonClickedListener;
    private int fulSizeEmptyImageRes;
    private SearchStickersFragment searchStickersfragment;
    private View tabsContainer;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(PACKS_LOADER_ID, null, this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.sp_fragment_stickers, container, false);
        } else if (contentView.getParent() != null) {
            ((ViewGroup) contentView.getParent()).removeView(contentView);
        }
        mSlidingTabLayout = (TabLayout) contentView.findViewById(R.id.sp_sliding_tabs);
        mSlidingTabLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.sp_stickers_tab_bg));
        tabsContainer = contentView.findViewById(R.id.sliding_tabs_container);
        mViewPager = (SwipeToggleViewPager) contentView.findViewById(R.id.view_pager);
        mViewPager.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.sp_stickers_list_bg));
        tabPadding = getResources().getDimensionPixelSize(R.dimen.sp_sticker_tab_padding);
        shopView = (BadgedShopIcon) contentView.findViewById(R.id.btn_shop);
        tabSize = getResources().getDimensionPixelSize(R.dimen.sp_sticker_tab_size);
        initFirstTabs();
        mPagerAdapter = new PagerAdapterWithImages(getChildFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                StorageManager.getInstance().storePackMarkedStatus(stickerTabs.get(position), false);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        updateTabs();
        return contentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        selectTabIfNeed();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(PackTabImageDownloadedEvent event) {
        if (mPagerAdapter != null) {
            mPagerAdapter.reloadPackTabImage(event.getPackName());
        }
    }

    private void initFirstTabs() {
        firstTabs.clear();
        if (StickersManager.isSearchTabEnabled) {
            firstTabs.add(SEARCH_TAB_KEY);
        }
        if (isEmojiAvailable()) {
            firstTabs.add(TAB_EMOJI);
        }
        if (StickersManager.hideEmptyRecentTab) {
            if (StorageManager.recentStickersCount < 0) {
                StorageManager.getInstance().updateRecentStickersCount();
            }
            if (StorageManager.recentStickersCount > 0) {
                firstTabs.add(TAB_RECENT);
            }
        } else {
            firstTabs.add(TAB_RECENT);
        }
        stickerTabs.addAll(firstTabs);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
                PacksColumns.CONTENT_URI,
                new String[]{PacksColumns.NAME, PacksColumns._ID},
                PacksColumns.STATUS + "=?",
                new String[]{String.valueOf(Status.ACTIVE.ordinal())},
                PacksColumns.PACK_ORDER
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // xxx: ugly hack to prevent showing empty stickers list
        mViewPager.setCurrentItem(StickersManager.isSearchTabEnabled ? 1 : 0);

        PacksCursor packsCursor = new PacksCursor(cursor);
        stickerTabs.clear();
        stickerTabs.addAll(firstTabs);
        if (packsCursor.moveToFirst()) {
            do {
                stickerTabs.add(packsCursor.getName());
            } while (packsCursor.moveToNext());
        }
        mPagerAdapter.notifyDataSetChanged();
        updateTabs();
        EventBus.getDefault().post(new PacksLoadedEvent());
    }

    /**
     * Populate tab strip with stickers tabs, control tabs, etc
     */
    private void updateTabs() {
        mSlidingTabLayout.setupWithViewPager(mViewPager);
        for (int i = 0; i < mSlidingTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mSlidingTabLayout.getTabAt(i);
            if (tab != null) tab.setCustomView(mPagerAdapter.getTabView(i));
        }
        mSlidingTabLayout.addTab(createTab(R.drawable.sp_ic_settings, CollectionsActivity.class, tabSize));

        int tabToSelectPosition = firstTabs.indexOf(StickersManager.defaultTab);
        if (tabToSelectPosition < 0) {
            // if recent tab disabled - select firs pack
            tabToSelectPosition = firstTabs.size();
        }
        if (!TextUtils.isEmpty(packToSelect)) {
            int foundedPosition = stickerTabs.indexOf(packToSelect);
            if (foundedPosition >= 0) {
                tabToSelectPosition = foundedPosition;
            }
            packToSelect = null;
        }
        mViewPager.setCurrentItem(tabToSelectPosition);

        if (StickersManager.isShopEnabled) {
            int shopDividerWidth = (int) getActivity().getResources().getDimension(R.dimen.sp_sticker_shop_divider);
            // create empty for tabs
            TabLayout.Tab emptyTab = mSlidingTabLayout.newTab();
            View emptyView = new View(getActivity());
            emptyView.setMinimumWidth(tabSize + shopDividerWidth);
            emptyTab.setCustomView(emptyView);
            // prevent all touches from empty tab
            emptyView.setOnTouchListener((v, event) -> true);
            mSlidingTabLayout.addTab(emptyTab);
            shopView.setColorFilter(ContextCompat.getColor(getContext(), R.color.sp_stickers_tab_icons_filter), PorterDuff.Mode.SRC_IN);
            CompatUtils.setBackgroundDrawable(shopView,
                    Utils.createColorsSelector(
                            ContextCompat.getColor(getActivity(), R.color.sp_stickers_tab_bg),
                            Utils.blendColors(
                                    ContextCompat.getColor(getActivity(), R.color.sp_stickers_tab_bg),
                                    Color.parseColor("#ffffff"),
                                    0.8f
                            )
                    ));
            shopView.setOnClickListener(v -> showShop());
            shopView.setVisibility(View.VISIBLE);
            shopView.updateBadgeStatus();
        } else {
            shopView.setVisibility(View.GONE);
        }
    }

    public void selectTabIfNeed() {
        String packToShow = StorageManager.getInstance().getPackToShowName();
        if (!TextUtils.isEmpty(packToShow)) {
            packToSelect = packToShow;
            int tabPosition = stickerTabs.indexOf(packToShow);
            if (tabPosition >= 0) {
                mViewPager.setCurrentItem(tabPosition);
            }
            StorageManager.getInstance().clearPackToShowName();
        }
    }

    private void showShop() {
        shopView.setIsMarked(false);
        StorageManager.getInstance().storeShopVisited();
        if (onShopButtonClickedListener != null) {
            onShopButtonClickedListener.onShopButtonClicked();
        }
        getActivity().startActivity(new Intent(getActivity(), StickersManager.shopClass));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // nothing to do
    }

    public TabLayout.Tab createTab(@DrawableRes int icon, Class activityClass, int tabSize) {
        ImageView tabView = new SquareImageView(getActivity());
        tabView.setMinimumWidth(tabSize);
        tabView.setImageResource(icon);
        tabView.setPadding(tabPadding, tabPadding, tabPadding, tabPadding);
        tabView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), activityClass);
            getActivity().startActivity(intent);
        });
        tabView.setColorFilter(ContextCompat.getColor(getActivity(), R.color.sp_stickers_tab_icons_filter));
        tabView.setBackgroundResource(R.drawable.sp_tab_bg_selector);
        TabLayout.Tab tab = mSlidingTabLayout.newTab();
        tab.setCustomView(tabView);
        return tab;
    }

    /**
     * Set listener for sticker selection
     *
     * @param listener Sticker select listener
     */
    public void setOnStickerSelectedListener(OnStickerSelectedListener listener) {
        this.stickerSelectedListener = listener;
    }

    /**
     * Set file listener for selected sticker
     *
     * @param listener File listener
     */
    public void setOnstickerSelectedFileListenr(OnStickerFileSelectedListener listener) {
        this.onStickerFileSelectedListener = listener;
    }

    public void setOnShopButtonCickedListener(OnShopButtonClickedListener listener) {
        this.onShopButtonClickedListener = listener;
    }

    /**
     * Set listener for emoji sp_tab backspace icon
     *
     * @param listener Backspace click listener
     */
    public void setOnEmojiBackspaceClickListener(OnEmojiBackspaceClickListener listener) {
        this.emojiBackspaceClickListener = listener;
    }

    /**
     * Set full size image res for empty tab
     *
     * @param imageResId Drawable res id
     */
    public void setFullSizeEmptyImage(@DrawableRes int imageResId) {
        this.fulSizeEmptyImageRes = imageResId;
    }

    /**
     * Listener for stickers selecting from tab
     */
    private OnStickerSelectedListener analyticsTabStickerSelectedListener = new SimpleStickerSelectedLister() {
        @Override
        public void onStickerSelected(String code) {
            addStickerSelectedEvent(code, IAnalytics.Action.SOURCE_TAB);
        }
    };

    /**
     * Listener for stickers selecting from recent tab
     */
    private OnStickerSelectedListener analyticsRecentStickerSelectedListener = new SimpleStickerSelectedLister() {
        @Override
        public void onStickerSelected(String code) {
            addStickerSelectedEvent(code, IAnalytics.Action.SOURCE_RECENT);
        }
    };

    /**
     * Listener for emoji selecting
     */
    private OnStickerSelectedListener analyticsEmojiSelectedListener = new SimpleStickerSelectedLister() {

        @Override
        public void onEmojiSelected(String emoji) {
            AnalyticsManager.getInstance().onEmojiSelected(emoji);
        }
    };

    /**
     * Listener for stickers selecting from search
     */
    private OnStickerSelectedListener analyticsSearchStickersSelectedListener = new SimpleStickerSelectedLister() {
        @Override
        public void onStickerSelected(String code) {
            addStickerSelectedEvent(code, IAnalytics.Action.SOURCE_SEARCH);
        }
    };

    /**
     * Add stickers selected event to statistic
     *
     * @param code   Sticker code
     * @param source Sticker selected source
     */
    private void addStickerSelectedEvent(String code, IAnalytics.Action source) {
        AnalyticsManager.getInstance().onStickerSelected(NamesHelper.getContentIdFromCode(code), source);
    }

    /**
     * Listener for stickers selection and updating last using time
     */
    private OnStickerSelectedListener recentStickersTrackingListener = new SimpleStickerSelectedLister() {
        @Override
        public void onStickerSelected(String code) {
            StorageManager.getInstance().updateStickerUsingTime(NamesHelper.getContentIdFromCode(code));
        }
    };

    /**
     * Listener for emoji selection and updating last using time
     */
    private OnStickerSelectedListener recentEmojiTrackingListener = new SimpleStickerSelectedLister() {
        @Override
        public void onEmojiSelected(String code) {
            StorageManager.getInstance().updateEmojiUsingTime(code);
        }
    };

    /**
     * Change tabs visibility
     *
     * @param isVisible Is tab visible
     */
    public void setTabsVisible(boolean isVisible) {
        tabsContainer.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private View.OnClickListener externalSearchEditClickListener;

    public void setExternalSearchEditClickListener(View.OnClickListener externalSearchEditClickListener) {
        this.externalSearchEditClickListener = externalSearchEditClickListener;
    }

    private View.OnClickListener searchEditClickListener = v -> {
        if (externalSearchEditClickListener != null) {
            externalSearchEditClickListener.onClick(v);
        }
    };

    /**
     * Stickers pager adapter.
     * Handle stickers fragment and sp_tab images
     */
    private class PagerAdapterWithImages extends FragmentStatePagerAdapter {
        private final Drawable tabPlaceholderDrawable;
        Map<String, WeakReference<BadgedStickersTabIcon>> tabs = new HashMap<>();

        public PagerAdapterWithImages(FragmentManager childFragmentManager) {
            super(childFragmentManager);
            tabPlaceholderDrawable = ContextCompat.getDrawable(getContext(), R.drawable.sp_tab_placeholder_default);
            if (tabPlaceholderDrawable != null) {
                tabPlaceholderDrawable.setColorFilter(ContextCompat.getColor(getContext(), R.color.sp_stickers_tab_icons_filter), PorterDuff.Mode.SRC_IN);
            }
        }

        @Override
        public int getCount() {
            return stickerTabs.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            String packName = stickerTabs.get(position);
            if (SEARCH_TAB_KEY.equals(packName)) {
                return getSearchFragment();
            } else if (TAB_EMOJI.equals(packName)) {
                return getEmojiFragmnet();
            } else if (TAB_RECENT.equals(packName)) {
                return getRecentStickersFragment();
            } else {
                return getStickersFragment(packName);
            }
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        private SearchStickersFragment getSearchFragment() {
            searchStickersfragment = new SearchStickersFragment();
            if (stickerSelectedListener != null) {
                searchStickersfragment.addStickerSelectedListener(stickerSelectedListener);
            }
            if (onStickerFileSelectedListener != null) {
                searchStickersfragment.setStickerFileSelectedListener(onStickerFileSelectedListener);
            }
            searchStickersfragment.addStickerSelectedListener(recentStickersTrackingListener);
            searchStickersfragment.addStickerSelectedListener(analyticsSearchStickersSelectedListener);
            searchStickersfragment.setExternalSearchEditClickListener(searchEditClickListener);
            return searchStickersfragment;
        }

        private Fragment getEmojiFragmnet() {
            EmojiFragment emojiFragment = new EmojiFragment();
            if (stickerSelectedListener != null) {
                emojiFragment.addStickerSelectedListener(stickerSelectedListener);
            }
            emojiFragment.addRecentTrackingListener(recentEmojiTrackingListener);
            emojiFragment.addStickerSelectedListener(analyticsEmojiSelectedListener);
            emojiFragment.setOnBackspaceClickListener(emojiBackspaceClickListener);
            return emojiFragment;
        }

        private Fragment getRecentStickersFragment() {
            RecentStickersFragment recentStickersfragment = new RecentStickersFragment();
            if (stickerSelectedListener != null) {
                recentStickersfragment.addStickerSelectedListener(stickerSelectedListener);
            }
            if (onStickerFileSelectedListener != null) {
                recentStickersfragment.setStickerFileSelectedListener(onStickerFileSelectedListener);
            }
            if (fulSizeEmptyImageRes > 0) {
                recentStickersfragment.setFullSizeImageRes(fulSizeEmptyImageRes);
            }
            recentStickersfragment.addStickerSelectedListener(analyticsRecentStickerSelectedListener);
            return recentStickersfragment;
        }

        private Fragment getStickersFragment(String packName) {
            Bundle data = new Bundle();
            data.putString(StickersListFragment.ARGUMENT_PACK, packName);
            StickersListFragment stickersListFragment = new StickersListFragment();
            stickersListFragment.setArguments(data);
            if (stickerSelectedListener != null) {
                stickersListFragment.addStickerSelectedListener(stickerSelectedListener);
            }
            if (onStickerFileSelectedListener != null) {
                stickersListFragment.setStickerFileSelectedListener(onStickerFileSelectedListener);
            }
            stickersListFragment.addStickerSelectedListener(recentStickersTrackingListener);
            stickersListFragment.addStickerSelectedListener(analyticsTabStickerSelectedListener);
            return stickersListFragment;
        }

        public int getTabImageColorFilter(int position) {
            String packName = stickerTabs.get(position);
            if (SEARCH_TAB_KEY.equals(packName) || TAB_EMOJI.equals(packName) || TAB_RECENT.equals(packName)) {
                return R.color.sp_stickers_tab_icons_filter;
            } else {
                return 0;
            }
        }

        public String getPackName(int position) {
            return stickerTabs.get(position);
        }

        public View getTabView(int position) {
            BadgedStickersTabIcon tabView = new BadgedStickersTabIcon(getContext());
            tabView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            tabView.setPadding(tabPadding, tabPadding, tabPadding, tabPadding);
            int size = getResources().getDimensionPixelSize(R.dimen.sp_sticker_tab_size);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(size, size);
            tabView.setLayoutParams(lp);

            String packName = getPackName(position);
            if (SEARCH_TAB_KEY.equals(packName)) {
                tabView.setImageResource(R.drawable.sp_ic_search);
            } else if (TAB_EMOJI.equals(packName)) {
                tabView.setImageResource(R.drawable.sp_ic_emoji);
            } else if (TAB_RECENT.equals(packName)) {
                tabView.setImageResource(R.drawable.sp_ic_recent);
            } else {
                showTabImage(packName, tabView);
                tabs.put(packName, new WeakReference<>(tabView));
            }
            if (getTabImageColorFilter(position) != 0) {
                tabView.setColorFilter(ContextCompat.getColor(getContext(), getTabImageColorFilter(position)));
            }
            tabView.setPackName(packName);
            tabView.setOnClickListener(v -> mViewPager.setCurrentItem(position));
            tabView.setBackgroundResource(R.drawable.sp_tab_bg_selector);
            return tabView;
        }

        private void showTabImage(String packName, BadgedStickersTabIcon tabView) {
            File tabFile = StorageManager.getInstance().getImageFile(NamesHelper.getTabIconName(packName));
            if (tabFile != null && tabFile.exists()) {
                Glide.with(StickersFragment.this)
                        .load(tabFile)
                        .placeholder(tabPlaceholderDrawable)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(tabView);
            } else {
                tabView.setImageDrawable(tabPlaceholderDrawable);
            }
        }

        public void reloadPackTabImage(String packName) {
            if (!TextUtils.isEmpty(packName) && tabs.get(packName) != null) {
                BadgedStickersTabIcon tabView = tabs.get(packName).get();
                if (tabView != null) {
                    showTabImage(packName, tabView);
                }
            }
        }
    }

    /**
     * Check is emoji available for current platform
     *
     * @return Result of check
     */
    private boolean isEmojiAvailable() {
        return StickersManager.isEmojiTabEnabled &&
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT || StickersManager.getEmojiSettingsBuilder() != null);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (contentView != null && contentView.getParent() != null) {
            ((ViewGroup) contentView.getParent()).removeView(contentView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        contentView = null;
    }

    public boolean isSearchActive() {
        return StickersManager.isSearchTabEnabled && mViewPager.getCurrentItem() == firstTabs.indexOf(SEARCH_TAB_KEY) && isSearchFieldActive();
    }

    private boolean isSearchFieldActive() {
        return searchStickersfragment != null && searchStickersfragment.isSearchFieldActive();
    }

    public void setSwipeEnabled(boolean isEnabled) {
        if (mViewPager != null) {
            mViewPager.setSwipeEnable(isEnabled);
        }
    }

    public void setSearchHeight(int height) {
        if (searchStickersfragment != null) {
            searchStickersfragment.setSearchHeight(height);
        }
    }

    public void processSuggestStickerClick(String contentId) {
        if (stickerSelectedListener != null) {
            stickerSelectedListener.onStickerSelected(NamesHelper.getStickerCode(contentId));
        }
        recentStickersTrackingListener.onStickerSelected(contentId);
        AnalyticsManager.getInstance().onStickerSelected(contentId, IAnalytics.Action.SOURCE_SUGGEST);
    }
}