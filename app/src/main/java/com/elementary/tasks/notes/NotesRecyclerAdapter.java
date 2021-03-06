package com.elementary.tasks.notes;

import android.content.Context;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elementary.tasks.R;
import com.elementary.tasks.core.interfaces.SimpleListener;
import com.elementary.tasks.core.utils.AssetsUtil;
import com.elementary.tasks.core.utils.Configs;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.MeasureUtils;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.NoteListItemBinding;

import java.lang.ref.WeakReference;
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

public class NotesRecyclerAdapter extends RecyclerView.Adapter<NoteHolder> {

    private SimpleListener mEventListener;
    private List<NoteItem> mData = new ArrayList<>();

    public NotesRecyclerAdapter() {
    }

    public void notifyChanged(int position, String id) {
        NoteItem newItem = RealmDb.getInstance().getNote(id);
        mData.set(position, newItem);
        notifyItemChanged(position);
    }

    public void setData(List<NoteItem> list) {
        this.mData = list;
        notifyDataSetChanged();
    }

    public List<NoteItem> getData() {
        return mData;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public NoteItem getItem(int position) {
        return mData.get(position);
    }

    public void removeItem(int position) {
        if (position < mData.size()) {
            mData.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(0, mData.size());
        }
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new NoteHolder(NoteListItemBinding.inflate(inflater, parent, false).getRoot(), mEventListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final NoteHolder holder, final int position) {
        holder.setData(getItem(position));
    }

    @BindingAdapter({"loadNote"})
    public static void loadNote(TextView textView, NoteItem note) {
        String title = note.getSummary();
        if (TextUtils.isEmpty(title)) {
            textView.setVisibility(View.GONE);
            return;
        }
        Context context = textView.getContext();
        if (title.length() > 500) {
            String substring = title.substring(0, 500);
            title = substring + "...";
        }
        textView.setText(title);
        textView.setTypeface(AssetsUtil.getTypeface(context, note.getStyle()));
        textView.setTextSize(Prefs.getInstance(context).getNoteTextSize() + 12);
    }

    @BindingAdapter({"loadNoteCard"})
    public static void loadNoteCard(CardView cardView, int color) {
        cardView.setCardBackgroundColor(ThemeUtil.getInstance(cardView.getContext()).getNoteLightColor(color));
        if (Module.isLollipop()) {
            cardView.setCardElevation(Configs.CARD_ELEVATION);
        }
    }

    private static void setImage(ImageView imageView, byte[] image) {
        new Thread(() -> {
            Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
            imageView.post(() -> imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp,
                    imageView.getWidth(), imageView.getHeight(), false)));
        }).start();
    }

    private static void setClick(ImageView imageView, int position, String key) {
        Context context = imageView.getContext().getApplicationContext();
        imageView.setOnClickListener(view -> context.startActivity(new Intent(context, ImagePreviewActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(Constants.INTENT_ID, key)
                .putExtra(Constants.INTENT_DELETE, false)
                .putExtra(Constants.INTENT_POSITION, position)));
    }

    @BindingAdapter({"loadImage"})
    public static void loadImage(LinearLayout container, NoteItem item) {
        List<NoteImage> images = item.getImages();
        ImageView imageView = container.findViewById(R.id.noteImage);
        if (!images.isEmpty()) {
            imageView.setVisibility(View.VISIBLE);
            WeakReference<NoteImage> image = new WeakReference<>(images.get(0));
            setImage(imageView, image.get().getImage());
            int index = 1;
            LinearLayout horView = container.findViewById(R.id.imagesContainer);
            horView.removeAllViewsInLayout();
            while (index < images.size()) {
                ImageView imV = new ImageView(container.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MeasureUtils.dp2px(container.getContext(), 128),
                        MeasureUtils.dp2px(container.getContext(), 72));
                imV.setLayoutParams(params);
                setClick(imV, index, item.getKey());
                imV.setScaleType(ImageView.ScaleType.CENTER_CROP);
                horView.addView(imV);
                WeakReference<NoteImage> im = new WeakReference<>(images.get(index));
                setImage(imV, im.get().getImage());
                index++;
            }
        } else {
            imageView.setImageDrawable(null);
            imageView.setVisibility(View.GONE);
        }
    }

    public void setEventListener(SimpleListener eventListener) {
        mEventListener = eventListener;
    }
}
