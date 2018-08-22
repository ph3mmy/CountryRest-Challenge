package com.oluwafemi.countries.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.oluwafemi.countries.io.HttpHelper;
import com.oluwafemi.countries.model.Country;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The ViewModel class is designed to store and manage UI-related data in a lifecycle conscious way.
 * It allows data to survive configuration changes such as screen rotations.
 * AndroidViewModel makes the Context available to the viewModel class
 * source = https://developer.android.com/topic/libraries/architecture/viewmodel
 */
public class CountryViewModel extends AndroidViewModel {

    private static final String TAG = "CountryViewModel";

    public static MutableLiveData<List<Country>> countryLiveData;
    private static  final String requestUrl = "https://restcountries.eu/rest/v2/all";

    public CountryViewModel(@NonNull Application application) {
        super(application);
    }


    public LiveData<List<Country>> getCountryLiveData() {
        if (countryLiveData == null) {
            countryLiveData = new MutableLiveData<List<Country>>();
//            loadCountries();
            new CountriesAsyncTask(requestUrl).execute();
        }
        return countryLiveData;
    }

    static class CountriesAsyncTask extends AsyncTask<String, Void, List<Country>> {

        String url;

        CountriesAsyncTask(String url) {
            this.url = url;
        }

        @Override
        protected List<Country> doInBackground(String... strings) {

            List<Country> countries = new ArrayList<>();

            HttpHelper httpHelper = new HttpHelper();
            String response = httpHelper.makeNetworkRequest(url);

            if (response != null && !response.isEmpty()) {
                try {
                    JSONArray countryArray = new JSONArray(response);
                    for (int i = 0; i < countryArray.length(); i++) {
                        JSONObject countryObj = countryArray.getJSONObject(i);
                        String countryName = countryObj.getString("name");
                        JSONArray currencies = countryObj.getJSONArray("currencies");
                        String countryCurrency = currencies.getJSONObject(0).getString("name"); // get only first currency
                        JSONArray languages = countryObj.getJSONArray("languages");
                        String language = languages.getJSONObject(0).getString("name"); // get only first language

                        Country country = new Country();
                        country.setName(countryName);
                        country.setCurrency(countryCurrency);
                        country.setLanguage(language);

                        countries.add(country);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return countries;
        }

        @Override
        protected void onPostExecute(List<Country> countries) {
            super.onPostExecute(countries);
            countryLiveData.setValue(countries);
        }
    }

}
