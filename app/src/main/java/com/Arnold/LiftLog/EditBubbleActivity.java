package com.Arnold.LiftLog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditBubbleActivity extends ActionBarActivity implements Comparable, TextWatcher {

    private List<Bubble> bubbles = new ArrayList<>();

    private EditText bubbleContentInput;

    final DatabaseHandler db = new DatabaseHandler(this);

    private final int max_bubble_length = 40;

    private boolean editMode = false;

    private int bubbleIdEdit;
    private Bubble bubbleEdit;

    private Spinner bubble_types;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bubble);

                                            //set focus layout for save_bubble_content(edit text box)
                                            //when receive focus, layout changes to drawable/focus_border_style.xml
                                            //when lost  focus, layout  changes to drawable/lost_focus_style.xml
        TextView tv2=(TextView)findViewById(R.id.save_bubble_content);
        tv2.setBackgroundResource(R.drawable.lost_focus_style);
        tv2.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            public void onFocusChange(View view, boolean hasfocus) {
                if (hasfocus) {

                    view.setBackgroundResource(R.drawable.focus_border_style);
                } else {
                    view.setBackgroundResource(R.drawable.lost_focus_style);
                }
            }
        });
        // Set up text input handlers
        this.bubbleContentInput = (EditText) findViewById(R.id.save_bubble_content);
        this.bubbleContentInput.setText("");
        this.bubbleContentInput.addTextChangedListener(this);

        //Updates the list of bubbles
        this.updateBubbles();

        // Initialize the 'Save Bubble' button
        final Button save_button = (Button) findViewById(R.id.save_bubble_button);

        save_button.getBackground().setColorFilter(0xFF666666, PorterDuff.Mode.MULTIPLY);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBubble();
            }
        });

        bubble_types = (Spinner) findViewById(R.id.type_spinner);
    }

    public void updateBubbles() {

        // Initialize the Layout
        LinearLayout layout = (LinearLayout) findViewById(R.id.View_Bubbles);

        layout.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

        layout.removeAllViews();

        this.bubbles = db.getAllBubbles();

        //Collections.sort(bubbles);

        //loop that creates buttons based on the number of logs stored in the database
        //these buttons will be scrollable because of the xml file. Clicking on a button will
        //bring you to another activity in which you can see the contents of the log

        // The double while loop was part of an earlier attempt with the TableLayout,
        // disregard the unnecessary inner loop

        //sets button parameters
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);

        // Scrollview for showing exercise bubbles
        ScrollView exercise_scroll = new ScrollView(this);
        LinearLayout exercise_bubs = new LinearLayout(this);
        exercise_bubs.setOrientation(LinearLayout.VERTICAL);
        exercise_bubs.setVerticalScrollBarEnabled(true);
        exercise_scroll.addView(exercise_bubs, lp);

        // Scrollview for showing repetition bubbles
        ScrollView reps_sets_scroll = new ScrollView(this);
        LinearLayout reps_sets_bubs = new LinearLayout(this);
        reps_sets_bubs.setOrientation(LinearLayout.VERTICAL);
        reps_sets_bubs.setVerticalScrollBarEnabled(true);
        reps_sets_scroll.addView(reps_sets_bubs, lp);

        // Scrollview for showing duration bubbles
        ScrollView weight_rest_scroll = new ScrollView(this);
        LinearLayout weight_rest_bubs = new LinearLayout(this);
        weight_rest_bubs.setOrientation(LinearLayout.VERTICAL);
        weight_rest_bubs.setVerticalScrollBarEnabled(true);
        weight_rest_scroll.addView(weight_rest_bubs, lp);

        for (final Bubble curr_bubble : bubbles) {

            //new button being created for bubble
            final Button myButton = new Button(this);

            //set the text of the log to be the logs title (will add date later)
            myButton.setText(curr_bubble.getBubbleContent());

            myButton.setClickable(true);
            myButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    bubbleContentInput.setText("");
                    editMode = true;
                    editBubbles(curr_bubble);
                }
            });

            myButton.setPadding(2, 2, 2, 2);

            myButton.setWidth(325);

            // Adding color to the bubs and placing them in the right column
            if (curr_bubble.getBubbleType() == Bubble.BUBBLE_TYPE_EXERCISE) {
                myButton.getBackground().setColorFilter(0xFF00DD00, PorterDuff.Mode.MULTIPLY);
                exercise_bubs.addView(myButton);
            } else if (curr_bubble.getBubbleType() == Bubble.BUBBLE_TYPE_REPS ||
                    curr_bubble.getBubbleType() == Bubble.BUBBLE_TYPE_SETS ) {

                if (curr_bubble.getBubbleType() == Bubble.BUBBLE_TYPE_REPS) {
                    myButton.getBackground().setColorFilter(0xFFFE5000, PorterDuff.Mode.MULTIPLY);
                } else {
                    myButton.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
                }
                reps_sets_bubs.addView(myButton);
            } else {

                if (curr_bubble.getBubbleType() == Bubble.BUBBLE_TYPE_WEIGHT) {
                    myButton.getBackground().setColorFilter(0xFF00CCEE, PorterDuff.Mode.MULTIPLY);
                } else {
                    myButton.getBackground().setColorFilter(0xFF0000EE, PorterDuff.Mode.MULTIPLY);
                }
                weight_rest_bubs.addView(myButton);
            }
            myButton.setOnLongClickListener(new View.OnLongClickListener(){
                public boolean onLongClick(View v){AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditBubbleActivity.this);
                    alertDialogBuilder.setTitle("Delete Bubble");
                    alertDialogBuilder.setMessage("Do you wish to delete this bubble?");

                    //don't delete button
                    alertDialogBuilder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing
                        }
                    });

                    //delete button
                    alertDialogBuilder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //delete bubble from database
                            bubbleContentInput.setText("");
                            db.deleteBubble(curr_bubble.getBubbleContent());
                            updateBubbles();

                            Toast.makeText(EditBubbleActivity.this, "Bubble Deleted", Toast.LENGTH_SHORT).show();
                        }
                    });
                    alertDialogBuilder.create().show();
                    return true;
                }
            });
        }

        ScrollView.LayoutParams scroll = new ScrollView.LayoutParams(ScrollView.LayoutParams.WRAP_CONTENT, ScrollView.LayoutParams.MATCH_PARENT);

        layout.addView(exercise_scroll, scroll);
        layout.addView(reps_sets_scroll, scroll);
        layout.addView(weight_rest_scroll, scroll);
    }

    public void saveBubble(){

        String bubble_type_string = String.valueOf(bubble_types.getSelectedItem());

        int bubble_type = -1;

//        if (bubble_type_string.equals("Exercise")) {
//            bubble_type = Bubble.BUBBLE_TYPE_EXERCISE;
//        } else if (bubble_type_string.equals("Reps")) {
//            bubble_type = Bubble.BUBBLE_TYPE_REPS;
//        } else if (bubble_type_string.equals("Sets")) {
//            bubble_type = Bubble.BUBBLE_TYPE_SETS;
//        } else if (bubble_type_string.equals("Weight")) {
//            bubble_type = Bubble.BUBBLE_TYPE_WEIGHT;
//        } else if (bubble_type_string.equals("Rest Duration")) {
//            bubble_type = Bubble.BUBBLE_TYPE_REST;
//        } else {
//            this.bubbleContentInput.setError("Invalid Bubble type.");
//            return;
//        }

        bubble_type = bubble_types.getSelectedItemPosition();


        boolean success;
        //Add code to save the bubble
        String bubbleContent = this.bubbleContentInput.getText().toString();

        if (bubbleContent.equals("") || bubbleContent.length() > max_bubble_length) {
            this.bubbleContentInput.setError("Bubble content cannot be empty or exceed "+this.max_bubble_length+" characters.");
            this.bubbleContentInput.setText("");

            return;
        }

        if (!editMode) {
            success = db.addBubble(new Bubble(bubbleContent, bubble_type));
        }
        else {
            if (bubbleEdit.getBubbleID() == bubble_types.getSelectedItemPosition()) {
                success = db.updateBubble(bubbleEdit.getBubbleID(), new Bubble(bubbleContent, bubble_type));
            }
            else{
                db.deleteBubble(bubbleEdit.getBubbleContent());
                success = db.addBubble(new Bubble(bubbleContent, bubble_type));
            }
            editMode=false;
        }

        if (success) {
            Toast.makeText(this, "Yeah, Bubble Saved!", Toast.LENGTH_SHORT).show();
            this.bubbleContentInput.setText("");
            this.updateBubbles();
        }
        else {
            Toast.makeText(this, "Error Saving Bubble. Please, try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_bubble, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //If there is text in the input bubble, then shows confirm dialog
        String bubbleContent = this.bubbleContentInput.getText().toString();
        if (!bubbleContent.equals("")){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditBubbleActivity.this);
            alertDialogBuilder.setTitle("Return");
            alertDialogBuilder.setMessage("Do you want to return and lose your unsaved data?");

            alertDialogBuilder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //do nothing
                }
            });

            alertDialogBuilder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Return to Main Screen
                    Intent intent = new Intent(EditBubbleActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });
            alertDialogBuilder.create().show();
        }
        else{
            //If there's no text, just returns to Home Screen
            this.finish();
        }
    }

    public void editBubbles(Bubble bubble) {
        if (editMode){
            bubbleEdit = bubble;
            bubbleContentInput.setText(bubble.getBubbleContent());
            bubble_types.setSelection(bubble.getBubbleType());
        }
    }

    @Override
    public int compareTo(Object another) {
        return 0;
    }

    /* -- */
    /*Methods to listen while the user types a text in the New Bubble input*/
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if (s.length() > this.max_bubble_length) {
            this.bubbleContentInput.setError("Bubble content cannot exceed "+this.max_bubble_length+" characters.");
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
    /* -- */


}
