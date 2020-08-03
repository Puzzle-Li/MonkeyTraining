package mymou.task.individual_tasks;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;
import android.view.View;

import androidx.fragment.app.Fragment;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;

/**
 * Parent Task Fragment
 *
 * All tasks must inherit from this Fragment
 * Enables communiation with TaskManager parent
 *
 */
public abstract class Task extends Fragment {

    public abstract void setFragInterfaceListener(TaskInterface callback);



    public void endOfTrial(boolean successfulTrial, double rew_scalar, TaskInterface callback, PreferencesManager preferencesManager) {
        String outcome;
        if (successfulTrial) {
            outcome = PreferencesManager.ec_correct_trial;
        } else {
            outcome = PreferencesManager.ec_incorrect_trial;
        }
        // Send outcome up to parent
        callback.trialEnded_(outcome, rew_scalar);
    }

     public void endOfTrial(boolean successfulTrial, TaskInterface callback) {
        String outcome;
        if (successfulTrial) {
            outcome = PreferencesManager.ec_correct_trial;
        } else {
            outcome = PreferencesManager.ec_incorrect_trial;
        }
        // Send outcome up to parent
        callback.trialEnded_(outcome, 1);
    }

    public void touchPrevention(View view, TaskInterface callback) {
        String outcome = PreferencesManager.ec_wrong_gocue_pressed;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.trialEnded_(outcome, 1);
            }
        });
    }

    public void logEvent(String event, TaskInterface callback) {
        callback.logEvent_(event);
    }



}
