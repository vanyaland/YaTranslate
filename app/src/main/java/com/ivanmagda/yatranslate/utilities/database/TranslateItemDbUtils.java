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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ivanmagda.yatranslate.model.core.TranslateItem;
import com.ivanmagda.yatranslate.model.core.TranslateLangItem;
import com.ivanmagda.yatranslate.utilities.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

import static com.ivanmagda.yatranslate.data.TranslateContract.HistoryEntry;
import static com.ivanmagda.yatranslate.data.TranslateContract.HistoryEntry.COLUMN_LANG_TRANSLATE_FROM;
import static com.ivanmagda.yatranslate.data.TranslateContract.HistoryEntry.COLUMN_LANG_TRANSLATE_TO;
import static com.ivanmagda.yatranslate.data.TranslateContract.HistoryEntry.COLUMN_TEXT_TO_TRANSLATE;
import static com.ivanmagda.yatranslate.data.TranslateContract.HistoryEntry.COLUMN_TEXT_TRANSLATED;
import static com.ivanmagda.yatranslate.data.TranslateContract.HistoryEntry.buildUriWithText;

public final class TranslateItemDbUtils {

    private TranslateItemDbUtils() {
    }

    // Public.

    public static List<TranslateItem> addToHistory(@NonNull final Context context,
                                                   @NonNull final List<TranslateItem> items) {
        List<TranslateItem> itemsToInsert = new ArrayList<>(items);

        for (TranslateItem anTranslateItem : itemsToInsert) {
            long insertedId = addToHistory(context, anTranslateItem);
            if (insertedId != TranslateItem.ID_NOT_FOUND) {
                anTranslateItem.setId(insertedId);
            }
        }

        return itemsToInsert;
    }

    public static void toggleFavorite(@NonNull final Context context,
                                      @NonNull final TranslateItem translateItem) {
        final long id = translateItem.getId();
        if (id == TranslateItem.ID_NOT_FOUND) throw new IllegalArgumentException("Illegal item id");

        Uri uri = HistoryEntry.buildUriWithId(translateItem.getId());

        translateItem.toggleFavorite();
        ContentValues contentValues = toContentValues(translateItem);

        context.getContentResolver().update(uri, contentValues, null, null);
    }

    /**
     * Build TranslateItem from the current position of the Cursor.
     *
     * @param cursor The cursor from which data will be extracted.
     * @return Created TranslateItem from the cursor value.
     */
    public static
    @Nullable
    TranslateItem buildFromCursor(@NonNull final Context context,
                                  @NonNull final Cursor cursor) {
        if (cursor.getCount() == 0) {
            return null;
        }

        long id = cursor.getLong(cursor.getColumnIndexOrThrow(HistoryEntry._ID));
        boolean isFavorite = cursor
                .getInt(cursor.getColumnIndexOrThrow(HistoryEntry.COLUMN_FAVORITE)) != 0;
        String textToTranslate = cursor.getString(
                cursor.getColumnIndexOrThrow(COLUMN_TEXT_TO_TRANSLATE));
        String translatedText = cursor.getString(
                cursor.getColumnIndexOrThrow(COLUMN_TEXT_TRANSLATED));
        String fromLang = cursor.getString(
                cursor.getColumnIndexOrThrow(COLUMN_LANG_TRANSLATE_FROM));
        String toLang = cursor.getString(
                cursor.getColumnIndexOrThrow(COLUMN_LANG_TRANSLATE_TO));
        TranslateLangItem langItem = getTranslateLangItemFromKeys(context, fromLang, toLang);

        return new TranslateItem(id, isFavorite, textToTranslate, translatedText, langItem);
    }

    private static long addToHistory(@NonNull final Context context,
                                     @NonNull final TranslateItem translateItem) {
        if (!isExist(context, translateItem)) {
            Uri uri = context.getContentResolver()
                    .insert(HistoryEntry.CONTENT_URI, toContentValues(translateItem));
            return ContentUris.parseId(uri);
        }

        return TranslateItem.ID_NOT_FOUND;
    }

    public static TranslateItem searchForTranslation(@NonNull final Context context,
                                                     String text,
                                                     TranslateLangItem langItem) {
        if (TextUtils.isEmpty(text) || langItem == null) return null;

        Uri searchUri = buildUriWithText(text);
        Cursor cursor = context.getContentResolver().query(searchUri, null, null, null, null);
        if (cursor == null) return null;

        List<TranslateItem> list = buildTranslateItems(context, cursor);
        if (ArrayUtils.isEmpty(list)) return null;

        for (TranslateItem anItem : list) {
            if (anItem.getTextToTranslate().equals(text) &&
                    anItem.getTranslateLangItem().getToLang().equals(langItem.getToLang()) &&
                    anItem.getTranslateLangItem().getFromLang().equals(langItem.getFromLang())) {
                return anItem;
            }
        }

        cursor.close();

        return null;
    }

    public static void clearHistory(@NonNull final Context context) {
        context.getContentResolver().delete(HistoryEntry.CONTENT_URI, null, null);
    }

    // Private.

    private static ContentValues toContentValues(@NonNull final TranslateItem item) {
        ContentValues contentValues = new ContentValues(5);

        contentValues.put(HistoryEntry.COLUMN_TEXT_TO_TRANSLATE, item.getTextToTranslate());
        contentValues.put(HistoryEntry.COLUMN_TEXT_TRANSLATED, item.getTranslatedText());
        contentValues.put(HistoryEntry.COLUMN_LANG_TRANSLATE_FROM,
                item.getTranslateLangItem().getFromLang());
        contentValues.put(HistoryEntry.COLUMN_LANG_TRANSLATE_TO,
                item.getTranslateLangItem().getToLang());
        contentValues.put(HistoryEntry.COLUMN_FAVORITE, item.isFavorite() ? 1 : 0);

        if (item.getId() != TranslateItem.ID_NOT_FOUND) {
            contentValues.put(HistoryEntry._ID, item.getId());
        }

        return contentValues;
    }

    private static List<TranslateItem> buildTranslateItems(@NonNull final Context context,
                                                           Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        }

        List<TranslateItem> items = new ArrayList<>(cursor.getCount());

        while (cursor.moveToNext()) {
            TranslateItem translateItem = buildFromCursor(context, cursor);
            if (translateItem != null) {
                items.add(translateItem);
            }
        }

        return items;
    }

    private static boolean isExist(@NonNull final Context context,
                                   @NonNull final TranslateItem translateItem) {
        Uri searchUri = buildUriWithText(translateItem.getTextToTranslate());
        Cursor cursor = context.getContentResolver().query(searchUri, null, null, null, null);

        List<TranslateItem> list = buildTranslateItems(context, cursor);
        if (ArrayUtils.isEmpty(list)) return false;

        for (TranslateItem anItem : list) {
            if (anItem.equals(translateItem)) return true;
        }

        cursor.close();

        return false;
    }

    @SuppressWarnings("ConstantConditions")
    private static TranslateLangItem getTranslateLangItemFromKeys(@NonNull final Context context,
                                                                  @NonNull final String fromKey,
                                                                  @NonNull final String toKey) {
        List<TranslateLangItem> items = TranslateLangDbUtils.searchForLangWithKey(context, fromKey);
        List<TranslateLangItem> fromLangItems = TranslateLangDbUtils.searchForLangWithKey(context, toKey);

        if (ArrayUtils.isEmpty(items) || ArrayUtils.isEmpty(fromLangItems)) {
            return null;
        }

        if (!ArrayUtils.isEmpty(fromLangItems)) {
            items.addAll(fromLangItems);
        }

        String fromLangName = null;
        String toLangName = null;

        for (TranslateLangItem anItem : items) {
            if (!TextUtils.isEmpty(fromLangName) && !TextUtils.isEmpty(toLangName)) break;

            if (anItem.getFromLang().equals(fromKey)) {
                fromLangName = anItem.getFromLangName();
            } else if (anItem.getToLang().equals(fromKey)) {
                fromLangName = anItem.getToLangName();
            }

            if (anItem.getFromLang().equals(toKey)) {
                toLangName = anItem.getToLangName();
            } else if (anItem.getToLang().equals(toKey)) {
                toLangName = anItem.getToLangName();
            }
        }

        /* This probably could happen if we doesn't fetch supported languages yet. */
        if (TextUtils.isEmpty(fromLangName) || TextUtils.isEmpty(toLangName)) {
            return new TranslateLangItem(fromKey, toKey, null, null);
        }

        return new TranslateLangItem(fromKey, toKey, fromLangName, toLangName);
    }
}
