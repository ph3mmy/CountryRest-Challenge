package com.oluwafemi.countries.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oluwafemi.countries.R;
import com.oluwafemi.countries.model.Country;

import java.util.ArrayList;
import java.util.List;

public class CountryRecyclerAdapter extends RecyclerView.Adapter<CountryRecyclerAdapter.CountryHolder> {

    private static final String TAG = "CountryRecyclerAdapter";

    private List<Country> countries;
    private Context mContext;

    public CountryRecyclerAdapter(Context mContext, List<Country> countries) {
        this.mContext = mContext;
        this.countries = countries;
    }

    public CountryRecyclerAdapter(Context mContext) {
        this.mContext = mContext;
        this.countries = new ArrayList<>();
    }

    @NonNull
    @Override
    public CountryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_item_country, parent, false);
        return new CountryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CountryHolder holder, int position) {

        if (countries != null) {
            Country country = countries.get(position);

            holder.tvCountryName.setText(country.getName());
            holder.tvCurrency.setText(country.getCurrency());
            holder.tvLanguage.setText(country.getLanguage());
        }
    }

    @Override
    public int getItemCount() {
        return countries.size();
    }

    public void addCountries(List<Country> newCountries) {
        if (countries != null) {
            countries.clear();
            countries.addAll(newCountries);
        }
    }

    public void removeItem(int position) {
        countries.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, countries.size());
    }

    class CountryHolder extends RecyclerView.ViewHolder {

        TextView tvCountryName, tvCurrency, tvLanguage;

        public CountryHolder(View itemView) {
            super(itemView);

            tvCountryName = itemView.findViewById(R.id.tv_country_name);
            tvCurrency = itemView.findViewById(R.id.tv_currency_name);
            tvLanguage = itemView.findViewById(R.id.tv_language);
        }
    }
}
