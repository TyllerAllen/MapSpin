package com.wlu.cp470.group12.mapspin;

import android.app.Activity;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class SpinUITest {

    @Before
    public void setUp(){
        ActivityScenario.launch(MapsActivity.class);
    }

    @Test
    public void shouldSpin() {
        onView(withId(R.id.green_circle)).check(matches(rotatedBy(0)));
        onView(withId(R.id.green_circle)).perform(spin());
        onView(withId(R.id.textName)).check(matches(isDisplayed()));
        onView(withId(R.id.spin_again)).perform(click());
        onView(withId(R.id.green_circle)).check(matches(not(rotatedBy(0))));
    }

    private ViewAction spin(){
        return new ViewAction(){

            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Spin circle";
            }

            @Override
            public void perform(UiController uiController, View view) {
                CircleImageView circle = (CircleImageView) view;
                circle.onFling(
                        MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,0,0,0),
                        MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,0,20,0),
                        0,10);
                }
        };
    }


    private float getRotation(ViewInteraction matcher){
        final float[] rotationHolder = {0f};
      matcher.perform(new ViewAction() {
          @Override
          public Matcher<View> getConstraints() {
              return null;
          }

          @Override
          public String getDescription() {
              return "Rotation of view";
          }

          @Override
          public void perform(UiController uiController, View view) {
              rotationHolder[0] = view.getRotation();
          }
      });
      return rotationHolder[0];
    }


    private ViewAction eventuallyNotRoatatedBy(float degrees, int millis){
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return rotatedBy(degrees);
            }

            @Override
            public String getDescription() {
                return "Checking for rotation to be eventually " + degrees;
            }

            @Override
            public void perform(UiController uiController, View view) {
                final long startTime = System.currentTimeMillis();
                final long endTime = startTime + millis;
                while (System.currentTimeMillis() < endTime){
                    if(view.getRotation() != degrees){
                        return;
                    }
                    uiController.loopMainThreadForAtLeast(50);
                }
                throw new PerformException.Builder()
                        .withActionDescription(this.getDescription())
                        .withViewDescription(HumanReadables.describe(view))
                        .withCause(new TimeoutException())
                        .build();
            }
        };
    }

    private Matcher<View> rotatedBy(float degrees) {
        return new BoundedMatcher<View, CircleImageView>(CircleImageView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("Rotated by "+degrees+" degrees");
            }

            @Override
            protected boolean matchesSafely(CircleImageView item) {
                Log.i("ROTATION", item.lastDegrees + "");
                return item.lastDegrees == degrees;
            }
        };
    }
}
