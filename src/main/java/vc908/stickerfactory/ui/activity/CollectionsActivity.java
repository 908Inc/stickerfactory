package vc908.stickerfactory.ui.activity;

import android.database.Cursor;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.LegacySwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import java.util.LinkedList;
import java.util.List;

import vc908.stickerfactory.R;
import vc908.stickerfactory.StickersManager;
import vc908.stickerfactory.StorageManager;
import vc908.stickerfactory.TasksManager;
import vc908.stickerfactory.analytics.AnalyticsManager;
import vc908.stickerfactory.provider.packs.PacksColumns;
import vc908.stickerfactory.provider.packs.PacksCursor;
import vc908.stickerfactory.provider.packs.Status;
import vc908.stickerfactory.utils.NamesHelper;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class CollectionsActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private MyDraggableSwipeableItemAdapter myItemAdapter;
    private List<PackInfoHolder> data = new LinkedList<>();
    private Handler mHandler = new Handler();
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sp_activity_more);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
            getSupportActionBar().setTitle(R.string.sp_collections);
        }

        getPacksData();
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mLayoutManager = new LinearLayoutManager(this);

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable((NinePatchDrawable) ContextCompat.getDrawable(this, R.drawable.sp_material_shadow));

        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        myItemAdapter = new MyDraggableSwipeableItemAdapter();
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(myItemAdapter);
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mWrappedAdapter);

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();
        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);
        mRecyclerView.setItemAnimator(animator);

        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(this, R.drawable.sp_list_divider), true));

        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);

    }

    @Override
    public String getScreenName() {
        return "Collections";
    }

    private void getPacksData() {
        data.clear();
        Cursor cursor = getContentResolver().query(
                PacksColumns.CONTENT_URI,
                new String[]{PacksColumns._ID, PacksColumns.NAME, PacksColumns.TITLE, PacksColumns.ARTIST, PacksColumns.LAST_MODIFY_DATE},
                PacksColumns.STATUS + "=?",
                new String[]{String.valueOf(Status.ACTIVE.ordinal())},
                PacksColumns.PACK_ORDER);
        PacksCursor packsCursor = new PacksCursor(cursor);
        while (packsCursor.moveToNext()) {
            data.add(new PackInfoHolder(
                    false,
                    packsCursor.getId(),
                    packsCursor.getName(),
                    packsCursor.getTitle(),
                    packsCursor.getArtist(),
                    packsCursor.getLastModifyDate() != null ? packsCursor.getLastModifyDate() : 0));
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public void onPause() {
        mRecyclerViewDragDropManager.cancelDrag();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }
        if (mRecyclerViewSwipeManager != null) {
            mRecyclerViewSwipeManager.release();
            mRecyclerViewSwipeManager = null;
        }

        if (mRecyclerViewTouchActionGuardManager != null) {
            mRecyclerViewTouchActionGuardManager.release();
            mRecyclerViewTouchActionGuardManager = null;
        }
        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        myItemAdapter = null;
        mLayoutManager = null;

        super.onDestroy();
    }

    public class MyDraggableSwipeableItemAdapter extends RecyclerView.Adapter<MyDraggableSwipeableItemAdapter.MyViewHolder> implements DraggableItemAdapter<MyDraggableSwipeableItemAdapter.MyViewHolder>, LegacySwipeableItemAdapter<MyDraggableSwipeableItemAdapter.MyViewHolder> {
        private static final String TAG = "MyDraggableItemAdapter";

        @Override
        public int onGetSwipeReactionType(MyViewHolder myViewHolder, int position, int x, int y) {
            if (!data.get(position).isSwiped) {
                closeSwipedItems();
            }
            return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_LEFT;
        }

        @Override
        public void onSetSwipeBackground(MyViewHolder viewHolder, int position, int type) {
            switch (type) {
                case RecyclerViewSwipeManager.DRAWABLE_SWIPE_LEFT_BACKGROUND:
                    viewHolder.mRemoveView.setVisibility(View.VISIBLE);
                    break;
                default:
            }
        }

        @Override
        public int onSwipeItem(MyViewHolder myViewHolder, int i, int result) {
            switch (result) {
                case RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT:
                    return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
                case RecyclerViewSwipeManager.RESULT_SWIPED_LEFT:
                    return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION;
                case RecyclerViewSwipeManager.RESULT_CANCELED:
                    return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
                default:
                    return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION;
            }
        }

        @Override
        public void onPerformAfterSwipeReaction(MyViewHolder holder, int position, int result, int reaction) {
            switch (reaction) {
                case RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION:
                    data.get(position).isSwiped = true;
//                    notifyItemChanged(position);
                    break;
                default:
                    data.get(position).isSwiped = false;
            }
        }

        public class MyViewHolder extends AbstractDraggableSwipeableItemViewHolder {
            public View frontViewContainer;
            public ImageView mRemoveView;
            public ViewGroup mContainer;
            public ImageView mDragHandle;
            public ImageView packImageView;
            public TextView titleView;
            public TextView artistView;
            public String packName;

            public MyViewHolder(View v) {
                super(v);
                mContainer = (ViewGroup) v.findViewById(R.id.container);
                frontViewContainer = v.findViewById(R.id.front_view_container);
                // prevent touch event at underlying view
                frontViewContainer.setOnTouchListener((v1, event) -> false);
                mDragHandle = (ImageView) v.findViewById(R.id.drag_handle);
                mDragHandle.setColorFilter(ContextCompat.getColor(CollectionsActivity.this, R.color.sp_reorder_icon));
                packImageView = (ImageView) v.findViewById(R.id.pack_image);
                mRemoveView = (ImageView) v.findViewById(R.id.delete);
                mRemoveView.setColorFilter(ContextCompat.getColor(CollectionsActivity.this, R.color.sp_remove_icon));
                titleView = (TextView) v.findViewById(R.id.pack_title);
                artistView = (TextView) v.findViewById(R.id.pack_artist);
            }

            @Override
            public View getSwipeableContainerView() {
                return frontViewContainer;
            }
        }

        public MyDraggableSwipeableItemAdapter() {
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            return data.get(position).id;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            holder.packName = data.get(position).name;

            Glide.with(CollectionsActivity.this)
                    .load(StorageManager.getInstance().getImageFile(NamesHelper.getMainIconName(holder.packName)))
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(holder.packImageView);

            holder.mRemoveView.setOnClickListener(v -> performDelete(holder, position));
            holder.titleView.setText(data.get(position).title);
            holder.artistView.setText(data.get(position).artist);

            holder.setMaxLeftSwipeAmount(-0.2f);
            holder.setMaxRightSwipeAmount(0);
            holder.setSwipeItemHorizontalSlideAmount(data.get(position).isSwiped ? -0.2f : 0);

            holder.frontViewContainer.setOnLongClickListener(v -> {
                int currentPosition = holder.getAdapterPosition();
                if (data.get(currentPosition).isSwiped) {
                    closeSwipedItems();
                } else {
                    data.get(currentPosition).isSwiped = true;
                    notifyItemChanged(currentPosition);
                }
                return true;
            });
            holder.frontViewContainer.setOnClickListener(v -> {
                closeSwipedItems();
                showStickerPackInfo(holder.packName);

            });
        }

        private void showStickerPackInfo(String packName) {
            StickersManager.showPackInfoByPackName(CollectionsActivity.this, packName);
        }

        public void performDelete(final MyViewHolder holder, final int position) {
            final PackInfoHolder tempItem = data.remove(position);
            tempItem.isSwiped = false;
            final Runnable deletingRunnable = () -> {
                TasksManager.getInstance().addRemovePackTask(holder.packName);
                StorageManager.getInstance().deactivatePack(holder.packName);
                AnalyticsManager.getInstance().onPackDeleted(holder.packName);
            };
            Snackbar.make(mRecyclerView, R.string.sp_package_removed, Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.sp_undo), v -> {
                        mHandler.removeCallbacks(deletingRunnable);
                        data.add(position, tempItem);
                        notifyItemInserted(position);
                    }).show();

            notifyItemRemoved(position);
//              SnackbarManager.SHORT_DURATION_MS(1500) + animation duration + swipe animation duration
            mHandler.postDelayed(deletingRunnable, 2000);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sp_list_item_dragable_pack, parent, false));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public void onMoveItem(int fromPosition, int toPosition) {
            if (fromPosition == toPosition) {
                return;
            }
            PackInfoHolder movedItem = data.remove(fromPosition);
            data.add(toPosition, movedItem);
            notifyItemMoved(fromPosition, toPosition);
            StorageManager.getInstance().updatePacksOrder(toList(data));
        }


        @Override
        public boolean onCheckCanStartDrag(MyViewHolder holder, int position, int x, int y) {
            float deltaX = ViewCompat.getTranslationX(holder.frontViewContainer);
            return (x >= holder.mDragHandle.getLeft() + deltaX)
                    && (x <= holder.mDragHandle.getRight() + deltaX)
                    && (y >= holder.mDragHandle.getTop())
                    && (y <= holder.mDragHandle.getBottom());
        }

        @Override
        public ItemDraggableRange onGetItemDraggableRange(MyViewHolder holder, int position) {
            // no drag-sortable range specified
            return null;
        }
    }

    private List<String> toList(List<PackInfoHolder> data) {
        LinkedList<String> list = new LinkedList<>();
        for (PackInfoHolder info : data) {
            list.add(info.name);
        }
        return list;
    }

    private void closeSwipedItems() {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isSwiped) {
                data.get(i).isSwiped = false;
                myItemAdapter.notifyItemChanged(i);
            }
        }
    }

    public class PackInfoHolder {
        boolean isSwiped;
        long id;
        public String name;
        String title;
        String artist;
        long lastModifyDate;

        public PackInfoHolder(boolean isSwiped, long id, String name, String title, String artist, long lastModifyDate) {
            this.isSwiped = isSwiped;
            this.id = id;
            this.name = name;
            this.title = title;
            this.artist = artist;
            this.lastModifyDate = lastModifyDate;
        }
    }
}
