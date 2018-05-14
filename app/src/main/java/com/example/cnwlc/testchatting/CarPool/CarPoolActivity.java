package com.example.cnwlc.testchatting.CarPool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cnwlc.testchatting.Chatting.ChattingActivity;
import com.example.cnwlc.testchatting.ChattingRoom.ChattingRoomActivity;
import com.example.cnwlc.testchatting.Main.FriendsListActivity;
import com.example.cnwlc.testchatting.More.MoreActivity;
import com.example.cnwlc.testchatting.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.example.cnwlc.testchatting.Main.FriendsListActivity.clientThread_list;

public class CarPoolActivity extends Activity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
// OnMapReadyCallback 인터페이스의 onMapReady 메소드를 구현해줘야 합니다.  
// 맵이 사용할 준비가 되었을 때(NULL이 아닌 GoogleMap 객체를 파라미터로 제공해 줄 수 있을 때)  호출되어지는 메소드입니다.  

// MarkerOptions으로 마커가 표시될 위치(position), 마커에 표시될 타이틀(title), 마커 클릭시 보여주는 간단한 설명(snippet)를 설정하고
// addMarker 메소드로 GoogleMap 객체에 추가해주면 지도에 표시됩니다.

    private GoogleApiClient mGoogleApiClient = null;
    private static GoogleMap mGoogleMap = null;
    private static Marker currentMarker = null;

    // 디폴트 위치, Seoul
    // LatLng(double latitude, double longitude) ( Lat : 위도(Latitude), Lng : 경도(Longitude) )
    // Constructs a LatLng with the given latitude and longitude, measured in degrees.
    private static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int UPDATE_INTERVAL_MS = 60000;  // 60초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 60000; // 60초

    private Activity mActivity;
    boolean askPermissionOnceAgain = false;

    String sDeparture, sDestinate;
    List<Address> addr = null;
    ArrayList<Address> arrayListAdd = new ArrayList<Address>();
    ArrayList<Address> arrayListDep = new ArrayList<Address>();
    ArrayList<Address> arrayListDes = new ArrayList<Address>();

    SharedPreferences pref;
    SharedPreferences.Editor edit;
    String Sid, Scp, Simgpath, data = null;

    Button regBtn, delBtn;

    private static final String TAG_JSON = "chu";
    private static final String TAG_CNT = "cnt";
    private static final String TAG_CP = "cp";
    private static final String TAG_IMG = "img";
    private static final String TAG_DEPARTURE = "departure";
    private static final String TAG_DESTINATION = "destination";
    int noOfmember;
    String mJsonString, cp, img, departure, destination;
    String[] StrBaCp, StrBaImg, StrBaDep, StrBaDes;

    double currentLat, currentLon, departureLat, destinationLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // FLAG_KEEP_SCREEN_ON, FLAG_TURN_SCREEN_ON : 화면 전원키기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mActivity = this;
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        // mapFragment.getMapAsync(OnMapReadyCallback onMapReadyCallback)
        // GoogleMap 객체를 얻어오기 위한 메서드. getMapAsync() 를 하고 아래의 OnMapReady() 메소드에서 처리를 GoogleMap 객체관련 처리를 합니다.
        mapFragment.getMapAsync(this);

        // 아이디 -> 이름 가져오기
        pref = getSharedPreferences("login_information", MODE_PRIVATE);
        edit = pref.edit();
        Sid = pref.getString("id", "no id");
        Scp = pref.getString("Scp", "no cp");
        Simgpath = pref.getString("imgpath", "no cp");
        Log.e(TAG, " id = " + Sid);

        regBtn = (Button) findViewById(R.id.registBtn);
        delBtn = (Button) findViewById(R.id.deleteBtn);
    }

    // mGoogleApiClient 접속 및 퍼미션 체크, 알림창 띄우기
    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        Log.e("mGoogleApiClient : ", "" + mGoogleApiClient);

        // api 접속
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();

        // 앱 정보에서 퍼미션을 허가했는지를 다시 검사해봐야 한다.
        if (askPermissionOnceAgain) {
            // VERSION_CODES.M : 마시멜로 버전
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                askPermissionOnceAgain = false;
                checkPermissions();
            }
        }
    }

    // 운전자 또는 동승자 등록 여부 알림창 띄우기
    public void driverORpass(String sdata) {
        if (sdata.equals("1")) {
            /** 안내 알림창 띄우기 -> 커스텀 알림창으로 변경해서 "체크박스(다시보지않음)+텍스트 띄우기" */
            AlertDialog.Builder builder = new AlertDialog.Builder(CarPoolActivity.this);
            builder.setTitle("카풀 사용 안내");
            builder.setMessage("카풀 서비스 사용 방법 \n\n" +
                    "1. 운전자 또는 동승자로서 \"등록\"버튼을 눌러 등록한다.\n" +
                    "2. 운전자로 등록 시 자신의 현 위치에 반경 2km내에 동승자가 뜸\n" +
                    "3. 동승자의 경우 출발지와 목적지를 등록 후 대기\n" +
                    "4. 등록 후 수정을 원할 경우 \"더보기\"에서 수정 가능 ");
            builder.setCancelable(false);
            builder.setPositiveButton("등록", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CarPoolActivity.this);
                    builder.setMessage("운전자/동승자 등록");
                    builder.setCancelable(false);
                    builder.setPositiveButton("운전자", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent registIntent = new Intent(CarPoolActivity.this, CarPoolDriverActivity.class);
                            startActivity(registIntent);
                        }
                    });
                    builder.setNegativeButton("동승자", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            regBtn.setVisibility(View.VISIBLE);
                            delBtn.setVisibility(View.VISIBLE);

                            sDeparture = "";
                            sDestinate = "";
                            InsertData insertData = new InsertData();
                            insertData.execute(Sid, Scp, Simgpath, sDeparture, sDestinate);
                        }
                    });
                    builder.show();
                }
            });
            builder.show();
        }
    }

    // mGoogleMap 정의, Ui, 나침반, 지도이동, 줌, 퍼미션체크, buildGoogleApiClient() 함수 호출
    @Override
    public void onMapReady(GoogleMap map) {
        Log.d(TAG, "onMapReady");
        mGoogleMap = map;
        // 런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에 지도의 초기위치를 서울로 이동
        setCurrentLocation(null, "위치정보 가져올 수 없음", "위치 퍼미션과 GPS 활성 요부 확인하세요");

        // getUiSettings : UiSettings 객체 획득, setCompassEnabled : 나침판 보여줄지 여부
        mGoogleMap.getUiSettings().setCompassEnabled(true);
        // mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        // animateCamera : 지도 이동
        // CameraUpdateFactory.zoomTo(float) : 다른 모든 속성은 동일하게 유지하면서 확대/축소 수준을 주어진 값으로 변경하는 CameraUpdate를 제공
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        // 마시멜로 버전 이상일 경우 허가 요청, 이하일 경우 진행
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // API 23 이상이면 런타임 퍼미션 처리 필요
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

            if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED) {
                // ActivityCompat : 액티비티 호환성
                ActivityCompat.requestPermissions(mActivity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } else {
                if (mGoogleApiClient == null) {
                    buildGoogleApiClient();
                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // setMyLocationEnabled : My Location 계층 활성화. Location 계층이 활성화되면 My Location 버튼이 지도 오른쪽 위 모서리에 나타납니다.
                    // 사용자가 버튼을 클릭했을 때 현재 위치를 알 경우, 카메라가 기기의 현재 위치를 지도의 중앙에 표시합니다.
                    // 기기가 정지해 있을 때는 위치가 지도 위에 작은 파란 점으로 나타나고, 이동 중일 때는 V자 기호로 나타납니다.
                    mGoogleMap.setMyLocationEnabled(true);
                }
            }
        } else {
            if (mGoogleApiClient == null) {
                buildGoogleApiClient();
            }
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    // 현재 위치를 <주소, 위도/경도> 로 표시
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        String markerTitle = getCurrentAddress(location);
        String markerSnippet = "위도:" + String.valueOf(location.getLatitude()) + " 경도:" + String.valueOf(location.getLongitude());
        //현재 위치에 마커 생성
        setCurrentLocation(location, markerTitle, markerSnippet);
    }

    /**
     * 현재 위치 가져오기
     */
    public String getCurrentAddress(Location location) {
        // 지오코더 : GPS를 주소로 변환 / 역지오코딩 : 주소 -> 위도, 경도 / 지오코딩 : 위도 경도 -> 주소
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            // getFromLocation : 주소를 얻어오는 메소드, 인자로 위도와 경도, 그리고 결과를 몇 개 받을 것인지를 넘겨주면 됨.
            // '좌표(위도, 경도)' 를 '주소나 지명' 으로 변환하는 경우
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }

    // mGoogleApiClient 정의 -> 연결
    protected synchronized void buildGoogleApiClient() {
        // GoogleApiClient.Builder(this) : API에 연결하려면 Google Play 서비스 API 클라이언트의 인스턴스를 생성해야 합니다.
        // GoogleApiClient.Builder를 사용하여 Google API 클라이언트의 인스턴스를 생성합니다.

        // addConnectionCallbacks(this).addOnConnectionFailedListener(this) : GoogleApiClient에 대한 수동 관리 연결을 시작하려면 콜백 인터페이스,
        // ConnectionCallbacks 및 OnConnectionFailedListener에 대한 구현을 지정해야합니다.
        // 이러한 인터페이스는 Google Play 서비스에 대한 연결이 성공하거나 실패하거나 일시 중지 된 경우 비동기 connect () 메소드에 대한 응답으로 콜백을받습니다.

        // LocationServices.API : Fused Location Provider API를 사용하기 위해 addApi() 함수에 설정
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    // 접속 후 LocationRequest로 위치 업데이트 및 정확성 정의, 퍼미션 및 지도이동
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected");
        // 위치서비스가 꺼져있을 경우 if문 안의 method로 이동
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL_MS);        // setInteval : 위치가 update되는 주기
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);     // setFastestInterval : 위치 획득 후 update되는 주기
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Priority는 4가지의 설정값이 있다.
        // PRIORITY_HIGH_ACCURACY : 배터리소모를 고려하지 않으며 정확도를 최우선으로 고려
        // PRIORITY_LOW_POWER : 저전력을 고려하며 정확도가 떨어짐
        // PRIORITY_NO_POWER : 추가적인 배터리 소모없이 위치정보 획득
        // PRIORITY_BALANCED_POWER_ACCURACY : 전력과 정확도의 밸런스를 고려. 정확도 다소 높음

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onConnected success : call FusedLocationApi");
                // FusedLocationApi : 저전력으로 위치측위의 정확도를 향상시켰고, 기존보다 간편하게 API 호출하여 위치를 측위할 수 있도록 개선
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            }
        } else {
            Log.d(TAG, "onConnected fail : call FusedLocationApi");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            mGoogleMap.getUiSettings().setCompassEnabled(true);

            //mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
    }

    // 접속 실패
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Location location = null;
        location.setLatitude(DEFAULT_LOCATION.latitude);
        location.setLongitude(DEFAULT_LOCATION.longitude);
        setCurrentLocation(location, "위치정보 가져올 수 없음", "위치 퍼미션과 GPS 활성 요부 확인하세요");
    }

    @Override
    // 연결 일시 중지
    public void onConnectionSuspended(int cause) {
        if (cause == CAUSE_NETWORK_LOST)
            Log.e(TAG, "onConnectionSuspended(): Google Play services " + "connection lost.  Cause: network lost.");
        else if (cause == CAUSE_SERVICE_DISCONNECTED)
            Log.e(TAG, "onConnectionSuspended():  Google Play services " + "connection lost.  Cause: service disconnected");
    }

    // LocationManager 을 통해 모바일의 GPS or NetWork 정보로 위치 좌표를 받아옴
    public boolean checkLocationServicesStatus() {
        // LocationManager : 모바일의 Gps 또는 Network 정보로 위치좌표를 받아오는 방법
        // 위치 제공자는 총 2가지 종류가 있다. 1. GPS_PROVIDER / 2. NETWORK_PROVIDER
        // 실내에서는 GPS_PROVIDER를 호출해도 응답이 없다. -> 응답을 기다리는 형태로 코딩을 했다면 별다른 처리를 하지 않으면 실내에서는 무한정 대기한다.
        // 따라서 타이머를 설정하여 GPS_PROVIDER를 호출 한 뒤 일정 시간이 지나도 응답이 없을 경우
        // NETWORK_PROVIDER를 호출 하거나, 또는 둘 다 한꺼번에 호출하여 들어오는 값을 사용하는 방식으로 코딩을 하는것이 일반적이겠다.
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // 현재 위치를 위도와 경도로 받아와 마커로 표시, 현재 위치를 못받아 올 경우 지정된 서울 위치로 이동
    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        if (currentMarker != null)
            currentMarker.remove();

        if (location != null) {
            // 위도 경도 위치를 LatLng 객체를 통해 설정
            currentLat = location.getLatitude();
            currentLon = location.getLongitude();

            LatLng currentLocation = new LatLng(currentLat, currentLon);

            // 구글 맵에 표시할 마커에 대한 옵션 설정
            MarkerOptions markerOptions = new MarkerOptions();  // 마커를 원하는 이미지로 변경해줘야함
            markerOptions.position(currentLocation);    // 지도에서 마커의 위치에 대한 LatLng 값. 이는 Marker 객체의 유일한 필수 속성.
            markerOptions.title(markerTitle);           // 사용자가 마커를 눌렀을때 정보 창에 표시되는 문자열
            markerOptions.snippet(markerSnippet);       // snippet : 마커 아래에 표시되는 추가 텍스트
            markerOptions.draggable(true);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));    // 기본 마커 이미지 대신 표시되는 비트맵

            currentMarker = mGoogleMap.addMarker(markerOptions);
            // 현재 위치로 카메라 이동
//            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

            // 반경 1KM원
            CircleOptions circle = new CircleOptions().center(currentLocation) //원점
                    .radius(4000)      //반지름 단위 : m
                    .strokeWidth(10)  //선너비 0f : 선없음
                    .fillColor(Color.parseColor("#00000000")); //배경색
            //원추가
            this.mGoogleMap.addCircle(circle);

            OutData outData = new OutData();
            outData.execute(Sid);

            return;
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        currentMarker = mGoogleMap.addMarker(markerOptions);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION));
    }

    // 여기부터는 런타임 퍼미션 처리을 위한 메소드들
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        boolean fineLocationRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED && fineLocationRationale)
            showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
        else if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED && !fineLocationRationale) {
            showDialogForPermissionSetting("퍼미션 거부 + Don't ask again(다시 묻지 않음) " + "체크 박스를 설정한 경우로 설정에서 퍼미션 허가해야합니다.");
        } else if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {
            if (mGoogleApiClient == null) {
                buildGoogleApiClient();
            }
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permsRequestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults.length > 0) {
            boolean permissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (permissionAccepted) {
                if (mGoogleApiClient == null) {
                    buildGoogleApiClient();
                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mGoogleMap.setMyLocationEnabled(true);
                }
            } else {
                checkPermissions();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CarPoolActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ActivityCompat.requestPermissions(mActivity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    private void showDialogForPermissionSetting(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CarPoolActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                askPermissionOnceAgain = true;
                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + mActivity.getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(myAppSettings);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    // 여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CarPoolActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("위치 서비스가 꺼져있습니다.\n" + "위치 서비스를 키시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                mGoogleMap.setMyLocationEnabled(true);
                            }
                        } else mGoogleMap.setMyLocationEnabled(true);
                        return;
                    }
                } else {
                    setCurrentLocation(null, "위치정보 가져올 수 없음", "위치 퍼미션과 GPS 활성 요부 확인하세요");
                }
                break;
        }
    }

    // 동승자 출발지, 도착지 마커 찍기
    public Location findGeoPointDeparture(Context mcontext, String address) {
        Location loc = new Location("");
        Geocoder coder = new Geocoder(mcontext);
//        List<Address> addr = null;// 한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 설정

        try {
            // getFromLocationName : '주소나 지명' 을 '좌표(위도, 경도)' 으로 변환하는 경우
            addr = coder.getFromLocationName(address, 5);
        } catch (IOException e) {
            e.printStackTrace();
        }// 몇개 까지의 주소를 원하는지 지정 1~5개 정도가 적당

        if (addr != null) {
            Address lating = addr.get(0);
            arrayListDep.add(lating);

            double lat = lating.getLatitude(); // 위도가져오기
            double lon = lating.getLongitude(); // 경도가져오기
            loc.setLatitude(lat);
            loc.setLongitude(lon);

            LatLng PassLocation = new LatLng(lat, lon);
            // 구글 맵에 표시할 마커에 대한 옵션 설정
            MarkerOptions markerOptions = new MarkerOptions();  // 마커를 원하는 이미지로 변경해줘야함
            markerOptions.position(PassLocation);    // 지도에서 마커의 위치에 대한 LatLng 값. 이는 Marker 객체의 유일한 필수 속성.
            markerOptions.title(address + "(출발지)");           // 사용자가 마커를 눌렀을때 정보 창에 표시되는 문자열
            markerOptions.snippet("위도 : " + lat + " / 경도 : " + lon);       // snippet : 마커 아래에 표시되는 추가 텍스트
            markerOptions.draggable(true);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));    // 기본 마커 이미지 대신 표시되는 비트맵

            mGoogleMap.addMarker(markerOptions);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(PassLocation));
            mGoogleMap.setOnMarkerClickListener(this);
        }
        return loc;
    }
    public Location findGeoPointDestination(Context mcontext, String address) {
        Location loc = new Location("");
        Geocoder coder = new Geocoder(mcontext);
//        List<Address> addr = null;// 한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 설정

        try {
            // getFromLocationName : '주소나 지명' 을 '좌표(위도, 경도)' 으로 변환하는 경우
            addr = coder.getFromLocationName(address, 5);
        } catch (IOException e) {
            e.printStackTrace();
        }// 몇개 까지의 주소를 원하는지 지정 1~5개 정도가 적당

        if (addr != null) {
            Address lating = addr.get(0);
            arrayListDes.add(lating);

            double lat = lating.getLatitude(); // 위도가져오기
            double lon = lating.getLongitude(); // 경도가져오기
            loc.setLatitude(lat);
            loc.setLongitude(lon);

            LatLng DriverLocation = new LatLng(lat, lon);
            // 구글 맵에 표시할 마커에 대한 옵션 설정
            MarkerOptions markerOptions = new MarkerOptions();  // 마커를 원하는 이미지로 변경해줘야함
            markerOptions.position(DriverLocation);    // 지도에서 마커의 위치에 대한 LatLng 값. 이는 Marker 객체의 유일한 필수 속성.
            markerOptions.title(address + "(도착지)");           // 사용자가 마커를 눌렀을때 정보 창에 표시되는 문자열
            markerOptions.snippet("위도 : " + lat + " / 경도 : " + lon);       // snippet : 마커 아래에 표시되는 추가 텍스트
            markerOptions.draggable(true);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));    // 기본 마커 이미지 대신 표시되는 비트맵
//            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(R.drawable.inbox2));

            mGoogleMap.addMarker(markerOptions);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(DriverLocation));
            mGoogleMap.setOnMarkerClickListener(this);
        }
        return loc;
    }

    // 운전자 출발지 마커 찍기
    public Location findGeoPointDepartureDriver(Context mcontext, String sDeparture, String sDestination) {
        Location loc = new Location("");
        Geocoder coder = new Geocoder(mcontext);
        System.out.println("findGeoPointDepartureDriver sDeparture : "+sDeparture);
        System.out.println("findGeoPointDepartureDriver sDestination : "+sDestination);
//        List<Address> addr = null;// 한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 설정

        try {
            // getFromLocationName : '주소나 지명' 을 '좌표(위도, 경도)' 으로 변환하는 경우
            addr = coder.getFromLocationName(sDeparture, 5);
            System.out.println("findGeoPointDepartureDriver addr : "+addr);
        } catch (IOException e) {
            e.printStackTrace();
        }// 몇개 까지의 주소를 원하는지 지정 1~5개 정도가 적당

        if (addr != null) {
            Address lating = addr.get(0);
            arrayListDep.add(lating);

            departureLat = lating.getLatitude(); // 위도가져오기
            destinationLon = lating.getLongitude(); // 경도가져오기
            loc.setLatitude(departureLat);
            loc.setLongitude(destinationLon);
            System.out.println("findGeoPointDepartureDriver departureLat : "+departureLat);

            double distance_A = DistanceByDegree(currentLat, currentLon, departureLat, destinationLon);
            double distance_B = DistanceByDegreeAndroid(currentLat, currentLon, departureLat, destinationLon);
            System.out.println("findGeoPointDepartureDriver distance_A : "+distance_A);

            LatLng PassLocation = new LatLng(departureLat, destinationLon);
            // 구글 맵에 표시할 마커에 대한 옵션 설정
            if (distance_A <= 4000 || distance_B <= 4000) {
                System.out.println("findGeoPointDepartureDriver PassLocation : "+PassLocation);

                MarkerOptions markerOptions = new MarkerOptions();  // 마커를 원하는 이미지로 변경해줘야함
                markerOptions.position(PassLocation);    // 지도에서 마커의 위치에 대한 LatLng 값. 이는 Marker 객체의 유일한 필수 속성.
                markerOptions.title(sDeparture);           // 사용자가 마커를 눌렀을때 정보 창에 표시되는 문자열
                markerOptions.snippet(sDestination);       // snippet : 마커 아래에 표시되는 추가 텍스트
                markerOptions.draggable(true);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));    // 기본 마커 이미지 대신 표시되는 비트맵

                mGoogleMap.addMarker(markerOptions);
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(PassLocation));
                mGoogleMap.setOnMarkerClickListener(this);
            }
        }
        return loc;
    }

    // 마커 클릭리스너
    @Override
    public boolean onMarkerClick(final Marker marker) {
        System.out.println("onMarkerClick mJsonString : "+mJsonString);
        if(mJsonString != null) {
            LayoutInflater inflater = getLayoutInflater();

            View dialogView = inflater.inflate(R.layout.custom_dialog_marker, null);

            ImageView imgV = (ImageView) dialogView.findViewById(R.id.imgView);
            TextView tvDepa2 = (TextView) dialogView.findViewById(R.id.departureTv2);
            TextView tvDest2 = (TextView) dialogView.findViewById(R.id.destinationTv2);

            tvDepa2.setText(marker.getTitle());
            tvDest2.setText(marker.getSnippet());

            for(int i=0; i<StrBaDes.length; i++) {
                if(marker.getTitle().equals(StrBaDep[i])) {
                    Glide.with(CarPoolActivity.this).load(StrBaImg[i]).into(imgV);

                    final int j=i;
                    System.out.println("onMarkerClick i : "+i);
                    System.out.println("onMarkerClick j : "+j);
                    System.out.println("onMarkerClick StrBaImg[i] : "+StrBaImg[i]);
                    System.out.println("onMarkerClick StrBaCp[i] : "+StrBaCp[i]);
                    System.out.println("onMarkerClick StrBaImg[i] : "+StrBaImg[i]);
                    System.out.println("onMarkerClick StrBaCp[j] : "+StrBaCp[j]);

                    clientThread_list.send("makeroom]" + Scp + "]" + StrBaCp[i] + "]" + Simgpath + "]" + StrBaImg[i] + "]" + null + "]with");

                    AlertDialog.Builder builder = new AlertDialog.Builder(this); //AlertDialog.Builder 객체 생성
                    builder.setTitle("카풀 요청"); //Dialog 제목
                    builder.setView(dialogView); //위에서 inflater가 만든 dialogView 객체 세팅 (Customize)
                    builder.setMessage("가는 길 심심하지 않으세요?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("채팅", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            clientThread_list.send("enterroom]" + Scp + "]" + StrBaCp[j] + "]" + null + "]with");

                            Intent chattingIntent = new Intent(getApplicationContext(), ChattingActivity.class);
                            chattingIntent.putExtra("push_cellphone_Me", Scp);
                            chattingIntent.putExtra("push_cellphone_Other", StrBaCp[j]);
                            chattingIntent.putExtra("push_path", StrBaImg[j]);
                            startActivity(chattingIntent);
                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                    builder.create().show();
                }
            }
        }
        return false;
    }

    // 버튼설정
    public void onClick(View v) {
        switch (v.getId()) {
            /** 운전자로 등록 시 아래 버튼 2개 뜨지 않도록 설정 */
            // 위 버튼
            case R.id.registBtn:
                final LayoutInflater inflater = getLayoutInflater();

                final View dialogView = inflater.inflate(R.layout.custom_dialog_passenger, null);
                final EditText editDepar = (EditText) dialogView.findViewById(R.id.depatureEtv);
                final EditText editDesti = (EditText) dialogView.findViewById(R.id.destinateEtv);

                AlertDialog.Builder builder = new AlertDialog.Builder(this); //AlertDialog.Builder 객체 생성
                builder.setTitle("등록"); //Dialog 제목
                builder.setView(dialogView); //위에서 inflater가 만든 dialogView 객체 세팅 (Customize)
                builder.setMessage("출발지와 목적지를 입력하세요.");
                builder.setCancelable(false);
                builder.setPositiveButton("등록", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        sDeparture = editDepar.getText().toString();
                        sDestinate = editDesti.getText().toString();

                        findGeoPointDeparture(CarPoolActivity.this, sDeparture);
                        findGeoPointDestination(CarPoolActivity.this, sDestinate);

                        InsertData insertData = new InsertData();
                        insertData.execute(Sid, Scp, Simgpath, sDeparture, sDestinate);
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getApplicationContext(), "취소하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.create().show();
                break;
            case R.id.deleteBtn:
                DeleteData deleteData = new DeleteData();
                deleteData.execute(Sid);
                break;

            // 맨 밑 버튼
            case R.id.friendlistBtn:
                Intent friendit = new Intent(this, FriendsListActivity.class);
                startActivity(friendit);
                break;
            case R.id.chattingRoomlistBtn:
                Intent chatroomit = new Intent(this, ChattingRoomActivity.class);
                startActivity(chatroomit);
                break;
            case R.id.carpoolBtn:
                Intent carpoolit = new Intent(this, CarPoolActivity.class);
                startActivity(carpoolit);
                break;
            case R.id.moreBtn:
                Intent friendIt = new Intent(this, MoreActivity.class);
                startActivity(friendIt);
                break;
        }
    }

    // 동승자 위치 또는 운전자 데이터 가져오기
    class OutData extends AsyncTask<String, Void, String> {
        String errorString = null;
        String id;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            id = params[0];
            String serverURL = "http://115.71.238.109/carpool_out.php";
            String postParameters = "id=" + id;

            try {
                URL url = new URL(serverURL);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();
                data = sb.toString().trim();
                Log.e("OutData data : ", data);

                return data;
            } catch (Exception e) {
                Log.d(TAG, "out : Error ", e);
                errorString = e.toString();

                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("CarpoolActivity result : ", result);

            if (data.equals("%")) {
                Log.e("CarpoolActivity data,equals(%) : ", data);

                regBtn.setVisibility(View.VISIBLE);
                delBtn.setVisibility(View.VISIBLE);
            } else if (data.contains("%") && data.length() > 2) {
                Log.e("CarpoolActivity data contain(%) : ", data);
                String[] de = data.split("%");

                findGeoPointDeparture(CarPoolActivity.this, de[0]);
                findGeoPointDestination(CarPoolActivity.this, de[1]);

                regBtn.setVisibility(View.VISIBLE);
                delBtn.setVisibility(View.VISIBLE);

                regBtn.setEnabled(false);
            } else if (data.startsWith("driver#")) {
                Log.e("CarpoolActivity data.startsWith(\"driver#\") : ", data);

                String[] de = data.split("#");
                mJsonString = de[1];
                Log.e("CarpoolActivity mJsonString : ", mJsonString);
                showResult();
            } else {
                Log.e("CarpoolActivity data : ", data);

                driverORpass(data);
            }
        }
    }
    private void showResult() {
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            JSONObject jsonNo = jsonArray.getJSONObject(0);
            String Sno = jsonNo.getString(TAG_CNT);
            noOfmember = Integer.parseInt(Sno);

            // 입력받은 값(id, name, cp, no)들을 배열로 받기
            StrBaCp = new String[noOfmember];
            StrBaImg = new String[noOfmember];
            StrBaDep = new String[noOfmember];
            StrBaDes = new String[noOfmember];

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);

                cp = item.getString(TAG_CP);
                img = item.getString(TAG_IMG);
                departure = item.getString(TAG_DEPARTURE);
                destination = item.getString(TAG_DESTINATION);

                StrBaCp[i] = cp;
                StrBaImg[i] = img;
                StrBaDep[i] = departure;
                StrBaDes[i] = destination;
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                System.out.println("CarPoolActivity StrBaCp["+i+"] : "+StrBaCp[i]);
                System.out.println("CarPoolActivity StrBaDep["+i+"] : "+StrBaDep[i]);
                findGeoPointDepartureDriver(CarPoolActivity.this, StrBaDep[i], StrBaDes[i]);
            }
        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }

    // 동승자 위치 삭제하기
    class DeleteData extends AsyncTask<String, Void, String> {
        String errorString = null;
        String id;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            id = params[0];
            String serverURL = "http://115.71.238.109/carpool_pass_delete.php";
            String postParameters = "id=" + id;

            try {
                URL url = new URL(serverURL);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();
                data = sb.toString().trim();
                Log.e("DeleteData data : ", data);

                return data;
            } catch (Exception e) {
                Log.d(TAG, "out : Error ", e);
                errorString = e.toString();

                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG, " DeleteData response - " + result);

            if (data.equals("0")) {
                Log.e("CarpoolActivity Delete data : ", data);

                AlertDialog.Builder builder = new AlertDialog.Builder(CarPoolActivity.this);
                builder.setTitle("알림");
                builder.setMessage("삭제가 완료되었습니다. ");
                builder.setCancelable(true);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();

                regBtn.setEnabled(true);
            } else {
                Log.e("CarpoolActivity data : ", data);
            }
        }
    }

    // db에 동승자 데이터(아이디, 출발지, 목적지) 넣기
    class InsertData extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String id = params[0];
            String cp = params[1];
            String img = params[2];
            String depart = params[3];
            String destin = params[4];

            String serverURL = "http://115.71.238.109/carpool_pass_in.php";
            String postParameters = "id=" + id + "&cp=" + cp + "&img=" + img + "&departure=" + depart + "&destination=" + destin;

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                //httpURLConnection.setRequestProperty("content-type", "application/json");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));   // 출력 스트림에 출력
                outputStream.flush();   // 출력 스트림을 플러시(비운다)하고 버퍼링 된 모든 출력 바이트를 강제 실행
                outputStream.close();   // 출력 스트림을 닫고 모든 시스템 자원을 해제

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

                // [2-3]. 연결 요청 확인.
                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                    System.out.println("HTTP_OK 값 : " + HttpURLConnection.HTTP_OK);
                    Log.d(TAG, "inputStream : " + inputStream);
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                    Log.d(TAG, "inputStream : " + inputStream);
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                // 읽어온 결과물 리턴. 요청한 URL의 출력물을 BufferedReader로 받는다.
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();
                data = sb.toString().trim();
                Log.e("RECV DATA", data);
            } catch (Exception e) {
                Log.d(TAG, "InsertData: Error ", e);
            }
            return null;
        }

        // 백그라운드 작업이 완료된 후 결과값을 얻습니다.
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG, "POST response  - " + result);

            /* 서버에서 응답 */
            Log.e(TAG, "insert data = " + data);
            if (data.equals("0")) {
                Log.w("RESULT", "성공적으로 출발지, 목적지 등록에 성공했습니다!");

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CarPoolActivity.this);
                builder.setTitle("알림");
                builder.setMessage("출발지, 목적지 등록이 완료되었습니다..");
                builder.setCancelable(true);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
                regBtn.setEnabled(false);
            } else if (data.equals("1")) {
                Log.w("RESULT", "성공적으로 아이디 등록에 성공했습니다!");

                Toast.makeText(getApplicationContext(), "동승자를 선택하셨습니다.\n목적지와 출발지를 등록 가능합니다.", Toast.LENGTH_SHORT).show();
            } else {
                Log.w("RESULT", "에러 발생! ERRCODE = " + data);

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CarPoolActivity.this);
                builder.setTitle("알림");
                builder.setMessage("등록중 에러가 발생했습니다! errcode : " + data);
                builder.setCancelable(true);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        }
    }

    // 출발지와 운전자 현재 위치 계산하기
    // 두지점(위도,경도) 사이의 거리
    public double DistanceByDegree(double _latitude1, double _longitude1, double _latitude2, double _longitude2) {
        double theta, dist;
        theta = _longitude1 - _longitude2;
        dist = Math.sin(DegreeToRadian(_latitude1)) * Math.sin(DegreeToRadian(_latitude2)) + Math.cos(DegreeToRadian(_latitude1))
                * Math.cos(DegreeToRadian(_latitude2)) * Math.cos(DegreeToRadian(theta));
        dist = Math.acos(dist);
        dist = RadianToDegree(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;    // 단위 mile 에서 km 변환.
        dist = dist * 1000.0;      // 단위  km 에서 m 로 변환

        return dist;
    }
    // degree->radian 변환
    public double DegreeToRadian(double degree) {
        return degree * Math.PI / 180.0;
    }
    // randian -> degree 변환
    public double RadianToDegree(double radian) {
        return radian * 180d / Math.PI;
    }

    // 안드로이드 - 두지점(위도,경도) 사이의 거리
    public double DistanceByDegreeAndroid(double _latitude1, double _longitude1, double _latitude2, double _longitude2) {
        Location startPos = new Location("PointA");
        Location endPos = new Location("PointB");

        startPos.setLatitude(_latitude1);
        startPos.setLongitude(_longitude1);
        endPos.setLatitude(_latitude2);
        endPos.setLongitude(_longitude2);

        double distance = startPos.distanceTo(endPos);

        return distance;
    }
    /////////////////////////////////////

    /**
     * 운전자 등록 데이터 가져오고, 다른 동승자만 뜨게끔 구성(-> 범위 내에 있는 사람만 + Service or FCM으로 알려주기)
     */
    @Override
    public void onPause() {
        //위치 업데이트 중지
        Log.e(TAG, "onPause");

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.e(TAG, "onStop");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.unregisterConnectionCallbacks(this);
            mGoogleApiClient.unregisterConnectionFailedListener(this);

            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                mGoogleApiClient.disconnect();
            }
        }
        super.onDestroy();
    }
}


/**
 * http://webnautes.tistory.com/101
 */