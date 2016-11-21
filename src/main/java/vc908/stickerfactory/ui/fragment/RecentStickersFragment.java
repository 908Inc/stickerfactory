package vc908.stickerfactory.ui.fragment;

import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import de.greenrobot.event.EventBus;
import vc908.stickerfactory.Constants;
import vc908.stickerfactory.R;
import vc908.stickerfactory.StorageManager;
import vc908.stickerfactory.events.RecentStickersCountChangedEvent;
import vc908.stickerfactory.provider.recentlystickers.RecentlyStickersColumns;

/**
 * Recent stickers list
 *
 * @author Dmitry Nezhydenko
 */
public class RecentStickersFragment extends StickersListFragment {

    private View emptyView;
    private ImageView emptyImage;
    private
    @DrawableRes
    int fullSizeEmptyImageRes;
    private ImageView fullSizeEmptyImage;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = super.onCreateView(inflater, container, savedInstanceState);
        if (layout != null) {
            emptyView = layout.findViewById(R.id.empty_view);
            emptyImage = (ImageView) layout.findViewById(R.id.empty_image);
            fullSizeEmptyImage = (ImageView) layout.findViewById(R.id.sp_full_size_empty_image);
        }
        return layout;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                RecentlyStickersColumns.CONTENT_URI,
                new String[]{RecentlyStickersColumns._ID, RecentlyStickersColumns.CONTENT_ID, RecentlyStickersColumns.LAST_USING_TIME},
                null,
                null,
                RecentlyStickersColumns.LAST_USING_TIME + " DESC LIMIT " + Constants.RECENT_STICKERS_COUNT
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        super.onLoadFinished(loader, cursor);
        updateEmptyViewState();
        if (StorageManager.recentStickersCount != cursor.getCount()) {
            StorageManager.recentStickersCount = cursor.getCount();
            EventBus.getDefault().post(new RecentStickersCountChangedEvent());
        }
        progress.setVisibility(View.GONE);
    }

    private void updateEmptyViewState() {
        if (emptyView != null && getActivity() != null) {
            if (adapter.getItemCount() > 0) {
                emptyView.setVisibility(View.GONE);
                fullSizeEmptyImage.setVisibility(View.GONE);
            } else {
                if (fullSizeEmptyImageRes > 0) {
                    fullSizeEmptyImage.setImageResource(fullSizeEmptyImageRes);
                    fullSizeEmptyImage.setVisibility(View.VISIBLE);
                } else {
                    emptyView.setVisibility(View.VISIBLE);
                    emptyImage.setColorFilter(ContextCompat.getColor(getActivity(), R.color.sp_stickers_empty_image_filter), PorterDuff.Mode.SRC_IN);
                }
            }
        }

    }

    @Override
    protected int getLayoutId() {
        return R.layout.sp_fragment_recent_stickers_list;
    }

    public void setFullSizeImageRes(@DrawableRes int fullSizeEmptyImageRes) {
        this.fullSizeEmptyImageRes = fullSizeEmptyImageRes;
    }
}
