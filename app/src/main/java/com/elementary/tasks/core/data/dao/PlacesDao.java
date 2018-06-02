package com.elementary.tasks.core.data.dao;

import com.elementary.tasks.core.data.models.Place;
import com.elementary.tasks.core.data.models.SmsTemplate;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import static androidx.room.OnConflictStrategy.REPLACE;

/**
 * Copyright 2018 Nazar Suhovich
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
@Dao
public interface PlacesDao {

    @Query("SELECT * FROM Place")
    LiveData<List<Place>> loadAll();

    @Query("SELECT * FROM Place")
    List<SmsTemplate> getAll();

    @Insert(onConflict = REPLACE)
    void insert(Place place);

    @Insert(onConflict = REPLACE)
    void insertAll(Place... places);

    @Delete
    void delete(Place place);

    @Query("SELECT * FROM Place WHERE id=:id")
    LiveData<Place> loadByKey(String id);

    @Query("SELECT * FROM Place WHERE id=:id")
    Place getByKey(String id);
}