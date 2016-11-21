package vc908.stickerfactory;

import android.app.AlarmManager;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for starting and interact with background job
 *
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class JobScheduler {
    private static JobScheduler instance;


    private Handler handler = new Handler();

    private List<Job> jobs = new ArrayList<>();

    private Runnable sendAnalyticsTask = new Runnable() {
        @Override
        public void run() {
            StorageManager.getInstance().sendAnalyticsEvents();
            handler.postDelayed(this, AlarmManager.INTERVAL_FIFTEEN_MINUTES);
        }
    };

    private Runnable checkPackUpdatesTask = new Runnable() {
        @Override
        public void run() {
            NetworkManager.getInstance().checkPackUpdates();
            handler.postDelayed(this, AlarmManager.INTERVAL_HALF_DAY);
        }
    };

    private JobScheduler() {
        jobs.add(new Job(sendAnalyticsTask, 3000));
        jobs.add(new Job(checkPackUpdatesTask, 0));
    }

    public static JobScheduler getInstance() {
        if (instance == null) {
            instance = new JobScheduler();
        }
        return instance;
    }

    public void start() {
        for (Job job : jobs) {
            job.start();
        }
    }

    private class Job {

        private Runnable runnable;
        private final long startDelay;

        public Job(Runnable runnable, long startDelay) {
            this.runnable = runnable;
            this.startDelay = startDelay;
        }

        public void start() {
            handler.postDelayed(runnable, startDelay);
        }
    }
}
