package vc908.stickerfactory.ui.fragment;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bartoszlipinski.recyclerviewheader2.RecyclerViewHeader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import vc908.stickerfactory.EmojiSettingsBuilder;
import vc908.stickerfactory.R;
import vc908.stickerfactory.StickersManager;
import vc908.stickerfactory.StorageManager;
import vc908.stickerfactory.emoji.Emoji;
import vc908.stickerfactory.emoji.EmojiList;
import vc908.stickerfactory.provider.recentlyemoji.RecentlyEmojiColumns;
import vc908.stickerfactory.provider.recentlyemoji.RecentlyEmojiCursor;
import vc908.stickerfactory.provider.recentlyemoji.RecentlyEmojiSelection;
import vc908.stickerfactory.ui.OnEmojiBackspaceClickListener;
import vc908.stickerfactory.ui.OnStickerSelectedListener;
import vc908.stickerfactory.ui.view.SquareImageView;
import vc908.stickerfactory.ui.view.SquareTextView;
import vc908.stickerfactory.utils.CompatUtils;
import vc908.stickerfactory.utils.Logger;
import vc908.stickerfactory.utils.Utils;

/**
 * Fragment with emoji list
 *
 * @author Dmitry Nezhydenko
 */
public class EmojiFragment extends Fragment {

    private static final String TAG = EmojiFragment.class.getSimpleName();
    private List<OnStickerSelectedListener> stickerSelectedListeners = new ArrayList<>();
    private View layout;
    private int size;
    private OnEmojiBackspaceClickListener emojiBackspaceClickListener;
    private int padding;
    private OnStickerSelectedListener recentEmojiTrackingListener;
    private RecentEmojiAdapter recentEmojiAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (layout == null) {
            layout = inflater.inflate(R.layout.sp_fragment_emoji_list, container, false);
            RecyclerViewHeader header = (RecyclerViewHeader) layout.findViewById(R.id.header);
            RecyclerView rv = (RecyclerView) layout.findViewById(R.id.recycler_view);
            GridView recentGridView = (GridView) layout.findViewById(R.id.grid);
            rv.setHasFixedSize(true);
            size = 28;//(int) Utils.dp(getEmojiSize(People.DATA[0], 10));
            padding = Utils.dp(8, getActivity());
            // calculate emoji columns count
            int minItemSize = Utils.dp(48, getContext());
            int backspaceColumnSize = getResources().getDimensionPixelSize(R.dimen.sp_backspace_column_width);
            int itemsSpanCount = (int) (Math.floor((Utils.getScreenWidthInPx(getContext()) - backspaceColumnSize) / minItemSize));
            int itemWidth = (int) Math.floor(((float) Utils.getScreenWidthInPx(getContext())) / itemsSpanCount);
            int columnsCount = (Utils.getScreenWidthInPx(getContext()) / itemWidth);
            rv.setLayoutManager(new GridLayoutManager(getActivity(), columnsCount));
            header.attachTo(rv);
            if (StickersManager.getEmojiSettingsBuilder() == null) {
                EmojiAdapter adapter = new EmojiAdapter();
                adapter.setHasStableIds(true);
                rv.setAdapter(adapter);
            } else {
                rv.setAdapter(new CustomEmojiAdapter(getContext(), stickerSelectedListeners));
            }
            ImageView backspaceView = (ImageView) layout.findViewById(R.id.clear_button);
            backspaceView.setColorFilter(ContextCompat.getColor(getActivity(), R.color.sp_stickers_backspace));
            backspaceView.getLayoutParams().height = (Utils.getScreenWidthInPx(getContext()) - backspaceColumnSize) / itemsSpanCount;
            backspaceView.setOnClickListener(v -> {
                if (emojiBackspaceClickListener != null) {
                    emojiBackspaceClickListener.onEmojiBackspaceClicked();
                }
            });
            backspaceView.setOnLongClickListener(l -> {
                Utils.copyToClipboard(getActivity(), StorageManager.getInstance().getUserID());
                return true;
            });
            CompatUtils.setBackgroundDrawable(backspaceView, Utils.createSelectableBackground(getActivity()));
            // recent
            recentGridView.setNumColumns(columnsCount);
            int recentEmojiRowsCount = getResources().getInteger(R.integer.recent_emoji_rows_count);
            RecentlyEmojiCursor recentEmojiCursor = new RecentlyEmojiSelection()
                    .query(
                            getActivity().getContentResolver(),
                            null,
                            RecentlyEmojiColumns.LAST_USING_TIME + " DESC LIMIT " + columnsCount * recentEmojiRowsCount);
            List<String> recentEmojiList = new ArrayList<>();
            while (recentEmojiCursor.moveToNext()) {
                recentEmojiList.add(recentEmojiCursor.getCode());
            }
            recentEmojiCursor.close();
            if (recentEmojiAdapter == null) {
                recentEmojiAdapter = new RecentEmojiAdapter(recentEmojiList);
                recentGridView.setAdapter(recentEmojiAdapter);
            }
            if (recentEmojiList.size() > 0) {
                recentEmojiAdapter.notifyDataSetChanged();
            } else {
                layout.findViewById(R.id.recent_divider).setVisibility(View.GONE);
            }
            recentGridView.setOnItemClickListener((parent, view, position, id) -> {
                for (OnStickerSelectedListener listener : stickerSelectedListeners) {
                    listener.onEmojiSelected(recentEmojiList.get(position));
                }
            });
        }
        return layout;
    }

    /**
     * Add emoji selected listener
     *
     * @param stickerSelectedListener Listener
     */
    public void addStickerSelectedListener(OnStickerSelectedListener stickerSelectedListener) {
        stickerSelectedListeners.add(stickerSelectedListener);
    }


    /**
     * Set click listener for backspace icon
     *
     * @param listener Backspace click listener
     */
    public void setOnBackspaceClickListener(OnEmojiBackspaceClickListener listener) {
        this.emojiBackspaceClickListener = listener;
    }

    /**
     * Listener to add selected emoji to recent list. It's separate from
     * common listeners list, becouse we don't want to track clicks on recent
     * items list
     *
     * @param recentEmojiTrackingListener Listener
     */
    public void addRecentTrackingListener(OnStickerSelectedListener recentEmojiTrackingListener) {
        this.recentEmojiTrackingListener = recentEmojiTrackingListener;
    }

    private class RecentEmojiAdapter extends BaseAdapter {
        private List<String> data;
        private EmojiSettingsBuilder settings;

        public RecentEmojiAdapter(List<String> recentEmojiList) {
            this.data = recentEmojiList;
            settings = StickersManager.getEmojiSettingsBuilder();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public String getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (settings != null) {
                ImageView view = createRecentmojiImageView();
                try {
                    String itemKey = settings.getCustomEmojiMap().get(getItem(position));
                    switch (settings.getResourceLocation()) {
                        case DRAWABLE:
                            int imageId = getActivity().getResources().getIdentifier(itemKey, "drawable", getActivity().getPackageName());
                            view.setImageResource(imageId);
                            break;
                        case ASSETS:
                            InputStream is = getActivity().getAssets().open(settings.getAssetsFolder() + itemKey);
                            view.setImageBitmap(BitmapFactory.decodeStream(is));
                        default:
                    }
                } catch (Exception e) {
                    Logger.e(TAG, e);
                }
                return view;
            } else {
                TextView view = createEmojiTextView();
                view.setText(getItem(position));
                return view;
            }
        }
    }

    /**
     * e
     * Adapter for emoji list
     */
    private class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.ViewHolder> {

        private final Emoji[] data;

        public EmojiAdapter() {
            data = EmojiList.DATA;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(createEmojiTextView());
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String code = data[position].getEmoji();
            holder.tv.setText(code);
            holder.code = code;
        }

        @Override
        public int getItemCount() {
            return data.length;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private TextView tv;
            private String code;

            public ViewHolder(View itemView) {
                super(itemView);
                tv = (TextView) itemView;
                tv.setOnClickListener(v -> {
                    if (stickerSelectedListeners.size() > 0) {
                        for (OnStickerSelectedListener listener : stickerSelectedListeners) {
                            listener.onEmojiSelected(code);
                        }
                        recentEmojiTrackingListener.onEmojiSelected(code);
                    }
                });
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    private TextView createEmojiTextView() {
        TextView tv = new SquareTextView(getActivity());
        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tv.setTextSize(size);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(ContextCompat.getColor(getActivity(), android.R.color.black));
        CompatUtils.setBackgroundDrawable(tv, Utils.createSelectableBackground(tv.getContext()));
        return tv;
    }

    private ImageView createEmojiImageView() {
        SquareImageView iv = new SquareImageView(getActivity());
        iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        iv.setPadding(padding, padding, padding, padding);
        return iv;
    }

    private ImageView createRecentmojiImageView() {
        SquareImageView iv = new SquareImageView(getActivity());
        iv.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT));
        iv.setPadding(padding, padding, padding, padding);
        return iv;
    }

    private class CustomEmojiAdapter extends RecyclerView.Adapter<CustomEmojiAdapter.ViewHolder> {
        private final EmojiSettingsBuilder settings;
        private final ArrayList<Pair<String, String>> data = new ArrayList<>();
        private final AssetManager assetManager;
        private final Context mContext;

        private final List<OnStickerSelectedListener> mStickerSelectedListeners;

        CustomEmojiAdapter(Context context, List<OnStickerSelectedListener> stickerSelectedListeners) {
            this.mContext = context;
            this.mStickerSelectedListeners = stickerSelectedListeners;
            settings = StickersManager.getEmojiSettingsBuilder();
            if (settings != null && settings.getCustomEmojiMap() != null) {
                for (Map.Entry<String, String> entry : settings.getCustomEmojiMap().entrySet()) {
                    data.add(new Pair<>(entry.getKey(), entry.getValue()));
                }
            }
            assetManager = context.getAssets();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView view = createEmojiImageView();
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            try {
                Pair<String, String> item = data.get(position);
                switch (settings.getResourceLocation()) {
                    case DRAWABLE:
                        int imageId = mContext.getResources().getIdentifier(item.second, "drawable", mContext.getPackageName());
                        holder.iv.setImageResource(imageId);
                        break;
                    case ASSETS:
                        InputStream is = assetManager.open(settings.getAssetsFolder() + item.second);
                        holder.iv.setImageBitmap(BitmapFactory.decodeStream(is));
                    default:
                }
                holder.code = item.first;

            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView iv;

            private String code;

            public ViewHolder(View itemView) {
                super(itemView);
                iv = (ImageView) itemView;
                iv.setOnClickListener(v -> {
                    if (mStickerSelectedListeners.size() > 0) {
                        for (OnStickerSelectedListener listener : mStickerSelectedListeners) {
                            listener.onEmojiSelected(code);
                        }
                        recentEmojiTrackingListener.onEmojiSelected(code);
                    }
                });
            }
        }

    }
}