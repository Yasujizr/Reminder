package com.elementary.tasks.creators.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.support.annotation.Nullable;

import com.elementary.tasks.R;
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

public abstract class TypeFragment extends Fragment {

    private Context mContext;
    private ReminderInterface mInterface;

    @Nullable
    public abstract Reminder prepare();

    @Override
    public Context getContext() {
        return mContext;
    }

    public ReminderInterface getInterface() {
        return mInterface;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mContext == null) {
            mContext = context;
        }
        if (mInterface == null) {
            mInterface = (ReminderInterface) context;
            setDefault();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContext == null) {
            mContext = activity;
        }
        if (mInterface == null) {
            mInterface = (ReminderInterface) activity;
            setDefault();
        }
    }

    private void setDefault() {
        mInterface.setExclusionAction(null);
        mInterface.setRepeatAction(null);
        mInterface.setEventHint(getString(R.string.remind_me));
        mInterface.setHasAutoExtra(false, null);
    }

    public boolean onBackPressed() {
        return true;
    }
}
