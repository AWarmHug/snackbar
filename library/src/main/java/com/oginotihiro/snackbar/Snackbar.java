/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oginotihiro.snackbar;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class Snackbar {
    /**
     * Callback class for {@link Snackbar} instances.
     *
     * @see Snackbar#setCallback(Callback)
     */
    public static abstract class Callback {
        /** Indicates that the Snackbar was dismissed via an action click.*/
        public static final int DISMISS_EVENT_ACTION = 0;
        /** Indicates that the Snackbar was dismissed via a timeout.*/
        public static final int DISMISS_EVENT_TIMEOUT = 1;
        /** Indicates that the Snackbar was dismissed via a call to {@link #dismiss()}.*/
        public static final int DISMISS_EVENT_MANUAL = 2;
        /** Indicates that the Snackbar was dismissed from a new Snackbar being shown.*/
        public static final int DISMISS_EVENT_CONSECUTIVE = 3;

        /** @hide */
        @IntDef({DISMISS_EVENT_ACTION, DISMISS_EVENT_TIMEOUT,
                DISMISS_EVENT_MANUAL, DISMISS_EVENT_CONSECUTIVE})
        @Retention(RetentionPolicy.SOURCE)
        public @interface DismissEvent {}

        /**
         * Called when the given {@link Snackbar} is visible.
         *
         * @param snackbar The snackbar which is now visible.
         * @see Snackbar#show()
         */
        public void onShow(Snackbar snackbar) {
            // empty
        }

        /**
         * Called when the given {@link Snackbar} has been dismissed, either through a time-out,
         * having been manually dismissed, or an action being clicked.
         *
         * @param snackbar The snackbar which has been dismissed.
         * @param event The event which caused the dismissal. One of either:
         *              {@link #DISMISS_EVENT_ACTION}, {@link #DISMISS_EVENT_TIMEOUT},
         *              {@link #DISMISS_EVENT_MANUAL} or {@link #DISMISS_EVENT_CONSECUTIVE}.
         *
         * @see Snackbar#dismiss()
         */
        public void onDismissed(Snackbar snackbar, @DismissEvent int event) {
            // empty
        }
    }

    /** @hide */
    @IntDef({LEFT_RIGHT, TOP_BOTTOM, RIGHT_LEFT, BOTTOM_TOP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Direction {}

    public static final int LEFT_RIGHT = 0;
    public static final int TOP_BOTTOM = 1;
    public static final int RIGHT_LEFT = 2;
    public static final int BOTTOM_TOP = 3;

    /** @hide */
    @IntDef({LENGTH_INDEFINITE, LENGTH_SHORT, LENGTH_LONG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {}

    /**
     * Show the Snackbar indefinitely. This means that the Snackbar will be displayed from the time
     * that is {@link #show() shown} until either it is dismissed, or another Snackbar is shown.
     *
     * @see #setDuration
     */
    public static final int LENGTH_INDEFINITE = -2;
    /**
     * Show the Snackbar for a short period of time.
     *
     * @see #setDuration
     */
    public static final int LENGTH_SHORT = -1;
    /**
     * Show the Snackbar for a long period of time.
     *
     * @see #setDuration
     */
    public static final int LENGTH_LONG = 0;

    private static final int ANIMATION_DURATION = 750;
    private static final int ANIMATION_FADE_DURATION = 540;

    private static final Handler sHandler;
    private static final int MSG_SHOW = 0;
    private static final int MSG_DISMISS = 1;

    static {
        sHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MSG_SHOW:
                        ((Snackbar) message.obj).showView();
                        return true;
                    case MSG_DISMISS:
                        ((Snackbar) message.obj).hideView(message.arg1);
                        return true;
                }
                return false;
            }
        });
    }

    private final SnackbarManager.Callback mManagerCallback = new SnackbarManager.Callback() {
        @Override
        public void show() {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_SHOW, Snackbar.this));
        }

        @Override
        public void dismiss(int event) {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_DISMISS, event, 0, Snackbar.this));
        }
    };

    private final Context mContext;
    private final ViewGroup mTargetParent;
    private SnackbarLayoutBase mView;
    private int mDirection;
    private int mDuration;
    private int mAnimDuration;
    private int mAnimFadeDuration;
    private Callback mCallback;

    private Snackbar(ViewGroup parent, SnackbarLayoutBase layout) {
        mContext = parent.getContext();
        mTargetParent = parent;
        mView = layout;
    }

    /**
     * Make a Snackbar to display
     *
     * <p>Snackbar will try and find a parent view to hold Snackbar's view from the value given
     * to {@code view}. Snackbar will walk up the view tree trying to find a suitable parent.
     *
     * @param view              The view to find a parent from.
     * @param layout            The content view.
     * @param direction         The animation direction.
     * @param duration          How long to display the snackbar.  Either {@link #LENGTH_SHORT} or {@link
     *                          #LENGTH_LONG}
     */
    public static Snackbar make(@NonNull View view, @NonNull SnackbarLayoutBase layout, @Direction int direction, @Duration int duration) {
        return make(view, layout, direction, duration, ANIMATION_DURATION, ANIMATION_FADE_DURATION);
    }

    /**
     * Make a Snackbar to display
     *
     * <p>Snackbar will try and find a parent view to hold Snackbar's view from the value given
     * to {@code view}. Snackbar will walk up the view tree trying to find a suitable parent.
     *
     * @param view               The view to find a parent from.
     * @param layout             The content view.
     * @param direction          The animation direction.
     * @param duration           How long to display the message.  Either {@link #LENGTH_SHORT} or {@link
     *                           #LENGTH_LONG}
     * @param animationDuration
     */
    public static Snackbar make(@NonNull View view, @NonNull SnackbarLayoutBase layout, @Direction int direction, @Duration int duration, int animationDuration) {
        return make(view, layout, direction, duration, animationDuration, animationDuration);
    }

    /**
     * Make a Snackbar to display
     *
     * <p>Snackbar will try and find a parent view to hold Snackbar's view from the value given
     * to {@code view}. Snackbar will walk up the view tree trying to find a suitable parent.
     *
     * @param view               The view to find a parent from.
     * @param layout             The content view.
     * @param direction          The animation direction.
     * @param duration           How long to display the message.  Either {@link #LENGTH_SHORT} or {@link
     *                           #LENGTH_LONG}
     * @param animationDuration
     * @param animationFadeDuration
     */
    public static Snackbar make(@NonNull View view, @NonNull SnackbarLayoutBase layout, @Direction int direction, @Duration int duration, int animationDuration, int animationFadeDuration) {
        Snackbar snackbar = new Snackbar(findSuitableParent(view), layout);
        snackbar.setDirection(direction);
        snackbar.setDuration(duration);
        snackbar.setAnimationDuration(animationDuration);
        snackbar.setAnimationFadeDuration(animationFadeDuration);
        return snackbar;
    }

    private static ViewGroup findSuitableParent(View view) {
        ViewGroup fallback = null;
        do {
            if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content) {
                    // If we've hit the decor content view, use it.
                    return (ViewGroup) view;
                } else {
                    // It's not the content view but we'll use it as our fallback
                    fallback = (ViewGroup) view;
                }
            }

            if (view != null) {
                // Else, we will loop and crawl up the view hierarchy and try to find a parent
                final ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);

        // If we reach here then we didn't find a suitable content view so we'll fallback
        return fallback;
    }

    @NonNull
    public Snackbar setContentView(SnackbarLayoutBase layout) {
        mView = layout;
        return this;
    }

    @NonNull
    public SnackbarLayoutBase getContentView() {
        return mView;
    }

    @NonNull
    public Snackbar setDirection(@Direction int direction) {
        mDirection = direction;
        return this;
    }

    @Direction
    public int getDirection() {
        return mDirection;
    }

    @NonNull
    public Snackbar setDuration(@Duration int duration) {
        mDuration = duration;
        return this;
    }

    @Duration
    public int getDuration() {
        return mDuration;
    }

    @NonNull
    public Snackbar setAnimationDuration(int animationDuration) {
        if (animationDuration <= 0) {
            throw new IllegalArgumentException("animationDuration must be > 0");
        }
        mAnimDuration = animationDuration;
        return this;
    }

    public int getAnimationDuration() {
        return mAnimDuration;
    }

    @NonNull
    public Snackbar setAnimationFadeDuration(int animationFadeDuration) {
        if (animationFadeDuration > mAnimDuration) {
            throw new IllegalArgumentException("animationFadeDuration must be < animationDuration");
        }
        mAnimFadeDuration = animationFadeDuration;
        return this;
    }

    public int getAnimationFadeDuration() {
        return mAnimFadeDuration;
    }

    /**
     * Set a callback to be called when this the visibility of this {@link Snackbar} changes.
     */
    @NonNull
    public Snackbar setCallback(Callback callback) {
        mCallback = callback;
        return this;
    }

    /**
     * Show the {@link Snackbar}.
     */
    public void show() {
        SnackbarManager.getInstance().show(mDuration, mManagerCallback);
    }

    /**
     * Dismiss the {@link Snackbar}.
     */
    public void dismiss() {
        dispatchDismiss(Callback.DISMISS_EVENT_MANUAL);
    }

    /**
     * Dismiss the {@link Snackbar}.
     */
    public void dismiss(@Callback.DismissEvent int event) {
        dispatchDismiss(event);
    }

    private void dispatchDismiss(@Callback.DismissEvent int event) {
        SnackbarManager.getInstance().dismiss(mManagerCallback, event);
    }

    /**
     * Return whether this {@link Snackbar} is currently being shown.
     */
    public boolean isShown() {
        return SnackbarManager.getInstance().isCurrent(mManagerCallback);
    }

    /**
     * Returns whether this {@link Snackbar} is currently being shown, or is queued to be shown next.
     */
    public boolean isShownOrQueued() {
        return SnackbarManager.getInstance().isCurrentOrNext(mManagerCallback);
    }

    final void showView() {
        if (mView.getParent() == null) {
            mTargetParent.addView(mView);
        }

        mView.setOnAttachStateChangeListener(new SnackbarLayoutBase.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {}

            @Override
            public void onViewDetachedFromWindow(View v) {
                if (isShownOrQueued()) {
                    // If we haven't already been dismissed then this event is coming from a
                    // non-user initiated action. Hence we need to make sure that we callback
                    // and keep our state up to date. We need to post the call since removeView()
                    // will call through to onDetachedFromWindow and thus overflow.
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onViewHidden(Callback.DISMISS_EVENT_MANUAL);
                        }
                    });
                }
            }
        });

        if (ViewCompat.isLaidOut(mView)) {
            // If the view is already laid out, animate it now
            animateViewIn();
        } else {
            // Otherwise, add one of our layout change listeners and animate it in when laid out
            mView.setOnLayoutChangeListener(new SnackbarLayoutBase.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int left, int top, int right, int bottom) {
                    animateViewIn();
                    mView.setOnLayoutChangeListener(null);
                }
            });
        }
    }

    private void animateViewIn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ViewPropertyAnimatorCompat vpac = ViewCompat.animate(mView);

            if (mDirection == LEFT_RIGHT) {
                ViewCompat.setTranslationX(mView, -mView.getWidth());
                vpac.translationX(0f);
            } else if (mDirection == TOP_BOTTOM) {
                ViewCompat.setTranslationY(mView, -mView.getHeight());
                vpac.translationY(0f);
            } else if (mDirection == RIGHT_LEFT) {
                ViewCompat.setTranslationX(mView, mView.getWidth());
                vpac.translationX(0f);
            } else if (mDirection == BOTTOM_TOP) {
                ViewCompat.setTranslationY(mView, mView.getHeight());
                vpac.translationY(0f);
            }

            vpac.setInterpolator(new FastOutSlowInInterpolator()).setDuration(mAnimDuration)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(View view) {
                            mView.animateChildrenIn(mAnimDuration - mAnimFadeDuration, mAnimFadeDuration);
                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            if (mCallback != null) {
                                mCallback.onShow(Snackbar.this);
                            }
                            SnackbarManager.getInstance().onShown(mManagerCallback);
                        }
                    }).start();
        } else {
            Animation anim = null;

            if (mDirection == LEFT_RIGHT) {
                anim = AnimationUtils.loadAnimation(mView.getContext(), R.anim.oginotihiro_snackbar_left_in);
            } else if (mDirection == TOP_BOTTOM) {
                anim = AnimationUtils.loadAnimation(mView.getContext(), R.anim.oginotihiro_snackbar_top_in);
            } else if (mDirection == RIGHT_LEFT) {
                anim = AnimationUtils.loadAnimation(mView.getContext(), R.anim.oginotihiro_snackbar_right_in);
            } else if (mDirection == BOTTOM_TOP) {
                anim = AnimationUtils.loadAnimation(mView.getContext(), R.anim.oginotihiro_snackbar_bottom_in);
            }

            if (anim == null) return;

            anim.setInterpolator(new FastOutSlowInInterpolator());
            anim.setDuration(mAnimDuration);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mCallback != null) {
                        mCallback.onShow(Snackbar.this);
                    }
                    SnackbarManager.getInstance().onShown(mManagerCallback);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            mView.setAnimation(anim);
        }
    }

    final void hideView(int event) {
        if (mView.getVisibility() != View.VISIBLE) {
            onViewHidden(event);
        } else {
            animateViewOut(event);
        }
    }

    private void animateViewOut(final int event) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ViewPropertyAnimatorCompat vpac = ViewCompat.animate(mView);

            if (mDirection == LEFT_RIGHT) {
                vpac.translationX(-mView.getWidth());
            } else if (mDirection == TOP_BOTTOM) {
                vpac.translationY(-mView.getHeight());
            } else if (mDirection == RIGHT_LEFT) {
                vpac.translationX(mView.getWidth());
            } else if (mDirection == BOTTOM_TOP) {
                vpac.translationY(mView.getHeight());
            }

            vpac.setInterpolator(new FastOutSlowInInterpolator())
                    .setDuration(mAnimDuration)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(View view) {
                            mView.animateChildrenOut(0, mAnimFadeDuration);
                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            onViewHidden(event);
                        }
                    }).start();
        } else {
            Animation anim = null;

            if (mDirection == LEFT_RIGHT) {
                anim = AnimationUtils.loadAnimation(mView.getContext(), R.anim.oginotihiro_snackbar_left_out);
            } else if (mDirection == TOP_BOTTOM) {
                anim = AnimationUtils.loadAnimation(mView.getContext(), R.anim.oginotihiro_snackbar_top_out);
            } else if (mDirection == RIGHT_LEFT) {
                anim = AnimationUtils.loadAnimation(mView.getContext(), R.anim.oginotihiro_snackbar_right_out);
            } else if (mDirection == BOTTOM_TOP) {
                anim = AnimationUtils.loadAnimation(mView.getContext(), R.anim.oginotihiro_snackbar_bottom_out);
            }

            if (anim == null) return;

            anim.setInterpolator(new FastOutSlowInInterpolator());
            anim.setDuration(ANIMATION_DURATION);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    onViewHidden(event);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            mView.startAnimation(anim);
        }
    }

    private void onViewHidden(int event) {
        // First remove the view from the parent (if attached)
        final ViewParent parent = mView.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(mView);
        }
        // Now call the dismiss listener (if available)
        if (mCallback != null) {
            mCallback.onDismissed(this, event);
        }
        // Finally, tell the SnackbarManager that it has been dismissed
        SnackbarManager.getInstance().onDismissed(mManagerCallback);
    }

    public static abstract class SnackbarLayoutBase extends FrameLayout {
        interface OnLayoutChangeListener {
            void onLayoutChange(View view, int left, int top, int right, int bottom);
        }

        interface OnAttachStateChangeListener {
            void onViewAttachedToWindow(View v);
            void onViewDetachedFromWindow(View v);
        }

        private OnLayoutChangeListener mOnLayoutChangeListener;
        private OnAttachStateChangeListener mOnAttachStateChangeListener;

        void setOnLayoutChangeListener(OnLayoutChangeListener onLayoutChangeListener) {
            mOnLayoutChangeListener = onLayoutChangeListener;
        }

        void setOnAttachStateChangeListener(OnAttachStateChangeListener onAttachStateChangeListener) {
            mOnAttachStateChangeListener = onAttachStateChangeListener;
        }

        public SnackbarLayoutBase(Context context) {
            this(context, null);
        }

        public SnackbarLayoutBase(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            if (mOnLayoutChangeListener != null) {
                mOnLayoutChangeListener.onLayoutChange(this, left, top, right, bottom);
            }
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (mOnAttachStateChangeListener != null) {
                mOnAttachStateChangeListener.onViewAttachedToWindow(this);
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (mOnAttachStateChangeListener != null) {
                mOnAttachStateChangeListener.onViewDetachedFromWindow(this);
            }
        }

        protected abstract void animateChildrenIn(int delay, int duration);

        protected abstract void animateChildrenOut(int delay, int duration);
    }
}