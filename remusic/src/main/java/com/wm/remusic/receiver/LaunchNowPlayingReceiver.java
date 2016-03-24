/*
 * Copyright (C) 2014 Saravan Pantham
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wm.remusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wm.remusic.MainApplication;
import com.wm.remusic.activity.PlayingActivity;
import com.wm.remusic.service.MusicPlayer;

public class LaunchNowPlayingReceiver extends BroadcastReceiver {

    private MainApplication mApp;

    @Override
    public void onReceive(Context context, Intent intent) {
        mApp = (MainApplication) context.getApplicationContext();

        if (MusicPlayer.isPlaying()) {
            Intent activityIntent = new Intent(context, PlayingActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);

        }

    }

}
