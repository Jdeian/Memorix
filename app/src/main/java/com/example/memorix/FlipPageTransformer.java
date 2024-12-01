package com.example.memorix;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class FlipPageTransformer implements ViewPager2.PageTransformer {
    @Override
    public void transformPage(@NonNull View page, float position) {
        if (position < -1) {
            page.setAlpha(0);
        } else if (position <= 1) {
            page.setAlpha(1);
            page.setTranslationX(0);
            page.setRotationY(position * -30);
        } else {
            page.setAlpha(0);
        }
    }
}
