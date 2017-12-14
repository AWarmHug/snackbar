package com.oginotihiro.snackbar.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.oginotihiro.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private View mView;
    private Snackbar mSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mView = findViewById(R.id.view);
        findViewById(R.id.defaultBtn).setOnClickListener(this);
        findViewById(R.id.leftBtn).setOnClickListener(this);
        findViewById(R.id.topBtn).setOnClickListener(this);
        findViewById(R.id.rightBtn).setOnClickListener(this);
        findViewById(R.id.bottomBtn).setOnClickListener(this);
        findViewById(R.id.animDurationBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.defaultBtn) {
            Snackbar.make(mView, "Message", Snackbar.LENGTH_INDEFINITE).setAction("Action", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "我知道了", Toast.LENGTH_SHORT).show();
                }
            }).show();

        } else if (id == R.id.leftBtn) {
            SnackbarLayout layout = new SnackbarLayout(this, Gravity.LEFT | Gravity.CENTER);
            mSnackbar = Snackbar.make(mView, layout, Snackbar.LEFT_RIGHT, Snackbar.LENGTH_SHORT);
            mSnackbar.show();
        } else if (id == R.id.topBtn) {
            SnackbarLayout layout = new SnackbarLayout(this, Gravity.TOP | Gravity.CENTER);
            mSnackbar = Snackbar.make(mView, layout, Snackbar.TOP_BOTTOM, Snackbar.LENGTH_SHORT);
            mSnackbar.show();
        } else if (id == R.id.rightBtn) {
            SnackbarLayout layout = new SnackbarLayout(this, Gravity.RIGHT | Gravity.CENTER);
            mSnackbar = Snackbar.make(mView, layout, Snackbar.RIGHT_LEFT, Snackbar.LENGTH_SHORT);
            mSnackbar.show();
        } else if (id == R.id.bottomBtn) {
            SnackbarLayout layout = new SnackbarLayout(this, Gravity.BOTTOM | Gravity.CENTER);
            mSnackbar = Snackbar.make(mView, layout, Snackbar.BOTTOM_TOP, Snackbar.LENGTH_SHORT);
            mSnackbar.show();
        } else if (id == R.id.animDurationBtn) {
            SnackbarLayout layout = new SnackbarLayout(this, Gravity.BOTTOM | Gravity.CENTER);
            mSnackbar = Snackbar.make(mView, layout, Snackbar.BOTTOM_TOP, Snackbar.LENGTH_SHORT, 2000);
            // mSnackbar = Snackbar.make(mView, layout, Snackbar.BOTTOM_TOP, Snackbar.LENGTH_SHORT, 2000, 1500);
            mSnackbar.show();
        }
    }

    class SnackbarLayout extends Snackbar.SnackbarLayoutBase {
        TextView msgTv;

        public SnackbarLayout(Context context, int gravity) {
            this(context, null, gravity);
        }

        public SnackbarLayout(Context context, AttributeSet attrs, int gravity) {
            super(context, attrs);

            // set layout params
            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lp.gravity = gravity;

            setLayoutParams(lp);

            // inflate content view with other XML layout file
            LayoutInflater.from(context).inflate(R.layout.snackbar, this);

            msgTv = (TextView) findViewById(R.id.msgTv);
            msgTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSnackbar.dismiss(Snackbar.Callback.DISMISS_EVENT_ACTION);
                }
            });
        }

        @Override
        protected void animateChildrenIn(int delay, int duration) {
            ViewCompat.setAlpha(msgTv, 0f);
            ViewCompat.animate(msgTv).alpha(1f).setDuration(duration).setStartDelay(delay).start();
        }

        @Override
        protected void animateChildrenOut(int delay, int duration) {
            ViewCompat.setAlpha(msgTv, 1f);
            ViewCompat.animate(msgTv).alpha(0f).setDuration(duration).setStartDelay(delay).start();
        }
    }
}