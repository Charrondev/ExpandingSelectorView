package com.charrondev.expandingselectorview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

/**
 * View for opening up a selector with a nice animation. Takes custom views
 */
public class ExpandingSelectorView extends FrameLayout {

    private static final String TAG = ExpandingSelectorView.class.getSimpleName();

    public ExpandingSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(isInEditMode()) {
            createEditModeView(context);
        } else {
            createView(context);
            getAttributes(context, attrs);
        }
    }

    /**
     * This method inflates the view to be visible in the preview layout
     *
     * @param context a context is needed to inflate layout
     */
    private void createEditModeView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.expanding_selector_view_edit_mode, this, true);
    }

    /**
     * This method inflates the view
     *
     * @param context a context is needed to inflate layout
     */
    private void createView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.expanding_selector_view, this, true);
    }

    /**
     * Retrieve view attributes.
     */
    private void getAttributes(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandingSelectorView);

        if (typedArray != null) {
            try {
                int i = 0;
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            } finally {
                typedArray.recycle();
            }
        }
    }}
