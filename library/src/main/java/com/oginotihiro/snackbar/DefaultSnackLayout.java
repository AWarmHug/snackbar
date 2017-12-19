package com.oginotihiro.snackbar;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

/**
 * 作者：warm
 * 时间：2017-12-14 14:21
 * 描述：
 */

public class DefaultSnackLayout extends Snackbar.SnackbarLayoutBase {
    private TextView tvText;
    private Button btAction;

    public DefaultSnackLayout(Context context, int gravity) {
        this(context, null, gravity);
    }

    public DefaultSnackLayout(Context context, AttributeSet attrs, int gravity) {
        super(context, attrs);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = gravity;
        setLayoutParams(lp);
        inflate(context, R.layout.snackbar_default, this);
        tvText = (TextView) findViewById(R.id.snackbar_text);
        btAction = (Button) findViewById(R.id.snackbar_action);
    }


    public TextView getTvText() {
        return tvText;
    }

    public Button getBtAction() {
        return btAction;
    }

    @Override
    protected void animateChildrenIn(int delay, int duration) {
        ViewCompat.setAlpha(tvText, 0f);
        ViewCompat.animate(tvText).alpha(1f).setDuration(duration)
                .setStartDelay(delay).start();

        if (btAction.getVisibility() == VISIBLE) {
            ViewCompat.setAlpha(btAction, 0f);
            ViewCompat.animate(btAction).alpha(1f).setDuration(duration)
                    .setStartDelay(delay).start();
        }
    }

    @Override
    protected void animateChildrenOut(int delay, int duration) {
        ViewCompat.setAlpha(tvText, 1f);
        ViewCompat.animate(tvText).alpha(0f).setDuration(duration)
                .setStartDelay(delay).start();

        if (btAction.getVisibility() == VISIBLE) {
            ViewCompat.setAlpha(btAction, 1f);
            ViewCompat.animate(btAction).alpha(0f).setDuration(duration)
                    .setStartDelay(delay).start();
        }
    }
}
