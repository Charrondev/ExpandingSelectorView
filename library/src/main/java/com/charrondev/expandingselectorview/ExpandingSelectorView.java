package com.charrondev.expandingselectorview;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * View for opening up a selector with a nice animation. Takes custom views
 */
public class ExpandingSelectorView extends FrameLayout implements View.OnClickListener {

    private static final String TAG = ExpandingSelectorView.class.getSimpleName();

    private Context mContext;

    private TextView mTitle;
    private TextView mSelection;
    private ImageView mIcon;
    private ImageView mArrow;
    private CardView mCardView;
    private View mCustomView;
    private RelativeLayout mViewParent;

    private int mColor;
    private int mCustomViewHeight;
    private int cardHeight;
    private int cardLeftMargin;

    private boolean isOpen = false;

    public ExpandingSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        if (isInEditMode()) {
            LayoutInflater.from(context).inflate(R.layout.expanding_selector_view_edit_mode, this, true);
        } else {
            createView();
            getAttributes(context, attrs);
        }
    }

    /**
     * This method inflates the view
     */
    private void createView() {
        LayoutInflater.from(mContext).inflate(R.layout.expanding_selector_view, this, true);
        mTitle = (TextView) findViewById(R.id.tv_title);
        mSelection = (TextView) findViewById(R.id.tv_selection);
        mIcon = (ImageView) findViewById(R.id.img_icon);
        mArrow = (ImageView) findViewById(R.id.img_downArrow);
        mCardView = (CardView) findViewById(R.id.cardView);
        mViewParent = (RelativeLayout) findViewById(R.id.customViewParent);

        cardHeight = (int) getResources().getDimension(R.dimen.default_height);
        cardLeftMargin = (int) getResources().getDimension(R.dimen.default_margin_left);

        mCardView.setOnClickListener(this);
    }

    /**
     * Retrieve view attributes.
     */
    private void getAttributes(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandingSelectorView);

        if (typedArray != null) {
            try {
                mTitle.setText(typedArray.getResourceId(R.styleable.ExpandingSelectorView_viewTitle, R.string.default_view_title));

                mColor = ContextCompat.getColor(context, typedArray.getResourceId(R.styleable.ExpandingSelectorView_imgColor, R.color.darkGray));

                Drawable drawable = ContextCompat.getDrawable(context, typedArray.getResourceId(R.styleable.ExpandingSelectorView_imgResource, R.drawable.ic_event));
                drawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(drawable, mColor);
                mIcon.setImageDrawable(drawable);

                int viewResource = typedArray.getResourceId(R.styleable.ExpandingSelectorView_viewResource, R.layout.simple_custom_view);
                mCustomView = LayoutInflater.from(context).inflate(viewResource, mViewParent, false);
                mCustomView.measure(MATCH_PARENT, WRAP_CONTENT);
                mCustomViewHeight = mCustomView.getMeasuredHeight();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            } finally {
                typedArray.recycle();
            }
        }
    }

    @Override
    public void onClick(View v) {

        final int ANIMATION_DURATION = 200;

        final AnimatorSet animatorSet = new AnimatorSet();
        final FrameLayout.LayoutParams cardParams = (FrameLayout.LayoutParams) mCardView.getLayoutParams();

        if (!isOpen) {

            // Margin animation
            final ValueAnimator marginAnimator = ValueAnimator.ofInt(cardLeftMargin, 0);
            marginAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    cardParams.leftMargin = (int) animation.getAnimatedValue();
                    mCardView.requestLayout();
                }
            });

            // Height animation
            final ValueAnimator heightAnimator = ValueAnimator.ofInt(cardHeight, cardHeight + mCustomViewHeight);
            heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    cardParams.height = (int) animation.getAnimatedValue();
                    mCustomView.requestLayout();
                }
            });

            // Arrow animation
            final ObjectAnimator rotationAnimation = ObjectAnimator.ofFloat(mArrow, "rotation", 180f);

            // Title animation keep in same spot
            final RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) mTitle.getLayoutParams();
            final ValueAnimator titleAnimator = ValueAnimator.ofInt(0, cardLeftMargin);
            titleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    params1.leftMargin = (int) animation.getAnimatedValue();
                    mTitle.requestLayout();
                }
            });

            // Image translate
            final ObjectAnimator photoTranslate = ObjectAnimator.ofFloat(mIcon, "translationX", AndroidUtil.dpToPx(16));

            // Custom view fade in
            final RelativeLayout.LayoutParams customViewParams = (RelativeLayout.LayoutParams) mCustomView.getLayoutParams();
            customViewParams.addRule(RelativeLayout.BELOW, R.id.tv_selection);
            mViewParent.addView(mCustomView, customViewParams);

            final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mCustomView, "alpha", 0f, 1f);
            final ObjectAnimator translateAnimator = ObjectAnimator.ofFloat(mCustomView, "translationX", AndroidUtil.getDisplayWidth(mContext) / 1.5f, 0);

            animatorSet
                    .play(marginAnimator)
                    .with(heightAnimator)
                    .with(rotationAnimation)
                    .with(titleAnimator)
                    .with(photoTranslate)
                    .with(alphaAnimator)
                    .with(translateAnimator);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.setDuration(ANIMATION_DURATION);

//            mCustomView.setVisibility(VISIBLE);
            animatorSet.start();

            isOpen = true;
        } else {

            // Margin animation
            final ValueAnimator marginAnimator = ValueAnimator.ofInt(0, cardLeftMargin);
            marginAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    cardParams.leftMargin = (int) animation.getAnimatedValue();
                    mCardView.requestLayout();
                }
            });

            // Height animation
            final ValueAnimator heightAnimator = ValueAnimator.ofInt(cardHeight + mCustomViewHeight, cardHeight);
            heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    cardParams.height = (int) animation.getAnimatedValue();
                    mCustomView.requestLayout();
                }
            });

            // Arrow animation
            final ObjectAnimator rotationAnimation = ObjectAnimator.ofFloat(mArrow, "rotation", 0f);

            // Title animation keep in same spot
            final RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) mTitle.getLayoutParams();
            final ValueAnimator titleAnimator = ValueAnimator.ofInt(cardLeftMargin, 0);
            titleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    params1.leftMargin = (int) animation.getAnimatedValue();
                    mTitle.requestLayout();
                }
            });

            // Image translate
            final ObjectAnimator photoTranslate = ObjectAnimator.ofFloat(mIcon, "translationX", 0);

            // Custom view fade in
            final RelativeLayout.LayoutParams customViewParams = (RelativeLayout.LayoutParams) mCustomView.getLayoutParams();

            final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mCustomView, "alpha", 0f);
            final ObjectAnimator translateAnimator = ObjectAnimator.ofFloat(mCustomView, "translationX", AndroidUtil.getDisplayWidth(mContext) / 1.5f);
            mViewParent.removeView(mCustomView);

            animatorSet
                    .play(marginAnimator)
                    .with(heightAnimator)
                    .with(rotationAnimation)
                    .with(titleAnimator)
                    .with(photoTranslate)
                    .with(alphaAnimator)
                    .with(translateAnimator);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.setDuration(ANIMATION_DURATION);

            animatorSet.start();
            isOpen = false;
        }
    }
}
