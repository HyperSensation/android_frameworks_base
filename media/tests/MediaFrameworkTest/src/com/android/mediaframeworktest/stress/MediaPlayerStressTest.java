/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mediaframeworktest.stress;

import com.android.mediaframeworktest.MediaFrameworkTest;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.SurfaceHolder;

import com.android.mediaframeworktest.MediaNames;
import com.android.mediaframeworktest.functional.CodecTest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;

/**
 * Junit / Instrumentation test case for the media player
 */
public class MediaPlayerStressTest extends InstrumentationTestCase {
    private String TAG = "MediaPlayerStressTest";

    public MediaPlayerStressTest() {
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    private int mTotalPlaybackError = 0;
    private int mTotalComplete = 0;
    private int mTotalInfoUnknown = 0;
    private int mTotalVideoTrackLagging = 0;
    private int mTotalBadInterleaving = 0;
    private int mTotalNotSeekable = 0;
    private int mTotalMetaDataUpdate = 0;

    private void writeTestOutput(String filename, Writer output) throws Exception{
        output.write("File Name: " + filename);
        output.write(" Complete: " + CodecTest.onCompleteSuccess);
        output.write(" Error: " + CodecTest.mPlaybackError);
        output.write(" Unknown Info: " + CodecTest.mMediaInfoUnknownCount);
        output.write(" Track Lagging: " +  CodecTest.mMediaInfoVideoTrackLaggingCount);
        output.write(" Bad Interleaving: " + CodecTest.mMediaInfoBadInterleavingCount);
        output.write(" Not Seekable: " + CodecTest.mMediaInfoNotSeekableCount);
        output.write(" Info Meta data update: " + CodecTest.mMediaInfoMetdataUpdateCount);
        output.write("\n");
    }

    private void writeTestSummary(Writer output) throws Exception{
        output.write("Total Result:\n");
        output.write("Total Complete: " + mTotalComplete + "\n");
        output.write("Total Error: " + mTotalPlaybackError + "\n");
        output.write("Total Unknown Info: " + mTotalInfoUnknown + "\n");
        output.write("Total Track Lagging: " + mTotalVideoTrackLagging + "\n" );
        output.write("Total Bad Interleaving: " + mTotalBadInterleaving + "\n");
        output.write("Total Not Seekable: " + mTotalNotSeekable + "\n");
        output.write("Total Info Meta data update: " + mTotalMetaDataUpdate + "\n");
        output.write("\n");
    }

    private void updateTestResult(){
        if (CodecTest.onCompleteSuccess){
            mTotalComplete++;
        }
        else if (CodecTest.mPlaybackError){
            mTotalPlaybackError++;
        }
        mTotalInfoUnknown += CodecTest.mMediaInfoUnknownCount;
        mTotalVideoTrackLagging += CodecTest.mMediaInfoVideoTrackLaggingCount;
        mTotalBadInterleaving += CodecTest.mMediaInfoBadInterleavingCount;
        mTotalNotSeekable += CodecTest.mMediaInfoNotSeekableCount;
        mTotalMetaDataUpdate += CodecTest.mMediaInfoMetdataUpdateCount;
    }

    //Test that will start the playback for all the videos
    //under the samples folder
    @LargeTest
    public void testVideoPlayback() throws Exception {
        String fileWithError = "Filename:\n";
        File playbackOutput = new File("/sdcard/PlaybackTestResult.txt");
        Writer output = new BufferedWriter(new FileWriter(playbackOutput, true));

        boolean testResult = true;
        // load directory files
        boolean onCompleteSuccess = false;
        File dir = new File(MediaNames.MEDIA_SAMPLE_POOL);

        Instrumentation inst = getInstrumentation();
        Intent intent = new Intent();

        intent.setClass(getInstrumentation().getTargetContext(), MediaFrameworkTest.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        String[] children = dir.list();
        if (children == null) {
            Log.v("MediaPlayerApiTest:testMediaSamples", "dir is empty");
            return;
        } else {
            for (int i = 0; i < children.length; i++) {
                Activity act = inst.startActivitySync(intent);
                //Get filename of directory
                String filename = children[i];
                onCompleteSuccess =
                    CodecTest.playMediaSamples(dir + "/" + filename);
                if (!onCompleteSuccess){
                    //Don't fail the test right away, print out the failure file.
                    fileWithError += filename + '\n';
                    Log.v(TAG, "Failure File : " + fileWithError);
                    testResult = false;
                }
                Thread.sleep(3000);
                //Call onCreat to recreate the surface
                act.finish();
                //Write test result to an output file
                writeTestOutput(filename,output);
                //Get the summary
                updateTestResult();
            }
            writeTestSummary(output);
            output.close();
            assertTrue("testMediaSamples", testResult);
       }
    }
}