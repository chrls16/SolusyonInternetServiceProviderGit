    package com.example.solusyoninternetserviceprovider;

    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.Button;
    import android.widget.LinearLayout;
    import android.widget.TextView;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.viewpager2.widget.ViewPager2;

    public class OnboardingActivity extends AppCompatActivity {

        private ViewPager2 viewPager;
        private LinearLayout dotsIndicator;
        private Button btnNext;
        private TextView tvSkip;

        private final int[] pageLayouts = {
                R.layout.onboarding_page1,
                R.layout.onboarding_page2,
                R.layout.onboarding_page3
        };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_onboarding);

            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }

            viewPager = findViewById(R.id.viewPager);
            dotsIndicator = findViewById(R.id.dotsIndicator);
            btnNext = findViewById(R.id.btnNext);
            tvSkip = findViewById(R.id.tvSkip);

            OnboardingAdapter adapter = new OnboardingAdapter(pageLayouts);
            viewPager.setAdapter(adapter);

            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    buildDots(position);

                    if (position == pageLayouts.length - 1) {
                        btnNext.setText("Get Started");
                    } else {
                        btnNext.setText("Next");
                    }
                }
            });

            // FIX: Use .post() to ensure the ViewPager is ready before drawing dots
            viewPager.post(() -> {
                buildDots(0);
            });

            btnNext.setOnClickListener(v -> {
                int current = viewPager.getCurrentItem();
                if (current < pageLayouts.length - 1) {
                    viewPager.setCurrentItem(current + 1);
                } else {
                    finishOnboarding();
                }
            });

            tvSkip.setOnClickListener(v -> finishOnboarding());
        }

// Inside OnboardingActivity.java

// Top of the file
        private void finishOnboarding() {
            // Save the completion flag in SharedPreferences
            SharedPreferences preferences = getSharedPreferences("onboarding_pref", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isFinished", true);
            editor.apply();

            // Navigate to the Welcome Screen
            Intent intent = new Intent(OnboardingActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        }

        private void buildDots(int currentPage) {
            dotsIndicator.removeAllViews();

            for (int i = 0; i < pageLayouts.length; i++) {
                View dot = new View(this);
                LinearLayout.LayoutParams params;

                if (i == currentPage) {
                    params = new LinearLayout.LayoutParams(dpToPx(24), dpToPx(8));
                    dot.setBackgroundResource(R.drawable.dot_active);
                } else {
                    params = new LinearLayout.LayoutParams(dpToPx(8), dpToPx(8));
                    dot.setBackgroundResource(R.drawable.dot_inactive);
                }

                params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
                dot.setLayoutParams(params);
                dotsIndicator.addView(dot);
            }
        }

        private int dpToPx(int dp) {
            return (int) (dp * getResources().getDisplayMetrics().density);
        }
    }