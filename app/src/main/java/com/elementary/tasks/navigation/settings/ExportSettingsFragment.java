package com.elementary.tasks.navigation.settings;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.SeekBar;

import com.elementary.tasks.R;
import com.elementary.tasks.core.cloud.Dropbox;
import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.services.AlarmReceiver;
import com.elementary.tasks.core.utils.CalendarUtils;
import com.elementary.tasks.core.utils.MemoryUtil;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding;
import com.elementary.tasks.databinding.FragmentSettingsExportBinding;
import com.elementary.tasks.navigation.settings.export.FragmentCloudDrives;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

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

public class ExportSettingsFragment extends BaseSettingsFragment {

    private static final int CALENDAR_CODE = 124;

    private List<CalendarUtils.CalendarItem> mDataList;
    private int mItemSelect;

    private FragmentSettingsExportBinding binding;
    private View.OnClickListener mCalendarClick = view -> changeExportToCalendarPrefs();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsExportBinding.inflate(inflater, container, false);
        initExportToCalendarPrefs();
        initEventDurationPrefs();
        initSelectCalendarPrefs();
        initExportToStockPrefs();
        initSettingsBackupPrefs();
        initAutoBackupPrefs();
        initAutoBackupIntervalPrefs();
        initClearDataPrefs();
        initCloudDrivesPrefs();
        return binding.getRoot();
    }

    private void initCloudDrivesPrefs() {
        binding.cloudsPrefs.setOnClickListener(view -> replaceFragment(new FragmentCloudDrives(), getString(R.string.cloud_services)));
    }

    private void initClearDataPrefs() {
        binding.cleanPrefs.setOnClickListener(view -> showCleanDialog());
    }

    private void showCleanDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(true);
        builder.setTitle(mContext.getString(R.string.clean));
        builder.setNeutralButton(R.string.local, (dialog, which) -> {
            File dir = MemoryUtil.getParent();
            deleteRecursive(dir);
        });
        builder.setNegativeButton(mContext.getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(R.string.all, (dialog, which) -> {
            File dir = MemoryUtil.getParent();
            deleteRecursive(dir);
            new Thread(() -> {
                Google gdx = Google.getInstance(mContext);
                Dropbox dbx = new Dropbox(mContext);
                if (SuperUtil.isConnected(mContext)) {
                    try {
                        gdx.getDrive().clean();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    dbx.cleanFolder();
                }
            }).start();

        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    private void initAutoBackupIntervalPrefs() {
        binding.syncIntervalPrefs.setOnClickListener(view -> showIntervalDialog());
        binding.syncIntervalPrefs.setDependentView(binding.autoBackupPrefs);
        showBackupInterval();
    }

    private void showBackupInterval() {
        CharSequence[] items = {mContext.getString(R.string.one_hour),
                mContext.getString(R.string.six_hours),
                mContext.getString(R.string.twelve_hours),
                mContext.getString(R.string.one_day),
                mContext.getString(R.string.two_days)};
        binding.syncIntervalPrefs.setDetailText(items[getIntervalPosition()].toString());
    }

    private void showIntervalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(true);
        builder.setTitle(mContext.getString(R.string.interval));
        CharSequence[] items = {mContext.getString(R.string.one_hour),
                mContext.getString(R.string.six_hours),
                mContext.getString(R.string.twelve_hours),
                mContext.getString(R.string.one_day),
                mContext.getString(R.string.two_days)};
        builder.setSingleChoiceItems(items, getIntervalPosition(), (dialog, item) -> mItemSelect = item);
        builder.setPositiveButton(mContext.getString(R.string.ok), (dialog, which) -> {
            saveIntervalPrefs();
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialogInterface -> mItemSelect = 0);
        dialog.setOnDismissListener(dialogInterface -> mItemSelect = 0);
        dialog.show();
    }

    private void saveIntervalPrefs() {
        if (mItemSelect == 0) {
            mPrefs.setAutoBackupInterval(1);
        } else if (mItemSelect == 1) {
            mPrefs.setAutoBackupInterval(6);
        } else if (mItemSelect == 2) {
            mPrefs.setAutoBackupInterval(12);
        } else if (mItemSelect == 3) {
            mPrefs.setAutoBackupInterval(24);
        } else if (mItemSelect == 4) {
            mPrefs.setAutoBackupInterval(48);
        }
        new AlarmReceiver().enableAutoSync(mContext);
        showBackupInterval();
    }

    private int getIntervalPosition() {
        int position;
        int interval = mPrefs.getAutoBackupInterval();
        switch (interval){
            case 1:
                position = 0;
                break;
            case 6:
                position = 1;
                break;
            case 12:
                position = 2;
                break;
            case 24:
                position = 3;
                break;
            case 48:
                position = 4;
                break;
            default:
                position = 0;
                break;
        }
        mItemSelect = position;
        return position;
    }

    private void initAutoBackupPrefs() {
        binding.autoBackupPrefs.setChecked(mPrefs.isAutoBackupEnabled());
        binding.autoBackupPrefs.setOnClickListener(view -> changeAutoBackupPrefs());
    }

    private void changeAutoBackupPrefs() {
        boolean isChecked = binding.autoBackupPrefs.isChecked();
        binding.autoBackupPrefs.setChecked(!isChecked);
        mPrefs.setAutoBackupEnabled(!isChecked);
        if (binding.autoBackupPrefs.isChecked()) {
            new AlarmReceiver().enableAutoSync(mContext);
        } else {
            new AlarmReceiver().cancelAutoSync(mContext);
        }
    }

    private void initSettingsBackupPrefs() {
        binding.syncSettingsPrefs.setChecked(mPrefs.isSettingsBackupEnabled());
        binding.syncSettingsPrefs.setOnClickListener(view -> changeSettingsBackupPrefs());
    }

    private void changeSettingsBackupPrefs() {
        boolean isChecked = binding.syncSettingsPrefs.isChecked();
        binding.syncSettingsPrefs.setChecked(!isChecked);
        mPrefs.setSettingsBackupEnabled(!isChecked);
    }

    private void initExportToStockPrefs() {
        binding.exportToStockPrefs.setChecked(mPrefs.isStockCalendarEnabled());
        binding.exportToStockPrefs.setOnClickListener(view -> changeExportToStockPrefs());
    }

    private void changeExportToStockPrefs() {
        boolean isChecked = binding.exportToStockPrefs.isChecked();
        binding.exportToStockPrefs.setChecked(!isChecked);
        mPrefs.setStockCalendarEnabled(!isChecked);
    }

    private void initSelectCalendarPrefs() {
        binding.selectCalendarPrefs.setOnClickListener(view -> showSelectCalendarDialog());
        binding.selectCalendarPrefs.setDependentView(binding.exportToCalendarPrefs);
    }

    private void initEventDurationPrefs() {
        binding.eventDurationPrefs.setOnClickListener(view -> showEventDurationDialog());
        binding.eventDurationPrefs.setDependentView(binding.exportToCalendarPrefs);
        showEventDuration();
    }

    private void showEventDuration() {
        binding.eventDurationPrefs.setDetailText(String.format(Locale.getDefault(), getString(R.string.x_minutes),
                String.valueOf(mPrefs.getCalendarEventDuration())));
    }

    private void showEventDurationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.event_duration);
        DialogWithSeekAndTitleBinding b = DialogWithSeekAndTitleBinding.inflate(LayoutInflater.from(mContext));
        b.seekBar.setMax(120);
        b.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                b.titleView.setText(String.format(Locale.getDefault(), getString(R.string.x_minutes), String.valueOf(progress)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        int duration = mPrefs.getCalendarEventDuration();
        b.seekBar.setProgress(duration);
        b.titleView.setText(String.format(Locale.getDefault(), getString(R.string.x_minutes), String.valueOf(duration)));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            mPrefs.setCalendarEventDuration(b.seekBar.getProgress());
            showEventDuration();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void changeExportToCalendarPrefs() {
        if (!Permissions.checkPermission(getActivity(), Permissions.READ_CALENDAR)) {
            Permissions.requestPermission(getActivity(), CALENDAR_CODE, Permissions.READ_CALENDAR);
            return;
        }
        boolean isChecked = binding.exportToCalendarPrefs.isChecked();
        binding.exportToCalendarPrefs.setChecked(!isChecked);
        mPrefs.setCalendarEnabled(!isChecked);
        if (binding.exportToCalendarPrefs.isChecked() && !showSelectCalendarDialog()) {
            mPrefs.setCalendarEnabled(false);
            binding.exportToCalendarPrefs.setChecked(false);
        }
    }

    private boolean showSelectCalendarDialog() {
        mDataList = CalendarUtils.getCalendarsList(mContext);
        if (mDataList == null || mDataList.isEmpty()) {
            return false;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.choose_calendar);
        builder.setSingleChoiceItems(new ArrayAdapter<CalendarUtils.CalendarItem>(mContext, android.R.layout.simple_list_item_single_choice) {
            @Override
            public int getCount() {
                return mDataList.size();
            }

            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_single_choice, parent, false);
                }
                CheckedTextView tvName = (CheckedTextView) convertView.findViewById(android.R.id.text1);
                tvName.setText(mDataList.get(position).getName());
                return convertView;
            }
        }, getCurrentPosition(), (dialogInterface, i) -> {
            dialogInterface.dismiss();
            mPrefs.setCalendarId(mDataList.get(i).getId());
        });
        builder.create().show();
        return true;
    }

    private int getCurrentPosition() {
        int position = 0;
        int id = mPrefs.getCalendarId();
        for (int i = 0; i < mDataList.size(); i++) {
            CalendarUtils.CalendarItem item = mDataList.get(i);
            if (item.getId() == id) {
                position = i;
                break;
            }
        }
        return position;
    }

    private void initExportToCalendarPrefs() {
        binding.exportToCalendarPrefs.setOnClickListener(mCalendarClick);
        binding.exportToCalendarPrefs.setChecked(mPrefs.isCalendarEnabled());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CALENDAR_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    changeExportToCalendarPrefs();
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mCallback.onTitleChange(getString(R.string.export_and_sync));
            mCallback.onFragmentSelect(this);
        }
    }
}
