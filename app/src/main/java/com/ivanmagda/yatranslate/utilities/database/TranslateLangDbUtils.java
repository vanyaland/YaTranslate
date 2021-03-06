/**
 * Copyright (c) 2017 Ivan Magda
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ivanmagda.yatranslate.utilities.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.ivanmagda.yatranslate.model.core.TranslateLangItem;
import com.ivanmagda.yatranslate.utilities.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

import static com.ivanmagda.yatranslate.data.TranslateContract.LanguageEntry;

public final class TranslateLangDbUtils {

    private TranslateLangDbUtils() {
    }

    /**
     * Inserts the new languages data into the database.
     *
     * @param langItems List of languages to be persist.
     */
    public static void persistLangs(@NonNull final Context context, List<TranslateLangItem> langItems) {
        if (ArrayUtils.isEmpty(langItems)) return;

        List<ContentValues> valuesList = new ArrayList<>(langItems.size());

        for (TranslateLangItem anItem : langItems) {
            ContentValues contentValues = new ContentValues(4);

            contentValues.put(LanguageEntry.COLUMN_TRANSLATE_FROM_KEY, anItem.getFromLang());
            contentValues.put(LanguageEntry.COLUMN_TRANSLATE_TO_KEY, anItem.getToLang());
            contentValues.put(LanguageEntry.COLUMN_TRANSLATE_FROM_NAME, anItem.getFromLangName());
            contentValues.put(LanguageEntry.COLUMN_TRANSLATE_TO_NAME, anItem.getToLangName());

            valuesList.add(contentValues);
        }

        ContentValues[] cvArray = new ContentValues[valuesList.size()];
        valuesList.toArray(cvArray);

        // Delete old data and insert the new one.
        context.getContentResolver().delete(LanguageEntry.CONTENT_URI, null, null);
        context.getContentResolver().bulkInsert(LanguageEntry.CONTENT_URI, cvArray);
    }

    public static List<TranslateLangItem> buildItemsFromCursor(@NonNull final Cursor cursor) {
        List<TranslateLangItem> items = new ArrayList<>(cursor.getCount());

        while (cursor.moveToNext()) {
            String fromLangKey = cursor.getString(
                    cursor.getColumnIndexOrThrow(LanguageEntry.COLUMN_TRANSLATE_FROM_KEY));
            String toLangKey = cursor.getString(
                    cursor.getColumnIndexOrThrow(LanguageEntry.COLUMN_TRANSLATE_TO_KEY));
            String fromLangName = cursor.getString(
                    cursor.getColumnIndexOrThrow(LanguageEntry.COLUMN_TRANSLATE_FROM_NAME));
            String toLangName = cursor.getString(
                    cursor.getColumnIndexOrThrow(LanguageEntry.COLUMN_TRANSLATE_TO_NAME));

            items.add(new TranslateLangItem(fromLangKey, toLangKey, fromLangName, toLangName));
        }

        return items;
    }

    public static List<TranslateLangItem> searchForLangWithKey(@NonNull final Context context,
                                                               @NonNull final String key) {
        Uri uri = LanguageEntry.buildUriWithText(key);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            List<TranslateLangItem> items = buildItemsFromCursor(cursor);
            cursor.close();

            return items;
        }

        if (cursor != null) {
            cursor.close();
        }

        return null;
    }
}
