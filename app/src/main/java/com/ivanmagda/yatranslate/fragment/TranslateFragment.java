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

package com.ivanmagda.yatranslate.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ivanmagda.network.helper.GenericAsyncTaskLoader;
import com.ivanmagda.network.utils.Utils;
import com.ivanmagda.yatranslate.R;
import com.ivanmagda.yatranslate.api.YandexTranslateApi;
import com.ivanmagda.yatranslate.data.TranslateItem;
import com.ivanmagda.yatranslate.data.TranslateLangItem;
import com.ivanmagda.yatranslate.utils.FragmentUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TranslateFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<TranslateItem>> {

    /**
     * Identifies a particular Loader being used in this component.
     */
    private static final int TRANSLATE_LOADER_ID = 101;

    private static final String TRANSLATE_INPUT_STATE_KEY = "TRANSLATE_INPUT_STATE_KEY";
    private static final String TRANSLATE_RESULT_STATE_KEY = "TRANSLATE_RESULT_STATE_KEY";
    private static final String TRANSLATE_LANG_STATE_KEY = "TRANSLATE_LANG_STATE_KEY";

    public static final String TAG = TranslateFragment.class.getSimpleName();

    @BindView(R.id.et_translate_input)
    EditText mTranslateInput;

    @BindView(R.id.bt_translate)
    ImageButton mTranslateButton;

    @BindView(R.id.tv_translate_result)
    TextView mTranslateResultTextView;

    private String mTextToTranslate = "";
    private List<TranslateItem> mTranslateResults;
    private TranslateLangItem mTranslateLang = new TranslateLangItem("en", "ru");

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TranslateFragment() {
    }

    @SuppressWarnings("unused")
    public static TranslateFragment newInstance() {
        return new TranslateFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mTextToTranslate = savedInstanceState.getString(TRANSLATE_INPUT_STATE_KEY);
            mTranslateResults = savedInstanceState.getParcelableArrayList(TRANSLATE_RESULT_STATE_KEY);
            mTranslateLang = savedInstanceState.getParcelable(TRANSLATE_LANG_STATE_KEY);
        }

        getLoaderManager().initLoader(TRANSLATE_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_translate, container, false);
        ButterKnife.bind(this, view);

        setup();

        return view;
    }

    private void setup() {
        mTranslateInput.setText(mTextToTranslate);
        mTranslateInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mTextToTranslate = s.toString();
            }
        });

        mTranslateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentUtils.hideSoftKeyboard(getActivity());
                if (!TextUtils.isEmpty(mTextToTranslate)) queryForTranslate();
            }
        });

        updateResults();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(TRANSLATE_INPUT_STATE_KEY, mTextToTranslate);
        outState.putParcelableArrayList(TRANSLATE_RESULT_STATE_KEY,
                mTranslateResults != null ? new ArrayList<Parcelable>(mTranslateResults) : null);
        outState.putParcelable(TRANSLATE_LANG_STATE_KEY, mTranslateLang);
    }

    @Override
    public Loader<List<TranslateItem>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case TRANSLATE_LOADER_ID:
                return new GenericAsyncTaskLoader<>(
                        getActivity(),
                        YandexTranslateApi.getTranslation(mTextToTranslate, mTranslateLang)
                );
            default:
                throw new IllegalArgumentException("Unsupported loader with id: " + String.valueOf(id));
        }
    }

    @Override
    public void onLoadFinished(Loader<List<TranslateItem>> loader, List<TranslateItem> translateItems) {
        mTranslateResults = translateItems;
        updateResults();
    }

    @Override
    public void onLoaderReset(Loader<List<TranslateItem>> loader) {
        mTranslateResults = null;
        updateResults();
    }

    // Private helpers.

    private void queryForTranslate() {
        if (!Utils.isOnline(getContext())) {
            Toast.makeText(getActivity(), R.string.no_internet_connection_message, Toast.LENGTH_SHORT).show();
        } else {
            getLoaderManager().restartLoader(TRANSLATE_LOADER_ID, null, this);
        }
    }

    private void updateResults() {
        if (mTranslateResults != null) {
            StringBuilder stringBuilder = new StringBuilder(100);
            for (TranslateItem translateItem : mTranslateResults) {
                stringBuilder.append(translateItem.getTranslatedText()).append("\n");
            }
            mTranslateResultTextView.setText(stringBuilder.toString().trim());
        }
    }

}
