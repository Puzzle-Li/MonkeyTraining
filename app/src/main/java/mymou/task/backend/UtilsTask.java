package mymou.task.backend;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import mymou.preferences.PreferencesManager;

import java.util.Random;
import java.util.stream.IntStream;

public class UtilsTask {
    // Debug
    public static String TAG = "MymouUtils";

    // Make a list of the possible locations on the screen where cues can be placed
    public static Point[] getPossibleCueLocs(Activity activity) {
        PreferencesManager preferencesManager = new PreferencesManager(activity);
        int imageWidths = preferencesManager.cue_size;
        int border = preferencesManager.cue_spacing;  // Spacing between different task objects
        int totalImageSize = imageWidths + border;  //  找到整个图片占据屏幕的空间大小

        // Find centre of screen in pixels
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        // Find possible locs along each dimension
        int[] xlocs = calculateLocs(screenWidth, totalImageSize);
        int[] ylocs = calculateLocs(screenHeight, totalImageSize);

        // Populate 1D output array with all possible locations
        Point[] locs = new Point[xlocs.length * ylocs.length];
        int i_loc = 0;
        for (int i_x = 0; i_x < xlocs.length; i_x++) {
            for (int i_y = 0; i_y < ylocs.length; i_y++) {
                locs[i_loc] = new Point(xlocs[i_x], ylocs[i_y]);
                i_loc += 1;
            }
        }

        return locs;

    }

    // Add a mono-colour cue to the task
    public static Button addColorCue(int id, int color, Context context, View.OnClickListener onClickListener, ConstraintLayout layout) {
        PreferencesManager preferencesManager = new PreferencesManager(context);
        Button goCueButton = new Button(context);
        goCueButton.setWidth(preferencesManager.cue_size);
        goCueButton.setHeight(preferencesManager.cue_size);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setStroke(preferencesManager.border_size, preferencesManager.border_colour);
        drawable.setColor(color);
        goCueButton.setBackgroundDrawable(drawable);
        goCueButton.setId(id);
        goCueButton.setOnClickListener(onClickListener);
        layout.addView(goCueButton);
        return goCueButton;
    }

    // Add an image to the task
    public static ImageButton addImageCue(int id, Context context, ConstraintLayout layout) {
        PreferencesManager preferencesManager = new PreferencesManager(context);
        ImageButton button = new ImageButton(context);
        button.setLayoutParams(new LinearLayout.LayoutParams(preferencesManager.cue_size, preferencesManager.cue_size));
        button.setId(id);
        button.setScaleType(ImageView.ScaleType.FIT_XY);
        button.setBackgroundColor(preferencesManager.border_colour);
        int border = preferencesManager.border_size;
        button.setPadding(border, border, border, border);
        layout.addView(button);
        return button;
    }

    // Add a _clickable_ image to the task
    public static ImageButton addImageCue(int id, Context context, ConstraintLayout layout, View.OnClickListener onClickListener) {
        PreferencesManager preferencesManager = new PreferencesManager(context);
        ImageButton button = new ImageButton(context);
        button.setLayoutParams(new LinearLayout.LayoutParams(preferencesManager.cue_size, preferencesManager.cue_size));
        button.setId(id);
        button.setScaleType(ImageView.ScaleType.FIT_XY);
        int border = preferencesManager.border_size;
        button.setPadding(border, border, border, border);
        button.setOnClickListener(onClickListener);
        layout.addView(button);
        return button;
    }

    // Switches on a particular monkeys cues, and switches off other monkey's cues
    public static void toggleMonkeyCues(int monkId, Button[][] all_cues) {
        for (int i_monk = 0; i_monk < all_cues.length; i_monk++) {
            toggleCues(all_cues[i_monk], false);
        }
        toggleCues(all_cues[monkId], true);
    }

    // Iterates through a list of cues enabling/disabling all in list
    public static void toggleCues(Button[] buttons, boolean status) {
        for (int i = 0; i < buttons.length; i++) {
            UtilsTask.toggleCue(buttons[i], status);
        }
    }



    // Iterates through a list of cues enabling/disabling all in list
    public static void toggleCues(ImageButton[] buttons, boolean status) {
        for (int i = 0; i < buttons.length; i++) {
            UtilsTask.toggleCue(buttons[i], status);
        }
    }

    // Iterates through a list of cues enabling/disabling visually all in list
    public static void toggleCues4View(ImageButton[] buttons, boolean status) {
        for (int i = 0; i < buttons.length; i++) {
            UtilsTask.toggleCue4View(buttons[i], status);
        }
    }

    // Iterates through a list of cues enabling/disabling clickably all in list
    public static void toggleCues4Click(ImageButton[] buttons, boolean status) {
        for (int i = 0; i < buttons.length; i++) {
            UtilsTask.toggleCue4Click(buttons[i], status);
        }
    }

    // Fully enable/disable individual cue
    public static void toggleCue(Button button, boolean status) {
        toggleView(button, status);
        button.setClickable(status);
    }

    // Fully enable/disable individual cue
    public static void toggleCue(ImageButton button, boolean status) {
        toggleView(button, status);
        button.setClickable(status);
    }

    // Just enable/disable clickably individual cue
    public static void toggleCue4Click(ImageButton button, boolean status) {
        button.setClickable(status);
    }

    // Just enable/disable visually individual cue
    public static void toggleCue4View(ImageButton button, boolean status) {
        toggleView(button, status);
    }

    public static void toggleView(View view, boolean status) {
        if (status) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
        view.setEnabled(status);
    }


    public static void randomlyPositionCues(View[] cues, Point[] locs) {
        // Make zero array tracking which locations have already been used
        int maxCueLocations = locs.length;
        int[] chosen = new int[maxCueLocations];

        Random r = new Random();

        // Loop through and place each cue
        int choice = r.nextInt(maxCueLocations);
        for (int i = 0; i < cues.length; i++) {
            while (chosen[choice] == 1) {
                choice = r.nextInt(maxCueLocations);
            }
            cues[i].setX(locs[choice].x);
            cues[i].setY(locs[choice].y);
            chosen[choice] = 1;
        }
    }

    public static void randomlyPositionCues(View[] cues, Activity activity) {
        Point[] locs = getPossibleCueLocs(activity);

        // Make zero array tracking which locations have already been used
        int maxCueLocations = locs.length;
        int[] chosen = new int[maxCueLocations];

        Random r = new Random();

        // Loop through and place each cue
        int choice = r.nextInt(maxCueLocations);
        for (int i = 0; i < cues.length; i++) {
            while (chosen[choice] == 1) {
                choice = r.nextInt(maxCueLocations);
            }
            cues[i].setX(locs[choice].x);
            cues[i].setY(locs[choice].y);
            chosen[choice] = 1;
        }
    }

    public static void randomlyPositionCue(View cue, Activity activity) {
        Point[] locs = getPossibleCueLocs(activity);
        Random r = new Random();
        int choice = r.nextInt(locs.length);
        cue.setX(locs[choice].x);
        cue.setY(locs[choice].y);
    }

    public static int[] calculateLocs(int screenLength, int totalImageSize) {
        int num_locs = screenLength / totalImageSize; // floor division

        int[] locs = new int[num_locs];

        int offset = screenLength - (num_locs * totalImageSize);  // Centre images on screen
        offset = offset / 2;

        for (int i = 0; i < num_locs; i++) {
            locs[i] = offset + i * totalImageSize;
        }

        return locs;
    }

    public static Point getScreenSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point screen_size = new Point();
        display.getSize(screen_size);
        return new Point(screen_size.x, screen_size.y);
    }

    //  locate the centre point of screen
    public static Point locateCentre_screen(Activity activity) {
        Point screen_size = getScreenSize(activity);
        return new Point(screen_size.x/2, screen_size.y/2);
    }

    // Place a cue in the centre of the screen
    public static void centreCue(View cue, Activity activity, int cueSize) {
        Point centre = locateCentre_screen(activity);
        cue.setX((float) (centre.x - cueSize/2));
        cue.setY((float) (centre.y - cueSize/2));
    }

    // Rolls until it finds an unchosen position in the provided boolean array
    public static int chooseValueNoReplacement(boolean[] chosen_vals) {
        Random r = new Random();
        int chosen_i = r.nextInt(chosen_vals.length);   //  chosen_i是一个随机数
        while (chosen_vals[chosen_i]) { //  当chosen_vals的第chosen_i个值是true时，再赋给chosen_i一个新的随机数，以保证chosen_vals[chosen_i]一定是false
            chosen_i = r.nextInt(chosen_vals.length);
        }
        return chosen_i;
    }


}
