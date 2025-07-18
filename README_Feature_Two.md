
# Battery Request Path Feature
## The Approach
The swapper has a form which they can fill to place a request, the requests will be saved in a local db
and synced immediately or when there is a connection. On the swapper's dashboard, they can see whether their
requests have been synced.

This aids in the event of urgency and there is no connection, the swapper should resort to make phone calls
and request the batteries.

When they are synced, they will appear on the dashboard of a central staff where they can act and update the requests accordingly.

The added updates can be synced to the requester's device so that they have a trail of the occurrences
for their request, they also have an option to confirm reception which automatically closes 
the request.

## Screenshots on App
<img src="/screenshots/Screenshot_20250718-154649_Swapper.jpg" width="220" height="470"/> &nbsp;&nbsp;&nbsp;<img src="/screenshots/Screenshot_20250718-171125_Swapper.jpg" width="220" height="470"/>

<img src="/screenshots/Screenshot_20250718-171108_Swapper.jpg" width="220" height="470"/>&nbsp;&nbsp;&nbsp; <img src="/screenshots/Screenshot_20250718-171117_Swapper.jpg" width="220" height="470"/> 
## Multi-country support on API
Since the country parameter only changes at login, the param is built into the baseUrl after it is stored
on the preferences, this reduces the effort on the development to pass around the parameter, this remains effective
and is updated whenever the user logs out/logs in.

```kotlin
    fun provideRetrofit(http: OkHttpClient, gson: Gson, countryCode: String): Retrofit {
        // Here the country code can be retrieved from preferences and injected into
        // this function then have it appended to baseUrl
        return Retrofit.Builder().baseUrl("$baseUrl/$countryCode/").addConverterFactory(
            GsonConverterFactory.create(
                gson
            )
        ).client(http).build()
    }
```

### Database Design
The following can be revised and adopted as the database for use in managing requests.
&nbsp;&nbsp;&nbsp;<img src="/screenshots/db_design.png" width="738" height="491"/>

### Architecture 
The app follows the SOLID principle, abstracts all the implementations and exposes interfaces.
Here is an overview of the implementation.
&nbsp;&nbsp;&nbsp;<img src="/screenshots/architecture.png" width="731" height="391"/>

### Recommendations for Prod
- Add pagination to allow sizing of data to be synced.
- Implement local DB schema export to facilitate migrations.