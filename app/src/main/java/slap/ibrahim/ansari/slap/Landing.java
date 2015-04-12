package slap.ibrahim.ansari.slap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Landing extends Activity {
	
	//Constants
	private static final int NUM_SAMPLES = 15;
//    private ParseUser user = new ParseUser();

    //State
	private int[] latest_data;
    ArrayList<Integer> list = new ArrayList<Integer>();
    double average = 0;
    ArrayList<Integer> x = new ArrayList<Integer>(), y = new ArrayList<Integer>(), z = new ArrayList<Integer>();
	
	//Layout members
	private TextView title;
	private Button pebButton, myoButton;
    private int width;

    private double time = System.currentTimeMillis();
	//Other members
	private PebbleDataReceiver receiver;
	private UUID uuid = UUID.fromString("Insert Here");
	private Handler handler = new Handler();
    private HashMap<String, Object> params = new HashMap<String, Object>();
	boolean doIt = true;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_landing);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "Insert Your own", "Insert your own");

        if(doIt){
//            ParseUser user = new ParseUser();
//            user.setUsername("test2");
//            user.setPassword("test2");
//
//            ParseGeoPoint point = new ParseGeoPoint(40.00000012, -30.0);//for first user/device`
//            user.put("location", point);
//
//            user.signUpInBackground(new SignUpCallback() {
//                public void done(ParseException e) {
//                    if (e == null) {
//                        //  Hooray! Let them use the app now.
//                    } else {
//                        // Sign up didn't succeed. Look at the ParseException
//                        // to figure out what went wrong
//                    }
//                }
//            });

            ParseUser.logInInBackground("ibrahim", "test2", new LogInCallback() {
                public void done(ParseUser user, ParseException e) {
                    if (user != null) {
                        // Hooray! The user is logged in.
                    } else {
                        // Signup failed. Look at the ParseException to see what happened.
                    }
                }
            });
        }

        title = (TextView)findViewById(R.id.title);
		pebButton = (Button)findViewById(R.id.pebble_button);
        myoButton = (Button)findViewById(R.id.myo_button);

		pebButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PebbleDictionary dict = new PebbleDictionary();
                Toast peb = Toast.makeText(getApplicationContext(), "Paired with Pebble", Toast.LENGTH_SHORT);
                peb.show();
				dict.addInt32(0, 0);
				PebbleKit.sendDataToPebble(getApplicationContext(), uuid, dict);
			}

		});

        myoButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast peb = Toast.makeText(getApplicationContext(), "Work in Progress!", Toast.LENGTH_SHORT);
                peb.show();
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
				for(int i = 0; i < NUM_SAMPLES; i++) {
					for(int j = 0; j < 3; j++) {
						try {
							latest_data[(3 * i) + j] = data.getInteger((3 * i) + j).intValue();
						} catch(Exception e) {
							latest_data[(3 * i) + j] = -1;
						}
					}
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
                            if (latest_data[2] > mZ + 3.05 * stdZ) {
                                width++;
                                Toast rec = Toast.makeText(getApplicationContext(), "Registered Slapp", Toast.LENGTH_LONG);
                                rec.show();
                                if (System.currentTimeMillis() - time > 1000) {
                                    time=System.currentTimeMillis();
                                    params.put("timestamp", time);
                                    params.put("location", ParseUser.getCurrentUser().get("location"));

                                    ParseCloud.callFunctionInBackground("bumped", params, new FunctionCallback<String>() {
                                        public void done(String nameOfOther, ParseException e) {
                                            if (e == null) {
                                                Log.d("D","NOT NULL");
                                                if (nameOfOther.length()>0) {
                                                    String[] info = nameOfOther.split(":", 3);
                                                    String phone = info[0];
                                                    String name = info[1];
                                                    String email = info[2];

                                                    Intent intent = new Intent(Contacts.Intents.Insert.ACTION);
                                                    intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                                                    intent.putExtra(Contacts.Intents.Insert.EMAIL, email);
                                                    intent.putExtra(Contacts.Intents.Insert.PHONE, phone);
                                                    intent.putExtra(Contacts.Intents.Insert.NAME, name);
                                                    startActivity(intent);

                                                } else {
                                                }
                                            } else {
                                            }

                                        }
                                    });
                                }
                            }
                            else width = 0;
//                            else if (spike(latest_data[0], mX, stdX))
//                                //fistbump
//                            else if (spike(latest_data[1], mY, stdY))
//                                //handshake
                        }


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

    private double stdDev(ArrayList<Integer> array) {
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

    private boolean spike(Integer a, double mean, double std) {
        return (a.doubleValue() > mean + 2.5 * std || a.doubleValue() < mean - 2.5 * std);
    }
}
