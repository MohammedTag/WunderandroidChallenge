package com.example.assignment.wunder.presenters.map_presenter;


import com.example.assignment.wunder.models.Vehicle;


import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


public interface mapViewListeners {
    void onCarsFeedSuccess(Call<List<Vehicle>> call, Response<List<Vehicle>> response);
    void onCarsFeedFailed(Call<List<Vehicle>> call, Throwable t);
}
