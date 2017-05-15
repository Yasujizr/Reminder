package com.backdoor.engine;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Copyright 2017 Nazar Suhovich
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
public class WorkerTest {
    @Test
    public void replaceNumbers() throws Exception {
        String input = "in half an hour read inbox";
        Recognizer recognizer = new Recognizer.Builder().setLocale(Locale.EN).setTimes(null).build();
        Model out = recognizer.parse(input);
        System.out.print(out);
    }
}