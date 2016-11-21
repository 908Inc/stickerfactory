package vc908.stickerfactory.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import vc908.stickerfactory.R;
import vc908.stickerfactory.analytics.AnalyticsManager;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.sp_primary_dark));
        }
        AnalyticsManager.getInstance().onScreenViewed(getScreenName());
    }

    public abstract String getScreenName();
}