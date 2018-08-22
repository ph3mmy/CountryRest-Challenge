package com.oluwafemi.countries;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import com.oluwafemi.countries.adapter.CountryRecyclerAdapter;
import com.oluwafemi.countries.databinding.ActivityMainBinding;
import com.oluwafemi.countries.model.Country;
import com.oluwafemi.countries.viewmodel.CountryViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    ActivityMainBinding binding;
    private CountryViewModel countryViewModel;
    CountryRecyclerAdapter adapter;
    private Observer<List<Country>> countryObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // init viewModel
        countryViewModel = ViewModelProviders.of(this).get(CountryViewModel.class);

        // init recyclerview
        binding.rvAllCountries.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CountryRecyclerAdapter(this);
        binding.rvAllCountries.setAdapter(adapter);

        binding.btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchCountries();
            }
        });

        // fetch all countries
        fetchCountries();
    }

    private void fetchCountries() {
        hideProgressShowRecycler(false);
        if (isNetworkAvailable()) {
            countryObserver = new Observer<List<Country>>() {
                @Override
                public void onChanged(@Nullable List<Country> countries) {
                    setListToAdapter(countries);
                }
            };
            countryViewModel.getCountryLiveData().observe(this, countryObserver);

        } else {
            Snackbar.make(binding.getRoot(), "No Internet Connection, Please check your connection and try again", Snackbar.LENGTH_SHORT).show();
            showEmptyState();
        }
    }

    private void setListToAdapter(List<Country> countries) {
        Log.e(TAG, "setListToAdapter: size of countries == " + countries.size() );
        if (countries.size() > 0) {
            binding.tvTitle.setText("All Countries ("+countries.size()+")");
            adapter.addCountries(countries);
            adapter.notifyDataSetChanged();
//            adapter = new CountryRecyclerAdapter(this);
//            binding.rvAllCountries.setAdapter(adapter);
            hideProgressShowRecycler(true);
            countryViewModel.getCountryLiveData().removeObserver(countryObserver);
        } else {
            showEmptyState();
        }
    }

    private void showEmptyState() {
        binding.llError.setVisibility(View.VISIBLE);
        binding.tvErrEmpty.setText("No country at this time, Try Again");
        binding.rvAllCountries.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.GONE);
    }

    private void hideProgressShowRecycler(boolean showRecycler) {
        if (showRecycler) {
            binding.llError.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.GONE);
            binding.rvAllCountries.setVisibility(View.VISIBLE);
        } else {
            binding.llError.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.rvAllCountries.setVisibility(View.GONE);
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        return networkInfo != null && networkInfo.isConnected();
    }
}
