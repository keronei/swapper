# Swapper Shift Tracking Prototype

## The Approach
Swappers have accounts that are already registered and will be required to login into a device being used
at a particular station providing the following: Username/phone, password and country. Once they are logged in, 
they can now check-in to the station.

Additional hardware is introduced in order to aid in verifying presence at the station, the hardware
is a Bluetooth Low Energy device that advertises packets every 5 seconds or so, the packets contain
information about the station, which includes station ID, location, etc - this kind of device runs on
a coin cell and can operate for up to 360 days before the battery runs out. This component will 
provide 'station signal'.

These are two levels for accessing the station device;

### Initial Login
- Internet connectivity is required.
- User is authenticated from the server and their credentials cached on the device.
- This is only done once on a station device.
- Multiple users can login to the same device and their accounts retained.
- No need to be at the station to login.
- Cannot perform any station-specific operations on the app.

### Check-in / Check-out
- After a swapper has logged in, they will be required to check-in in order to complete any tasks
on the app.
- Check-in requires username and station in order to verify if already logged in.
- The next step is station presence verification which requires location, station signal and a selfie.
- Once the details are captured, the location is written onto the image and persisted in the local
storage, its location is kept on the database alongside the rest of the check-in info.
- Check-out only requires the station presence verification and will be stored in the same place
with check-ins.
- This step happens offline, when connectivity is available, the entries are synced to the server.

### Screenshots

<img src="/screenshots/Screenshot_20250717-063615_Swapper.jpg" width="220" height="470"/>&nbsp;&nbsp;&nbsp; <img src="/screenshots/Screenshot_20250717-063718_Swapper.jpg" width="220" height="470"/> &nbsp;&nbsp;&nbsp;<img src="/screenshots/Screenshot_20250717-093944_Swapper.jpg_Swapper.jpg" width="220" height="470"/>

<img src="/screenshots/Screenshot_20250717-093932_Swapper.jpg" width="220" height="470"/>
## Technical Details
At first login, the swapper inputs their country, this will be used to determine the correct API 
for the country. The following format will work.

```
    baseUrl/api/v1/{country-code}/login/
```

The payload will have the following format:

```json
{
  "username": "Bob",
  "password": "hashed-password",
  "device": "sm-9001_Android12"
}

```
The login device should be stored in the server for traceability purposes. 

On a successful login, the user ID, username, auth token and country will be stored locally.

Every check-in/check-out will be stored with a foreign key reference to the saved user, the check-in 
details will include: date-time, station, photo, GPS coordinate, nature(check-in/check-out).

When a connection is restored, a workmanager will be started to sync these details to the server, the
API could look like this:

```
    baseUrl/api/v1/{country-code}/sessions/
```

```
[
    {'userId': 1, 'nature': 'check-in', 'time': '2025-07-17T08:23:21', 'station': 'Mpigi', 'coord': {'lat':35.0, 'lon':1.4}},
    ...
]
```

The BLE device will require firmware to make the broadcasts at the desired time with the required information,
the info can be packaged into iBeacon frame which allows modification of particulars. Some proprietary
devices allows configuration from an app, which significantly reduces the effort required to set them up.

The photos could be uploaded to S3 bucket or GCP.

### What can be improved
- Since the check-in requires the station signal and the device providing the signal may be 
non-functional due to battery replacement schedule being missed, swappers may be given an option to
request a bypass code, this will aid them access the app without station signal.

### Extra Costs  
The introduced device costs about UGX 32k each, the firmware will need to be written to it before installation.

