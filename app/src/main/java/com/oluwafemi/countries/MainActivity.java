package com.oluwafemi.countries;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MotionEvent;
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
    private Paint paint = new Paint();
    private boolean swipeBack;
    private boolean isSwipeActive;
    private int activeSwipedPosition;
    public static final int FLING_THRESHOLD_VELOCITY = 2000;

    RecyclerView.ViewHolder mHolder = null;
    private static final float SWIPE_ANCHOR_POINT = -1080;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // init viewModel
        countryViewModel = ViewModelProviders.of(this).get(CountryViewModel.class);

        // init recyclerview
        binding.rvAllCountries.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CountryRecyclerAdapter(this);
        binding.rvAllCountries.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        binding.rvAllCountries.setAdapter(adapter);

        // add itemTouchHelper to recycler for swiping
        addItemTouchHelper();

        binding.btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchCountries();
            }
        });

        // fetch all countries
        fetchCountries();
    }

    private void addItemTouchHelper() {

        final ItemTouchHelper.SimpleCallback helperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                Log.e(TAG, "onSwiped: swiped dir == " + direction);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                    /*try {

                        // TODO: anchor dX == -1080.0
                        Bitmap icon;
                        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                            View itemView = viewHolder.itemView;
                            float height = (float) itemView.getBottom() - (float) itemView.getTop();
                            float width = height / 3;
                            viewHolder.itemView.setTranslationX(dX / 3);

                            paint.setColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
                            RectF background = new RectF((float) itemView.getRight() + dX / 5, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                            c.drawRect(background, paint);
                            icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_del_bomb);
                            RectF icon_dest = new RectF((float) (itemView.getRight() + dX / 7), (float) itemView.getTop() + width,
                                    (float) itemView.getRight() + dX / 20, (float) itemView.getBottom() - width);
                            c.drawBitmap(icon, null, icon_dest, paint);

                            Log.e(TAG, "onChildDraw: Swiped DX == " + dX);
                        } else {
                            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                            Log.e(TAG, "onChildDraw: ELSE block DX == " + dX);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/

            }

        };

        ItemTouchHelper.Callback iCallback = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

                int dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                int swipeFlags = ItemTouchHelper.START;
//                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlag, swipeFlags);
//                return 0;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }

            }

           /* public int convertToAbsoluteDirection(int flags, int layoutDirection) {
                if (swipeBack) {
                    swipeBack = false;
                    return 0;
                }
                return super.convertToAbsoluteDirection(flags, layoutDirection);
            }*/

        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(iCallback);
        itemTouchHelper.attachToRecyclerView(binding.rvAllCountries);
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
        if (countries.size() > 0) {
            binding.tvTitle.setText("All Countries (" + countries.size() + ")");
            adapter.addCountries(countries);
            adapter.notifyDataSetChanged();
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

    @SuppressLint("ClickableViewAccessibility")
    private void setTouchListener(Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, float dY, int actionState, final boolean isCurrentlyActive) {

        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (dX < SWIPE_ANCHOR_POINT || dX > SWIPE_ANCHOR_POINT) {
                    swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
                }
                return false;
            }


        });


        try {

            Bitmap icon;
                View itemView = viewHolder.itemView;
                float height = (float) itemView.getBottom() - (float) itemView.getTop();
                float width = height / 3;

                if (dX == SWIPE_ANCHOR_POINT) {
                    viewHolder.itemView.setTranslationX(dX / 3);
                    paint.setColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
                    RectF background = new RectF((float) itemView.getRight() + dX / 5, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                    c.drawRect(background, paint);
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_del_bomb);
                    RectF icon_dest = new RectF((float) (itemView.getRight() + dX / 7), (float) itemView.getTop() + width,
                            (float) itemView.getRight() + dX / 20, (float) itemView.getBottom() - width);
                    c.drawBitmap(icon, null, icon_dest, paint);
                    isSwipeActive = true;
                    activeSwipedPosition = viewHolder.getAdapterPosition();
                } else if (dX < SWIPE_ANCHOR_POINT) {
                    itemView.setTranslationX(dX);
                    paint.setColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
                    RectF background = new RectF((float) itemView.getRight() + dX/3, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                    c.drawRect(background, paint);
                    deleteSwipedItem(viewHolder.getAdapterPosition());

                } else {
                    itemView.setTranslationX(dX/3);
                    paint.setColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
                    RectF background = new RectF((float) itemView.getRight() + dX/3, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                    c.drawRect(background, paint);


            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void deleteSwipedItem(int adapterPosition) {
        adapter.removeItem(adapterPosition);
    }

    public boolean isNetworkAvailable () {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            // if no network is available networkInfo will be null
            // otherwise check if we are connected
            return networkInfo != null && networkInfo.isConnected();
        }
    }
