package slap.ibrahim.ansari.slap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class Landing extends Activity {
	
	//Constants
	private static final int NUM_SAMPLES = 15;
    private ParseUser user = new ParseUser();

    //State
	private int[] latest_data;
    ArrayList<Integer> list = new ArrayList<Integer>();
    double average = 0;
    ArrayList<Integer> x = new ArrayList<Integer>(), y = new ArrayList<Integer>(), z = new ArrayList<Integer>();
	
	//Layout members
	private TextView 
		xView,
		yView,
		zView,
        dView;
	private Button startButton;
    private int width;
	
	//Other members
	private PebbleDataReceiver receiver;
	private UUID uuid = UUID.fromString("05dd04b7-09e2-49d9-9433-08b7547a68a0");
	private Handler handler = new Handler();
    private HashMap<String, Object> params = new HashMap<String, Object>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_landing);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "AYNhSHLFJspugnAcX1ClVwVbkKx1uW3CJRqtu9qw", "vBzikVLaJNzxWVJWpVgBnmyRe2ER0erfdBXjDMFg");

//        user.setUsername("Ibrahim");
//        user.setPassword("test");
//        user.setEmail("ibrahim@ibrahim.io");

//        user.signUpInBackground(new SignUpCallback() {
//            public void done(ParseException e) {
//                if (e == null) {
//                    // Hooray! Let them use the app now.
//                } else {
//                    // Sign up didn't succeed. Look at the ParseException
//                    // to figure out what went wrong
//                }
//            }
//        });

//        ParseUser.logInInBackground("Ibrahim", "test", new LogInCallback() {
//            public void done(ParseUser user, ParseException e) {
//                if (user != null) {
//                    // Hooray! The user is logged in.
//                } else {
//                    // Signup failed. Look at the ParseException to see what happened.
//                }
//            }
//        });

        xView = (TextView)findViewById(R.id.x_view);
		yView = (TextView)findViewById(R.id.y_view);
		zView = (TextView)findViewById(R.id.z_view);
        dView = (TextView)findViewById(R.id.data_view);
		startButton = (Button)findViewById(R.id.start_button);
		
		startButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				PebbleDictionary dict = new PebbleDictionary();
				dict.addInt32(0, 0);
				PebbleKit.sendDataToPebble(getApplicationContext(), uuid, dict);
			}
			
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		receiver = new PebbleDataReceiver(uuid) {
			
			@Override
			public void receiveData(Context context, int transactionId, PebbleDictionary data) {
				PebbleKit.sendAckToPebble(getApplicationContext(), transactionId);
				
				//Get data
				latest_data = new int[3 * NUM_SAMPLES];
				Log.d("BIG BOOTY HOES GOT SHIT FROM THE PEBBLE", "NEW DATA PACKET");
				for(int i = 0; i < NUM_SAMPLES; i++) {
					for(int j = 0; j < 3; j++) {
						try {
							latest_data[(3 * i) + j] = data.getInteger((3 * i) + j).intValue();
						} catch(Exception e) {
							latest_data[(3 * i) + j] = -1;
						}
					}
					Log.d("OINDRIL IS A GOING TO DRILL THEM BOOTY HOES", "Sample " + i + " data: X: " + latest_data[(3 * i)] + ", Y: " + latest_data[(3 * i) + 1] + ", Z: " + latest_data[(3 * i) + 2]);
				}
				
				//Show
				handler.post(new Runnable() {
					@Override
					public void run() {
                        if(x.size() > 15) {
                            x.remove(0);
                            y.remove(0);
                            z.remove(0);
                        }
                        x.add(latest_data[0]);
                        y.add(latest_data[1]);
                        z.add(latest_data[2]);

                        if(x.size() >= 10) {
                            double stdX = stdDev(x), stdY = stdDev(y), stdZ = stdDev(z);
                            double mX = mean(x), mY = mean(y), mZ = mean(z);

                            width = 0;

                            if (latest_data[2] > mZ + 1.5 * stdZ) {
                                width++;
                                Log.d("EYYYY IT WORKED!!!", "EYYY Itwerked!");
                            }
                            else width = 0;
//                            else if (spike(latest_data[0], mX, stdX))
//                                //fistbump
//                            else if (spike(latest_data[1], mY, stdY))
//                                //handshake
                        }

                        if (width == 1) {
//                            ParseGeoPoint point = new ParseGeoPoint(40.0, -30.0);//for first user/device`
//                            user.put("location", point);
//                            params.put("timestamp", 120120909);
//                            params.put("location", point);
//                            ParseCloud.callFunctionInBackground("bumped", params, new FunctionCallback<Boolean>() {
//                                public void done(Boolean foundAndDone, ParseException e) {
//                                    if (e == null) {
//                                        if (foundAndDone) {
//                                            Log.d("YESSSSS", "YESSSSSSSSS");
//                                        } else {
//                                            //show toast about failed bump
//                                        }
//                                    }
//                                }
//                            });
                        }

                        xView.setText("X: " + latest_data[0]);
                        yView.setText("Y: " + latest_data[1]);
						zView.setText("Z: " + latest_data[2]);
                        dView.setText("Data: " + width);
					}
				});
			}
		};
		
		PebbleKit.registerReceivedDataHandler(this, receiver);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		unregisterReceiver(receiver);
	}

    private double stdDev(ArrayList<Integer> array)
    {
        double m = 0.0;
        double num = 0.0;
        m = mean(array);

        for(Integer a : array)
        {
            num += (a.doubleValue() - m)*(a.doubleValue() - m);
        }
        return Math.sqrt(num/(double)array.size());
    }

    private double mean(ArrayList<Integer> itr) {
        double avg = 0.0;
        for(Integer a : itr) {
            avg += a;
        }
        return avg / itr.size();
    }

    private boolean spike(Integer a, double mean, double std)
    {
        return (a.doubleValue() > mean + 2.5 * std || a.doubleValue() < mean - 2.5 * std);
    }
}