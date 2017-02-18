package com.elementary.tasks.birthdays;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.creators.CreateReminderActivity;
import com.elementary.tasks.databinding.FragmentEventsListBinding;
import com.elementary.tasks.reminder.RecyclerListener;
import com.elementary.tasks.reminder.ReminderPreviewActivity;
import com.elementary.tasks.reminder.ShoppingPreviewActivity;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.ArrayList;
import java.util.List;

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

public class EventsListFragment extends Fragment implements RecyclerListener {

    private FragmentEventsListBinding binding;
    private List<EventsItem> mDataList = new ArrayList<>();
    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    private Context mContext;

    public void setData(List<EventsItem> datas){
        this.mDataList = new ArrayList<>(datas);
    }

    public static EventsListFragment newInstance(int page) {
        EventsListFragment pageFragment = new EventsListFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mContext == null) {
            mContext = context;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContext == null) {
            mContext = activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventsListBinding.inflate(inflater, container, false);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAdapter();
    }

    public void loadAdapter(){
        CalendarEventsAdapter mAdapter = new CalendarEventsAdapter(mContext, mDataList);
        mAdapter.setEventListener(this);
        binding.recyclerView.setAdapter(mAdapter);
        reloadView();
    }

    private void reloadView() {
        int size = mDataList != null ? mDataList.size() : 0;
        if (size > 0){
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.emptyItem.setVisibility(View.GONE);
        } else {
            binding.recyclerView.setVisibility(View.GONE);
            binding.emptyItem.setVisibility(View.VISIBLE);
        }
    }

    private void showBirthdayLcam(BirthdayItem birthdayItem) {
        String[] items = {getString(R.string.edit), getString(R.string.delete)};
        Dialogues.showLCAM(mContext, item -> {
            switch (item){
                case 0:
                    editBirthday(birthdayItem);
                    break;
                case 1:
                    RealmDb.getInstance().deleteBirthday(birthdayItem);
                    loadAdapter();
                    break;
            }
        }, items);
    }

    private void editBirthday(BirthdayItem item) {
        startActivity(new Intent(mContext, AddBirthdayActivity.class)
                .putExtra(Constants.INTENT_ID, item.getKey()));
    }

    @Override
    public void onItemClicked(int position, View view) {
        Object object = mDataList.get(position).getObject();
        if (object instanceof BirthdayItem) {
            editBirthday((BirthdayItem) object);
        } else if (object instanceof Reminder){
            showReminder((Reminder) object);
        }
    }

    private void showReminder(Reminder object) {
        if (Reminder.isSame(object.getType(), Reminder.BY_DATE_SHOP)){
            mContext.startActivity(new Intent(mContext, ShoppingPreviewActivity.class)
                    .putExtra(Constants.INTENT_ID, object.getUuId()));
        } else {
            mContext.startActivity(new Intent(mContext, ReminderPreviewActivity.class)
                    .putExtra(Constants.INTENT_ID, object.getUuId()));
        }
    }

    private void editReminder(String uuId) {
        startActivity(new Intent(mContext, CreateReminderActivity.class).putExtra(Constants.INTENT_ID, uuId));
    }

    private void showActionDialog(Reminder reminder) {
        final String[] items = {getString(R.string.open), getString(R.string.edit),
                getString(R.string.move_to_trash)};
        Dialogues.showLCAM(mContext, item -> {
            switch (item){
                case 0:
                    showReminder(reminder);
                    break;
                case 1:
                    editReminder(reminder.getUuId());
                    break;
                case 3:
                    if (RealmDb.getInstance().moveToTrash(reminder.getUuId())) {
                        EventControl control = EventControlFactory.getController(mContext, reminder.setRemoved(true));
                        control.stop();
                        loadAdapter();
                    }
                    break;
            }
        }, items);
    }

    @Override
    public void onItemLongClicked(int position, View view) {
        Object object = mDataList.get(position).getObject();
        if (object instanceof BirthdayItem) {
            showBirthdayLcam((BirthdayItem) object);
        } else if (object instanceof Reminder) {
            showActionDialog((Reminder) object);
        }
    }

    @Override
    public void onItemSwitched(int position, View view) {

    }
}
