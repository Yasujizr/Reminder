package com.elementary.tasks.core.controller;

import android.content.Context;

import com.elementary.tasks.core.services.AlarmReceiver;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.TimeCount;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.reminder.models.Reminder;

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

class WeeklyEvent extends EventManager {

    WeeklyEvent(Reminder reminder, Context context) {
        super(reminder, context);
    }

    @Override
    public boolean start() {
        if (TimeCount.isCurrent(mReminder.getEventTime())) {
            new AlarmReceiver().enableReminder(mContext, mReminder.getUuId());
            return true;
        }
        return false;
    }

    @Override
    public boolean stop() {
        new AlarmReceiver().cancelAlarm(mContext, mReminder.getUniqueId());
        RealmDb.getInstance().saveObject(mReminder.setActive(false));
        return true;
    }

    @Override
    public boolean pause() {
        new AlarmReceiver().cancelAlarm(mContext, mReminder.getUniqueId());
        return true;
    }

    @Override
    public boolean skip() {
        return false;
    }

    @Override
    public boolean resume() {
        new AlarmReceiver().enableReminder(mContext, mReminder.getUuId());
        return true;
    }

    @Override
    public boolean next() {
        long time = TimeCount.getInstance(mContext).getNextWeekdayTime(TimeUtil.getDateTimeFromGmt(mReminder.getEventTime()), mReminder.getWeekdays(), 0);
        RealmDb.getInstance().saveObject(mReminder.setEventTime(TimeUtil.getGmtFromDateTime(time)));
        return start();
    }

    @Override
    public boolean onOff() {
        if (isActive()) {
            return stop();
        } else {
            return start();
        }
    }

    @Override
    public boolean isActive() {
        return mReminder.isActive();
    }

    @Override
    public boolean canSkip() {
        return mReminder.getRepeatLimit() != -1 && mReminder.getEventCount() < mReminder.getRepeatLimit();
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public void setDelay(int delay) {

    }
}
