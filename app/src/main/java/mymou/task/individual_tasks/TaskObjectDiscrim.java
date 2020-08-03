package mymou.task.individual_tasks;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Random;

import mymou.R;
import mymou.Utils.UtilsSystem;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

/**
 * Task Object Discrimination (stimulus)
 * <p>
 * Subjects shown a stimuli, and then must choose them from up to 3 distractors
 * <p>
 * Stimuli are taken from Brady, T. F., Konkle, T., Alvarez, G. A. and Oliva, A. (2008). Visual
 * long-term memory has a massive storage capacity for object details. Proceedings of the National
 * Academy of Sciences, USA, 105 (38), 14325-14329.
 * <p>
 * TODO: Implement logging of task variables
 */
public class TaskObjectDiscrim extends Task {

    // Debug
    public static String TAG = "TaskObjectDiscrim";

    private static ImageButton[] cues, choice_cues;
    private static int chosen_cue_id;
    private static ConstraintLayout layout;
    private static PreferencesManager prefManager;
    private static Handler h0 = new Handler();  // Show object
    private static Handler h1 = new Handler();  // Hide object
    private static Handler h2 = new Handler();  // Show choices

    // The stimuli
    private static int[] stims = {
            R.drawable.aabaa,
            R.drawable.aabab,
            R.drawable.aabac,
            R.drawable.aabad,
            R.drawable.aabae,
            R.drawable.aabaf,
            R.drawable.aabag,
            R.drawable.aabah,
            R.drawable.aabai,
            R.drawable.aabaj,
            R.drawable.aabak,
            R.drawable.aabal,
            R.drawable.aabam,
            R.drawable.aaban,
            R.drawable.aabao,
            R.drawable.aabap,
            R.drawable.aaaaa,
            R.drawable.aaaab,
            R.drawable.aaaac,
            R.drawable.aaaad,
            R.drawable.aaaae,
            R.drawable.aaaaf,
            R.drawable.aaaag,
            R.drawable.aaaah,
            R.drawable.aaaai,
            R.drawable.aaaaj,
    };
    private static int num_stimuli = stims.length;
    private static int stimuliWidth = PreferencesManager.cue_size;
//    private static int num_choices = prefManager.od_num_stim + prefManager.od_num_distractors;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_empty, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        logEvent(TAG+" started", callback);

        assignObjects();

        touchPrevention(view, callback);

        startMovie(prefManager.od_num_stim);
    }




    private void startMovie(int num_steps) {
        Log.d(TAG, "Playing movie, frame: " + num_steps + "/" + prefManager.od_num_stim);
        if (num_steps > 0) {    //  第一轮进入此时num_steps是预先初始化为1的
            h0.postDelayed(new Runnable()  {    //  运用接口将刺激可见不可点击，相当于刺激的展示阶段
                @Override
                public void run() {
                    UtilsTask.toggleCues4View(cues, true);
                };

            }, prefManager.od_start_delay);

            h1.postDelayed(new Runnable() { //  运用接口将刺激不可见也不可点击
                @Override
                public void run() {
                    UtilsTask.toggleCues(cues, false);

                }
            }, prefManager.od_start_delay + prefManager.od_duration_on);

            h2.postDelayed(new Runnable() { //  运用接口进入else
                @Override
                public void run() {

                    startMovie(num_steps - 1);

                }
            }, prefManager.od_start_delay + prefManager.od_duration_on + prefManager.od_duration_off);

        } else {

            // Choice phase
            int num_dirs = 4;
            Random r = new Random();

            // First make array of chosen positions
            boolean[] chosen_pos_bool = UtilsSystem.getBooleanFalseArray(num_dirs); //  得到一个全错的boolean数组

            choice_cues = new ImageButton[prefManager.od_num_stim + prefManager.od_num_distractors];    //  待选cues是1+prefManager.od_num_distractors个ImageBotton
            // Add correct answer
            for (int i = 0; i < cues.length; i++) { //  第一次进入函数是，cue.length等于1
                choice_cues[i] = UtilsTask.addImageCue(chosen_cue_id, getContext(), layout, buttonClickListener);   //  让展示过的刺激的button赋给选项0
                choice_cues[i].setImageResource(stims[chosen_cue_id]);  //  给选项0设置图片
                int chosen_dir = r.nextInt(num_dirs);   //  在0到4中选个随机数，这个数是正确答案
//                positionObject(chosen_dir, choice_cues[i]); //  放置正确选项  //  TODO 重写放置函数
                chosen_pos_bool[chosen_dir] = true; //  在全错数组中把正确的位置改成true
            }

            // Now add distractors (without replacement)

            // Array to track chosen stimuli

            boolean[] chosen_cues_bool = UtilsSystem.getBooleanFalseArray(num_stimuli);
            chosen_cues_bool[chosen_cue_id] = true;

            // For each distractor
            for (int i = 0; i < prefManager.od_num_distractors; i++) {
                // Choose stimuli
                int chosen_cue = UtilsTask.chooseValueNoReplacement(chosen_cues_bool);
                chosen_cues_bool[chosen_cue] = true;

                // Add cue to the UI
                choice_cues[i + prefManager.od_num_stim] = UtilsTask.addImageCue(-1, getContext(), layout, buttonClickListener);
                choice_cues[i + prefManager.od_num_stim].setImageResource(stims[chosen_cue]);

                // choose position of cue
                int chosen_pos = UtilsTask.chooseValueNoReplacement(chosen_pos_bool);
                chosen_pos_bool[chosen_pos] = true;
//                positionObject(chosen_pos, choice_cues[i + prefManager.od_num_stim]);
            }
            positionObject(choice_cues);

        }
    }

    private void positionObject(ImageButton[] choice_cues) {
        Point centre = UtilsTask.locateCentre_screen(getActivity());
        if (prefManager.od_num_distractors != 0) {
            Random r = new Random();
            int rChoice = r.nextInt(choice_cues.length);
            for (int i = 0; i < choice_cues.length; i++) {
                choice_cues[i].setX((int) (centre.x + centre.x * 3 / 5 * Math.cos((double) (2 * 3.14 * (i+rChoice) / choice_cues.length)) - prefManager.cue_size / 2));
                choice_cues[i].setY((int) (centre.y + centre.x * 3 / 5 * Math.sin((double) (2 * 3.14 * (i+rChoice) / choice_cues.length)) - prefManager.cue_size / 2));
            }
        } else {
            UtilsTask.centreCue(choice_cues[0], getActivity(), prefManager.cue_size);
        }
    }

//    private void positionObject(int pos, ImageButton cue) {
//        switch (pos) {
//            case 0:
//                cue.setX(175);
//                cue.setY(1200);
//                break;
//            case 1:
//                cue.setX(725);
//                cue.setY(300);
//                break;
//            case 2:
//                cue.setX(725);
//                cue.setY(1200);
//                break;
//            case 3:
//                cue.setX(175);
//                cue.setY(300);
//                break;
//        }
//    }


    private void assignObjects() {
        prefManager = new PreferencesManager(getContext()); //  获取设置
        prefManager.ObjectDiscrim();

        layout = getView().findViewById(R.id.parent_task_empty);

        // Choose cues (without replacement)
        cues = new ImageButton[prefManager.od_num_stim];    //  刚进入程序，此时od_num_stim的值被PreferenceManager.class初始化为1
        Random r = new Random();
        chosen_cue_id = r.nextInt(num_stimuli); //  设置好一个0到num_stimuli区间的随机整数作为调用图片对应的button的Id，用于下一步调用资源库里的图片
        cues[0] = UtilsTask.addImageCue(chosen_cue_id, getContext(), layout);   //  将id对应的botton赋给cues[0]，加到layout
        cues[0].setImageResource(stims[chosen_cue_id]); //  为这个button选择图片

        // Position cue in centre
        UtilsTask.centreCue(cues[0], getActivity(), stimuliWidth);


        UtilsTask.toggleCues(cues, false);  //  将这个长度为1的botton数组以false状态给toggleCues，即使其不可见，不可点击

    }


    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick "+view.getId());

            // Did they select the appropriate cue
            boolean correct_chosen = Integer.valueOf(view.getId()) == chosen_cue_id;

            endOfTrial(correct_chosen, callback);

        }
    };

    @Override
    public void onPause() {
        super.onPause();
        super.onDestroy();
        h0.removeCallbacksAndMessages(null);
        h1.removeCallbacksAndMessages(null);
        h2.removeCallbacksAndMessages(null);
    }

    // Implement interface and listener to enable communication up to TaskManager
    TaskInterface callback;
    public void setFragInterfaceListener(TaskInterface callback) {
        this.callback = callback;
    }

}
