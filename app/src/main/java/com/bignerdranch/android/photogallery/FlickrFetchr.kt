package com.bignerdranch.android.photogallery

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bignerdranch.android.photogallery.api.FlickrApi
import com.bignerdranch.android.photogallery.api.FlickrResponse
import com.bignerdranch.android.photogallery.api.PhotoResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


/**To get more of A Repository Pattern
 * We moved it from our Fragment Class to here
 * */

//Tag used to Execute the Web Request In the Call Object flickrHomePageRequest
private const val TAG = "PhotoGalleryFragment"


/**FlickrFetchr Will Wrap Most Of the Networking Code in photo Gallery */
class FlickrFetchr {


    private val flickrApi: FlickrApi


    init {

        /**
         * Creating a Retrofit Object So that I can make web request based on the API Interface (FlickrApi) I defined
         * */
        /**
         * Notice How Retrofit.Builder() is a Fluent Interface therefore we were able to define the baseURL and build it
         * Note: Retrofit Does Not Generate code at Compile time instead it does all the work at runtime
         * Also Adding a Scalars Converter so that Retrofit can convert OkHttp Objects to Strings Since we return one in our Custom API
         * */

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
                //Now using a Gson Converter Instead of a Scalar Converter
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        /**     Using our Retrofit Object To Create a Instance of OUR INTERFACE FlickrApi
        When You call retrofit.create() Retrofit uses the information in the API interface to create and instantiate an anonymous class
        That implements our interface on the fly*/
        flickrApi = retrofit.create(FlickrApi::class.java)
    }

    /**Enqueues The Network Request and wraps the result in LiveData */
    fun fetchPhotos(): LiveData<List<GalleryItem>>
    {
        //Used to Save the response.body()
        /**Which when the Running Request on the Background Thread is Complete ,
         * Retrofit will store it in response.body() */
        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()

        /**Now we are going to execute a web request and log the results
         * Using the method we defined in the Interface fetchContents (RetroFit Will create the implementation and return the request
         * As well as convert it to a string type since we are using Squares Scalar Converter
         * */
        val flickrRequest: Call<FlickrResponse> = flickrApi.fetchPhotos()

        /**Using a Kotlin anonymous Class To determine when a Failure or a response was received using Log class
         * Call.enqueue() executes the web request represented by the Call object.
         * It Executes the request on a background thread
         * */
        flickrRequest.enqueue(object : Callback<FlickrResponse> {
            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
                Log.e(TAG,"Failed To Fetch Photos", t)
            }

            override fun onResponse(call: Call<FlickrResponse>, response: Response<FlickrResponse>) {
                Log.d(TAG, "Response Received: ${response.body()}")

                val flickrResponse: FlickrResponse? = response.body()

                val photoResponse: PhotoResponse? = flickrResponse?.photos

                //Fliters out gallery items with blank URL values using filter Not
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems ?:
                        mutableListOf()
                    galleryItems = galleryItems.filterNot {
                        it.url.isBlank()
                    }
                responseLiveData.value = galleryItems
            }
        })

        return responseLiveData //Returning the Live Data

    }





}






















