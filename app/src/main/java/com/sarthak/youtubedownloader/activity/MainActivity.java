package com.sarthak.youtubedownloader.activity;

import static java.util.Objects.nonNull;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import com.github.kiulian.downloader.model.search.SearchResultItem;
import com.github.kiulian.downloader.model.search.SearchResultVideoDetails;
import com.sarthak.youtubedownloader.R;
import com.sarthak.youtubedownloader.adapters.SearchResultAdapter;
import com.sarthak.youtubedownloader.constants.IntentKeys;
import com.sarthak.youtubedownloader.service.NetworkHelper;
import com.sarthak.youtubedownloader.service.SearchService;
import com.sarthak.youtubedownloader.util.CommonUtil;
import com.sarthak.youtubedownloader.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private EditText searchBarEditText;
    private ListView resultsListView;
    private SearchResultAdapter searchResultAdapter;
    private LinearLayout rootLinearLayout;
    private Button searchButton;
    private ProgressBar loadingCircularBar;
    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar("Youtube Downloader");

        searchBarEditText = findViewById(R.id.searchBar);
        rootLinearLayout = findViewById(R.id.linear_layout_main);
        loadingCircularBar = findViewById(R.id.search_result_progress_bar);

        searchButton = findViewById(R.id.searchButton);
        updateSearchButton(searchBarEditText);
        Button clearSearchTextButton = getClearSearchTextButton(searchBarEditText);
        addTextChangeListener(searchBarEditText, clearSearchTextButton);
        setupUI(rootLinearLayout, searchBarEditText, clearSearchTextButton);
        searchResultAdapter = new SearchResultAdapter(this, new ArrayList<>());
        addDataChangeListenerToListViewAdapter();
        resultsListView = getResultsListView(searchResultAdapter);

        requestStoragePermission();
        checkNetworkConnection();

        tempTestingOnApplicationLaunch();
    }

    @Override
    protected void setupToolbar(String title) {
        super.setupToolbar(title);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // This is the ID for the Up button
//                onBackPressed(); // Go back to the previous activity
                updateSearchResultAdapterList(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    @Override
    public OnBackInvokedDispatcher getOnBackInvokedDispatcher() {

        return super.getOnBackInvokedDispatcher();
    }

    private void addDataChangeListenerToListViewAdapter() {
        searchResultAdapter.setDataChangeListener(new SearchResultAdapter.DataChangeListener() {
            @Override
            public void onDataChanged(int listItemCount) {
                if (listItemCount > 0) {
                    resultsListView.setVisibility(View.VISIBLE);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setDisplayShowHomeEnabled(true);
                }
                else {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    getSupportActionBar().setDisplayShowHomeEnabled(false);
                    resultsListView.setVisibility(View.GONE);
                }
            }
        });
    }


    private void addTextChangeListener(EditText searchBarEditText, Button clearSearchTextButton) {
        searchBarEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    clearSearchTextButton.setVisibility(View.GONE);
                }
                else {
                    clearSearchTextButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private Button getClearSearchTextButton(EditText searchBarEditText) {
        Button clearSearchTextButton = findViewById(R.id.clear_search_text);
        clearSearchTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBarEditText.setText("");

                if (searchResultAdapter.getCount() > 0) {
                    searchBarEditText.requestFocus();
                    showKeyboard(MainActivity.this, searchBarEditText);
                }
                

            }
        });
        clearSearchTextButton.setVisibility(View.GONE);
        return clearSearchTextButton;
    }

    @NonNull
    private ListView getResultsListView(SearchResultAdapter searchResultAdapter) {
        ListView resultsListView = findViewById(R.id.resultList);
        resultsListView.setAdapter(searchResultAdapter);

        resultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                SearchResultItem searchResultItem = (SearchResultItem) parent.getItemAtPosition(position);

                Intent openDownloadFileActivityIntent = new Intent(MainActivity.this, DownloadFileActivity.class);
                openDownloadFileActivityIntent.putExtra(IntentKeys.SEARCH_RESULT_ITEM_EXTRA, JsonUtil.convertObjToJson(searchResultItem));

                startActivity(openDownloadFileActivityIntent);
            }
        });

        return resultsListView;
    }

    private void updateSearchButton(EditText searchBarEditText) {

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingCircularBar.setVisibility(View.VISIBLE);
                String query = CommonUtil.resolve(() -> searchBarEditText.getText().toString(), null);
                if (nonNull(query)) {
                    new SearchTask().execute(searchBarEditText.getText().toString());
                }
            }
        });
    }

    private class SearchTask extends AsyncTask<String, Void, List<SearchResultVideoDetails>> {

        @Override
        protected List<SearchResultVideoDetails> doInBackground(String... queries) {
            SearchService searchService = new SearchService(MainActivity.this);
            return searchService.getVideoTitles(queries[0]);
        }

        @Override
        protected void onPostExecute(List<SearchResultVideoDetails> searchResultItems) {
            if (CommonUtil.isEmpty(searchResultItems)) {
                CommonUtil.toastOnMainThread(MainActivity.this, "No Results found", Toast.LENGTH_LONG);
            }
            updateSearchResultAdapterList(searchResultItems);
            loadingCircularBar.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            loadingCircularBar.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled(List<SearchResultVideoDetails> searchResultVideoDetails) {
            super.onCancelled(searchResultVideoDetails);
            loadingCircularBar.setVisibility(View.GONE);
        }
    }

    private void updateSearchResultAdapterList(List<SearchResultVideoDetails> searchResultItems) {
        searchResultAdapter.clear();
        resultsListView.smoothScrollToPosition(0);
        CommonUtil.nullSafeList(searchResultItems).forEach(s -> {
            searchResultAdapter.add(s);
        });
        searchResultAdapter.notifyDataSetChanged();
    }

    public void setupUI(View view, final EditText editText, Button clearSearchTextButton) {
        // Set up touch listener for non-text box views to hide keyboard
        if (view.getId() != editText.getId() && view.getId() != clearSearchTextButton.getId()) {
            view.setOnTouchListener((v, event) -> {
                searchBarEditText.clearFocus();
                closeKeyboard(MainActivity.this, v);
                return false;
            });
        }

        // If a layout container, iterate over its children to apply the listener
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView, editText, clearSearchTextButton);
            }
        }
    }

    private void tempTestingOnApplicationLaunch() {
    }

    private void checkNetworkConnection() {
        if (NetworkHelper.isNotConnected(this)) {
            Toast.makeText(this, "No Internet", Toast.LENGTH_LONG).show();
        }
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_MEDIA_VIDEO}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    private static void closeKeyboard(Activity activity, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private static void showKeyboard(Activity activity, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}