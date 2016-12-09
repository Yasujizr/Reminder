package com.elementary.tasks.navigation.fragments;

import android.app.AlarmManager;
import android.os.Bundle;
import android.util.Log;

import com.elementary.tasks.R;
import com.elementary.tasks.core.calendar.Events;
import com.elementary.tasks.core.calendar.FlextCalendarFragment;
import com.elementary.tasks.core.calendar.FlextHelper;
import com.elementary.tasks.core.calendar.FlextListener;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.navigation.settings.images.MonthImage;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import hirondelle.date4j.DateTime;

/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class CalendarFragment extends BaseNavigationFragment {

    private static final String TAG = "CalendarFragment";

    @Override
    public void onResume() {
        super.onResume();
        showCalendar();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.calendar));
            mCallback.onFragmentSelect(this);
            mCallback.setClick(null);
        }
    }

    private void showCalendar() {
        ThemeUtil themeUtil = ThemeUtil.getInstance(mContext);
        FlextCalendarFragment calendarView = new FlextCalendarFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        args.putInt(FlextCalendarFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(FlextCalendarFragment.YEAR, cal.get(Calendar.YEAR));
        if (Prefs.getInstance(mContext).getStartDay() == 0) {
            args.putInt(FlextCalendarFragment.START_DAY_OF_WEEK, FlextCalendarFragment.SUNDAY);
        } else {
            args.putInt(FlextCalendarFragment.START_DAY_OF_WEEK, FlextCalendarFragment.MONDAY);
        }
        args.putBoolean(FlextCalendarFragment.DARK_THEME, themeUtil.isDark());
        args.putBoolean(FlextCalendarFragment.ENABLE_IMAGES, Prefs.getInstance(mContext).isCalendarImagesEnabled());
        MonthImage monthImage = Prefs.getInstance(mContext).getCalendarImages();
        Log.d(TAG, "showCalendar: " + Arrays.toString(monthImage.getPhotos()));
        args.putLongArray(FlextCalendarFragment.MONTH_IMAGES, monthImage.getPhotos());
        calendarView.setArguments(args);
        final FlextListener listener = new FlextListener() {
            @Override
            public void onClickDate(Date date) {
                Log.d(TAG, "onClick: " + date);
            }

            @Override
            public void onLongClickDate(Date date) {
                Log.d(TAG, "onLongClickDate: " + date);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                calendar.setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                long dateMills = calendar.getTimeInMillis();
//                startActivity(new Intent(mContext, ActionPickerDialog.class).putExtra("date", dateMills));
            }

            @Override
            public void onMonthChanged(int month, int year) {

            }

            @Override
            public void onCaldroidViewCreated() {
            }

            @Override
            public void onMonthSelected(int month) {

            }
        };
        calendarView.setListener(listener);
        calendarView.refreshView();
        replaceFragment(calendarView, getString(R.string.calendar));
        HashMap<DateTime, Events> map = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        for (int i = 0; i < 100; i++) {
            Events events = new Events();
            int color = themeUtil.getColor(themeUtil.colorPrimary(Prefs.getInstance(mContext).getReminderColor()));
            for (int j = 0; j < 9; j++) {
                events.addEvent("Event " + j, color, Events.Type.REMINDER);
            }
            map.put(FlextHelper.convertToDateTime(calendar.getTimeInMillis()), events);
            calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
        }
        calendarView.setEvents(map);
//        boolean isReminder = SharedPrefs.getInstance(mContext).getBoolean(Prefs.REMINDERS_IN_CALENDAR);
//        boolean isFeature = SharedPrefs.getInstance(mContext).getBoolean(Prefs.CALENDAR_FEATURE_TASKS);
//        calendarView.setEvents(new ReminderDataProvider(mContext, isReminder, isFeature).getEvents());
//        replace(calendarView, StartActivity.ACTION_CALENDAR);
//        SharedPrefs.getInstance(mContext).putInt(Prefs.LAST_CALENDAR_VIEW, 1);
        getActivity().invalidateOptionsMenu();
    }
}
