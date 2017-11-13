package com.weqa.util.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.weqa.R;
import com.weqa.ui.LandingScreenActivity;
import com.weqa.ui.TeamSummaryActivity;
import com.weqa.util.SharedPreferencesUtil;

/**
 * Created by Manish Ballav on 9/3/2017.
 */

public class DialogUtil {

    public static class Timer {

        private Dialog dialog;
        private boolean refreshHotspots;
        private LandingScreenActivity activity;
        public boolean buttonPressed = false;
        private TextView countdown;

        public Timer(Dialog dialog, boolean refreshHotspots, LandingScreenActivity activity) {
            this.dialog = dialog;
            this.refreshHotspots = refreshHotspots;
            this.activity = activity;
            this.countdown = (TextView) dialog.findViewById(R.id.countdown);
            thread.start();
        }

        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    for (int i = 5; i > 0; i--) {
                        final int j = i;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (dialog.isShowing())
                                    countdown.setText("" + j);
                            }
                        });
                        Thread.sleep(1000); // 5 seconds timer
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog.isShowing()) {
                                countdown.setText("0");
                                dialog.dismiss();
                            }
                            if (refreshHotspots && (!buttonPressed))  {
                                activity.updateFloorplanAvailability();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static class Timer2 {

        private Dialog dialog;
        private boolean refreshHotspots;
        private LandingScreenActivity activity;
        private String qrCodeBooked, qrCodeNew;
        private String bookingTime;
        public boolean buttonPressed = false;
        private TextView countdown;

        public Timer2(Dialog dialog, boolean refreshHotspots, LandingScreenActivity activity,
                      String qrCodeBooked, String qrCodeNew, String bookingTime) {
            this.dialog = dialog;
            this.refreshHotspots = refreshHotspots;
            this.activity = activity;
            this.qrCodeBooked = qrCodeBooked;
            this.qrCodeNew = qrCodeNew;
            this.bookingTime = bookingTime;
            this.countdown = (TextView) dialog.findViewById(R.id.countdown);
            thread.start();
        }

        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    for (int i = 5; i > 0; i--) {
                        final int j = i;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (dialog.isShowing())
                                    countdown.setText("" + j);
                            }
                        });
                        Thread.sleep(1000); // 5 seconds timer
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog.isShowing()) {
                                countdown.setText("0");
                                dialog.dismiss();
                            }
                        }
                    });
                    if (!buttonPressed) {
                        SharedPreferencesUtil util = new SharedPreferencesUtil(activity);
                        util.appendBooking(qrCodeNew, bookingTime);
                        activity.releaseQRCodeItem(qrCodeBooked, false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static class Timer3 {

        private Dialog dialog;
        private Activity activity;
        public boolean buttonPressed = false;
        private TextView countdown;

        public Timer3(Dialog dialog, Activity activity) {
            this.dialog = dialog;
            this.activity = activity;
            this.countdown = (TextView) dialog.findViewById(R.id.countdown);
            thread.start();
        }

        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    for (int i = 5; i > 0; i--) {
                        final int j = i;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (dialog.isShowing())
                                    countdown.setText("" + j);
                            }
                        });
                        Thread.sleep(1000); // 5 seconds timer
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog.isShowing()) {
                                countdown.setText("0");
                                dialog.dismiss();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static class Timer4 {

        private Dialog dialog;
        private TeamSummaryActivity activity;
        private String qrCodeBooked, qrCodeNew;
        private String bookingTime;
        public boolean buttonPressed = false;
        private TextView countdown;

        public Timer4(Dialog dialog, TeamSummaryActivity activity,
                      String qrCodeBooked, String qrCodeNew, String bookingTime) {
            this.dialog = dialog;
            this.activity = activity;
            this.qrCodeBooked = qrCodeBooked;
            this.qrCodeNew = qrCodeNew;
            this.bookingTime = bookingTime;
            this.countdown = (TextView) dialog.findViewById(R.id.countdown);
            thread.start();
        }

        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    for (int i = 5; i > 0; i--) {
                        final int j = i;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (dialog.isShowing())
                                    countdown.setText("" + j);
                            }
                        });
                        Thread.sleep(1000); // 5 seconds timer
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog.isShowing()) {
                                countdown.setText("0");
                                dialog.dismiss();
                            }
                        }
                    });
                    if (!buttonPressed) {
                        SharedPreferencesUtil util = new SharedPreferencesUtil(activity);
                        util.appendBooking(qrCodeNew, bookingTime);
                        activity.releaseQRCodeItem(qrCodeBooked, false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static void showMessageDialog(Context c, String textToDisplay) {
        final Dialog dialog = new Dialog(c);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_booking);

        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.bookingmessage);
        text.setText(textToDisplay);

        Button okButton = (Button) dialog.findViewById(R.id.okButton);
        okButton.setText("Close");
        // if button is clicked, close the custom dialog
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public static void showOkDialogWithCancel(final LandingScreenActivity activity, String textToDisplay,
                                              final String qrCode, final boolean refreshHotspots) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_booking_timer);

        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.bookingmessage);
        text.setText(textToDisplay);

        // start the timer
        final DialogUtil.Timer timer = new DialogUtil.Timer(dialog, refreshHotspots, activity);

        Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
        // if button is clicked, close the custom dialog
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.buttonPressed = true;
                dialog.dismiss();
                activity.releaseQRCodeItem(qrCode, true, true);
            }
        });
        cancelButton.setText("Release");

        Button okButton = (Button) dialog.findViewById(R.id.okButton);
        okButton.setText("Close");
        // if button is clicked, close the custom dialog
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.buttonPressed = true;
                dialog.dismiss();
                if (refreshHotspots) {
                    activity.updateFloorplanAvailability();
                }
            }
        });

        dialog.show();
    }

    public static void showOkDialogWithCancel(final TeamSummaryActivity activity, String textToDisplay,
                                              final String qrCode) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_booking_timer);

        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.bookingmessage);
        text.setText(textToDisplay);

        // start the timer
        final DialogUtil.Timer3 timer = new DialogUtil.Timer3(dialog, activity);

        Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
        // if button is clicked, close the custom dialog
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.buttonPressed = true;
                dialog.dismiss();
                activity.releaseQRCodeItem(qrCode, true);
            }
        });
        cancelButton.setText("Release");

        Button okButton = (Button) dialog.findViewById(R.id.okButton);
        okButton.setText("Close");
        // if button is clicked, close the custom dialog
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.buttonPressed = true;
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public static void showOkDialog(final LandingScreenActivity activity, String textToDisplay,
                                    final boolean refreshHotspots, final boolean doTimer) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (doTimer) {
            dialog.setContentView(R.layout.dialog_booking_timer);
        }
        else {
            dialog.setContentView(R.layout.dialog_booking);
        }

        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.bookingmessage);
        text.setText(textToDisplay);


        Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
        cancelButton.setVisibility(View.GONE);

        Button okButton = (Button) dialog.findViewById(R.id.okButton);
        // if button is clicked, close the custom dialog
        okButton.setText("Close");
        if (doTimer) {
            // start the timer
            final DialogUtil.Timer timer = new DialogUtil.Timer(dialog, refreshHotspots, activity);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    timer.buttonPressed = true;
                    dialog.dismiss();
                    if (refreshHotspots) {
                        activity.updateFloorplanAvailability();
                    }
                }
            });
        }
        else {
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (refreshHotspots) {
                        activity.updateFloorplanAvailability();
                    }
                }
            });
        }

        dialog.show();
    }

    public static void showOkDialog(final Activity activity, String textToDisplay,
                                    final boolean doTimer) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (doTimer) {
            dialog.setContentView(R.layout.dialog_booking_timer);
        }
        else {
            dialog.setContentView(R.layout.dialog_booking);
        }

        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.bookingmessage);
        text.setText(textToDisplay);


        Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
        cancelButton.setVisibility(View.GONE);

        Button okButton = (Button) dialog.findViewById(R.id.okButton);
        // if button is clicked, close the custom dialog
        okButton.setText("Close");
        if (doTimer) {
            // start the timer
            final DialogUtil.Timer3 timer = new DialogUtil.Timer3(dialog, activity);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    timer.buttonPressed = true;
                    dialog.dismiss();
                }
            });
        }
        else {
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }

        dialog.show();
    }


    public static void showDialogWithThreeButtons(final LandingScreenActivity activity, String textToDisplay,
                                              final String qrCode, final boolean refreshHotspots) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_booking_3buttons);

        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.message);
        text.setText(textToDisplay);

        Button cancelButton = (Button) dialog.findViewById(R.id.button1);
        cancelButton.setText("Release");
        // if button is clicked, close the custom dialog
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                activity.releaseQRCodeItem(qrCode, true, refreshHotspots);
            }
        });

        Button renewButton = (Button) dialog.findViewById(R.id.button2);
        renewButton.setText("Renew");
        // if button is clicked, close the custom dialog
        renewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                activity.renewQRCodeItem(qrCode);
            }
        });

        Button okButton = (Button) dialog.findViewById(R.id.button3);
        okButton.setText("Close");
        // if button is clicked, close the custom dialog
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public static void showDialogWithThreeButtons(final TeamSummaryActivity activity, String textToDisplay,
                                                  final String qrCode) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_booking_3buttons);

        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.message);
        text.setText(textToDisplay);

        Button cancelButton = (Button) dialog.findViewById(R.id.button1);
        cancelButton.setText("Release");
        // if button is clicked, close the custom dialog
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                activity.releaseQRCodeItem(qrCode, true);
            }
        });

        Button renewButton = (Button) dialog.findViewById(R.id.button2);
        renewButton.setText("Renew");
        // if button is clicked, close the custom dialog
        renewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                activity.renewQRCodeItem(qrCode);
            }
        });

        Button okButton = (Button) dialog.findViewById(R.id.button3);
        okButton.setText("Close");
        // if button is clicked, close the custom dialog
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}
