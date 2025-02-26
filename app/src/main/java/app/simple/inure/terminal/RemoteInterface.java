/*
 * Copyright (C) 2012 Steven Luo
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

package app.simple.inure.terminal;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import app.simple.inure.decorations.emulatorview.TermSession;
import app.simple.inure.extensions.activities.BaseActivity;
import app.simple.inure.preferences.ShellPreferences;
import app.simple.inure.terminal.util.SessionList;
import app.simple.inure.terminal.util.TermSettings;

public class RemoteInterface extends BaseActivity {
    
    protected static final String PRIVACT_OPEN_NEW_WINDOW = "inure.terminal.private.OPEN_NEW_WINDOW";
    protected static final String PRIVACT_SWITCH_WINDOW = "inure.terminal.private.SWITCH_WINDOW";
    protected static final String PRIVEXTRA_TARGET_WINDOW = "inure.terminal.private.target_window";
    protected static final String PRIVACT_ACTIVITY_ALIAS = "inure.terminal.TermInternal";
    
    private TermSettings mSettings;
    
    private TermService mTermService;
    private Intent mTSIntent;
    
    /**
     * Quote a string so it can be used as a parameter in bash and similar shells.
     */
    public static String quoteForBash(String s) {
        StringBuilder builder = new StringBuilder();
        String specialChars = "\"\\$`!";
        builder.append('"');
        int length = s.length();
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (specialChars.indexOf(c) >= 0) {
                builder.append('\\');
            }
            builder.append(c);
        }
        builder.append('"');
        return builder.toString();
    }
    
    protected void handleIntent() {
        TermService service = getTermService();
        if (service == null) {
            finish();
            return;
        }
        
        Intent myIntent = getIntent();
        String action = myIntent.getAction();
        if (action.equals(Intent.ACTION_SEND) && myIntent.hasExtra(Intent.EXTRA_STREAM)) {
            /* "permission.RUN_SCRIPT" not required as this is merely opening a new window. */
            Object extraStream = myIntent.getExtras().get(Intent.EXTRA_STREAM);
            if (extraStream instanceof Uri) {
                String path = ((Uri) extraStream).getPath();
                File file = new File(path);
                String dirPath = file.isDirectory() ? path : file.getParent();
                openNewWindow("cd " + quoteForBash(dirPath));
            }
        } else {
            // Intent sender may not have permissions, ignore any extras
            openNewWindow(null);
        }
        
        finish();
    }
    
    private ServiceConnection mTSConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            TermService.TSBinder binder = (TermService.TSBinder) service;
            mTermService = binder.getService();
            Log.d(TermDebug.LOG_TAG, "RemoteInterface connected to service");
            handleIntent();
        }
        
        public void onServiceDisconnected(ComponentName className) {
            mTermService = null;
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSettings = new TermSettings(getResources(), prefs);
        
        Intent TSIntent = new Intent(this, TermService.class);
        mTSIntent = TSIntent;
        startService(TSIntent);
        if (!bindService(TSIntent, mTSConnection, BIND_AUTO_CREATE)) {
            Log.e(TermDebug.LOG_TAG, "bind to service failed!");
            finish();
        }
    }
    
    @Override
    public void finish() {
        ServiceConnection conn = mTSConnection;
        if (conn != null) {
            unbindService(conn);
            
            // Stop the service if no terminal sessions are running
            TermService service = mTermService;
            if (service != null) {
                SessionList sessions = service.getSessions();
                if (sessions == null || sessions.size() == 0) {
                    stopService(mTSIntent);
                }
            }
            
            mTSConnection = null;
            mTermService = null;
        }
        super.finish();
    }
    
    protected TermService getTermService() {
        return mTermService;
    }
    
    protected String openNewWindow(String iInitialCommand) {
        TermService service = getTermService();
    
        String initialCommand = ShellPreferences.INSTANCE.getInitialCommand();
        Log.d(TermDebug.LOG_TAG, "initialCommand: " + initialCommand);
        if (iInitialCommand != null) {
            Log.d(TermDebug.LOG_TAG, "iInitialCommand: " + iInitialCommand);
            if (initialCommand != null) {
                initialCommand += System.lineSeparator() + iInitialCommand;
                Log.d(TermDebug.LOG_TAG, "initialCommand Appended: " + initialCommand);
            } else {
                initialCommand = iInitialCommand;
                Log.d(TermDebug.LOG_TAG, "initialCommand Reset: " + initialCommand);
            }
        }
        
        try {
            TermSession session = Term.createTermSession(this, mSettings, initialCommand);
            
            session.setFinishCallback(service);
            service.getSessions().add(session);
    
            String handle = UUID.randomUUID().toString();
            ((GenericTermSession) session).setHandle(handle);
    
            Intent intent = new Intent(PRIVACT_OPEN_NEW_WINDOW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
    
            return handle;
        } catch (
                IOException e) {
            return null;
        }
    }
    
    protected String appendToWindow(String handle, String iInitialCommand) {
        TermService service = getTermService();
        
        // Find the target window
        SessionList sessions = service.getSessions();
        GenericTermSession target = null;
        int index;
        for (index = 0; index < sessions.size(); ++index) {
            GenericTermSession session = (GenericTermSession) sessions.get(index);
            String h = session.getHandle();
            if (h != null && h.equals(handle)) {
                target = session;
                break;
            }
        }
        
        if (target == null) {
            // Target window not found, open a new one
            return openNewWindow(iInitialCommand);
        }
        
        if (iInitialCommand != null) {
            target.write(iInitialCommand);
            target.write('\r');
        }
        
        Intent intent = new Intent(PRIVACT_SWITCH_WINDOW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(PRIVEXTRA_TARGET_WINDOW, index);
        startActivity(intent);
        
        return handle;
    }
}
