package com.elementary.tasks.google_tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.elementary.tasks.core.cloud.GoogleTasks;
import com.elementary.tasks.core.utils.RealmDb;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

import java.io.IOException;
import java.util.List;
import java.util.Random;

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

public class SyncGoogleTasksAsync extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "SyncGoogleTasksAsync";
    private Context mContext;
    private TasksCallback mListener;

    public SyncGoogleTasksAsync(Context context, TasksCallback listener) {
        Log.d(TAG, "SyncGoogleTasksAsync: ");
        this.mContext = context;
        this.mListener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        GoogleTasks helper = new GoogleTasks(mContext);
        TaskLists lists = null;
        try {
            lists = helper.getTaskLists();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (lists != null && lists.size() > 0) {
            for (TaskList item : lists.getItems()) {
                String listId = item.getId();
                TaskListItem taskList = RealmDb.getInstance().getTaskList(listId);
                if (taskList != null) {
                    taskList.update(item);
                } else {
                    Random r = new Random();
                    int color = r.nextInt(15);
                    taskList = new TaskListItem(item, color);
                }
                RealmDb.getInstance().saveObject(taskList);
                List<Task> tasks = helper.getTasks(listId);
                for (Task task : tasks) {
                    TaskItem taskItem = RealmDb.getInstance().getTask(task.getId());
                    if (taskItem != null) {
                        taskItem.setListId(listId);
                        taskItem.update(task);
                    } else {
                        taskItem = new TaskItem(task, listId);
                    }
                    RealmDb.getInstance().saveObject(taskItem);
                }
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aVoid) {
        super.onPostExecute(aVoid);
//        UpdatesHelper.getInstance(mContext).updateTasksWidget();
        if (mListener != null) {
            mListener.onComplete();
        }
    }
}