/*
 * Hewlett-Packard Company
 * All rights reserved.
 *
 * This file, its contents, concepts, methods, behavior, and operation
 * (collectively the "Software") are protected by trade secret, patent,
 * and copyright laws. The use of the Software is governed by a license
 * agreement. Disclosure of the Software to third parties, in any form,
 * in whole or in part, is expressly prohibited except as authorized by
 * the license agreement.
 */
package com.hp.mss.hpprint.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.print.PrintAttributes;
import android.print.PrintDocumentInfo;
import android.print.PrintJob;
import android.print.PrintJobInfo;
import android.print.PrinterId;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.hp.mss.hpprint.model.ApplicationMetricsData;
import com.hp.mss.hpprint.model.ImagePrintItem;
import com.hp.mss.hpprint.model.PrintItem;
import com.hp.mss.hpprint.model.PrintJobData;
import com.hp.mss.hpprint.model.PrintMetricsData;
import com.hp.mss.hpprint.model.asset.ImageAsset;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the utility class that sends metrics to HP Print Metrics server
 * It is used inside HP print SDK, you should not create this yourself.
 */
class PrintMetricsCollector extends Thread {

    private static final String TAG = "PrintMetricsCollector";
    private static final String API_METHOD_NAME = "/v1/mobile_app_metrics";

    private static final String PRINT_SESSION_ID_LABEL = "print_session_id";

    private static final int PRINT_JOB_WAIT_TIME = 1000;
    private static final int MILS = 1000;

    private PrintJob printJob;
    private Handler metricsHandler;
    private Activity hostActivity;
    private static HashMap<String,String> appMetrics;
    private static HashMap<String,String> appSpecificMetrics;
    private String previewPaperSize;
    PrintJobData printJobData;
    PrintPluginStatusHelper pluginStatusHelper;

    /**
     * Called inside the Print SDK to send printing related data to HP server.
     * @param activity
     * @param printJob  a print job generated by PrintManager
     * @param appSpecificMetrics any metrics related data in key/value hash format.
     */
    public PrintMetricsCollector(Activity activity, PrintJob printJob, HashMap<String,String> appSpecificMetrics) {
        this(activity, printJob);
        this.appSpecificMetrics = appSpecificMetrics;
        this.pluginStatusHelper = PrintPluginStatusHelper.getInstance(activity.getApplicationContext());
    }


    /**
     * Called inside the Print SDK to send printing related data to HP server.
     * @param activity
     * @param printJob  a print job generated by PrintManager
     */
    public PrintMetricsCollector(Activity activity, PrintJob printJob) {
        this.hostActivity = activity;
        this.printJob = printJob;
        this.metricsHandler = new Handler();
        this.printJobData = PrintUtil.getPrintJobData();
        this.previewPaperSize = printJobData.getPreviewPaperSize();
    }

    @Override
    public void run() {
        if (printJob == null ) {
            return;
        }

        PrintMetricsData printMetricsData = new PrintMetricsData();
        printMetricsData.numOfPluginsInstalled = String.valueOf(pluginStatusHelper.getNumOfPluginsInstalled());
        printMetricsData.numOfPluginsEnabled = String.valueOf(pluginStatusHelper.getNumOfPluginsEnabled());


        String printJobInfoString = printJob.getInfo().toString();

        if (isJobFailed(printJob) && !printJobInfoString.contains("PDF printer")) {
            ImageLoaderUtil.cleanUpFileDirectory();
            printMetricsData.previewPaperSize = this.previewPaperSize;

            if (printJob.isFailed()) {
                printMetricsData.printResult = PrintMetricsData.PRINT_RESULT_FAILED;
            } else if (printJob.isCancelled()) {
                printMetricsData.printResult = PrintMetricsData.PRINT_RESULT_CANCEL;
            } else {
                printMetricsData.printResult = PrintMetricsData.PRINT_RESULT_FAILED;
            }

            postMetrics(printMetricsData);
            return;
        }

        if (hasJobInfo(printJob) || printJobInfoString.contains("PDF printer")) {

            PrintJobInfo printJobInfo = printJob.getInfo();
            PrintAttributes printJobAttributes = printJobInfo.getAttributes();
            PrinterId printerId = printJobInfo.getPrinterId();

            printMetricsData.previewPaperSize = this.previewPaperSize;

            PrintItem printItem = printJobData.getPrintItem(printJobAttributes.getMediaSize());

            printMetricsData.contentType = printItem.getAsset().getContentType();
            printMetricsData.contentWidthPixels = Integer.toString(printItem.getAsset().getAssetWidth());
            printMetricsData.contentHeightPixels = Integer.toString(printItem.getAsset().getAssetHeight());

            printMetricsData.printResult = PrintMetricsData.PRINT_RESULT_SUCCESS;

            try {
                Method gdi = PrintJobInfo.class.getMethod("getDocumentInfo");
                PrintDocumentInfo printDocumentInfo = (PrintDocumentInfo) gdi.invoke(printJobInfo);
                Method gsn = PrinterId.class.getMethod("getServiceName");
                ComponentName componentName = (ComponentName) gsn.invoke(printerId);

                printMetricsData.printPluginTech = componentName.getPackageName();

                if (printDocumentInfo.getContentType() == PrintDocumentInfo.CONTENT_TYPE_DOCUMENT) {
                    printMetricsData.paperType = PrintMetricsData.CONTENT_TYPE_DOCUMENT;
                } else if (printDocumentInfo.getContentType() == PrintDocumentInfo.CONTENT_TYPE_PHOTO) {
                    printMetricsData.paperType = PrintMetricsData.CONTENT_TYPE_PHOTO;
                } else if (printDocumentInfo.getContentType() == PrintDocumentInfo.CONTENT_TYPE_UNKNOWN) {
                    printMetricsData.paperType = PrintMetricsData.CONTENT_TYPE_UNKNOWN;
                }

                if(printJobInfo.getAttributes().getColorMode() == PrintAttributes.COLOR_MODE_MONOCHROME)
                    printMetricsData.blackAndWhiteFilter = "1";
                else
                    printMetricsData.blackAndWhiteFilter = "0";

                String width = Double.toString(printJobAttributes.getMediaSize().getWidthMils() / (float) MILS);
                String height = Double.toString(printJobAttributes.getMediaSize().getHeightMils() / (float) MILS);

                printMetricsData.paperSize = (width + " x " + height);
                printMetricsData.printerID = ApplicationMetricsData.md5(printerId.getLocalId());
                printMetricsData.numberOfCopy = String.valueOf(printJobInfo.getCopies());

                postMetrics(printMetricsData);

            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                Log.e(TAG, "CollectionRunner", e);
            }
            ImageLoaderUtil.cleanUpFileDirectory();
        } else {
            metricsHandler.postDelayed(this, PRINT_JOB_WAIT_TIME);
        }
    }

    public static void setApplicationMetrics(HashMap<String,String> map) {
        appMetrics = map;
    }

    private void postMetrics(PrintMetricsData printMetricsData) {
        postMetricsToHPServer(hostActivity.getApplicationContext(), printMetricsData);

        if (PrintUtil.metricsListener != null) {
            ((PrintUtil.PrintMetricsListener) PrintUtil.metricsListener).onPrintMetricsDataPosted(printMetricsData);
        }
    }

    private static boolean hasJobInfo(final PrintJob printJob) {
        return (printJob.isQueued() || printJob.isCompleted() || printJob.isStarted());
    }

    private static boolean isJobFailed(final PrintJob printJob) {
        return (printJob.isFailed() || printJob.isBlocked() || printJob.isCancelled());
    }

    private void postMetricsToHPServer(final Context context, final PrintMetricsData data) {
        if (!PrintUtil.sendPrintMetrics)
            return;
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest sr = new StringRequest(Request.Method.POST, MetricsUtil.getMetricsServer(context) + API_METHOD_NAME, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("PrintMetricsCollector", response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("PrintMetricsCollector", error.toString());
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = getMetricsParams(data);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String authorizationString = MetricsUtil.getAuthorizationString();

                Map<String,String> params = new HashMap<String, String>();

                params.put("Authorization", authorizationString);
                return params;
            }
        };
        queue.add(sr);
    }


    private Map<String, String> getMetricsParams(PrintMetricsData printMetricsData) {
        HashMap<String, String> combinedMetrics = new HashMap<String, String>();

        appMetrics = (new ApplicationMetricsData(hostActivity.getApplicationContext())).toMap();
        combinedMetrics.putAll(appMetrics);
        combinedMetrics.putAll(printMetricsData.toMap());

        if (appSpecificMetrics != null && !appSpecificMetrics.isEmpty())
            combinedMetrics.putAll(appSpecificMetrics);

        combinedMetrics.put(PRINT_SESSION_ID_LABEL, String.valueOf(MetricsUtil.getCurrentSessionCounter(hostActivity)));

        return combinedMetrics;
    }



}


