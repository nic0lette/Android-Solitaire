/*
  Copyright 2008 Google Inc.
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
       http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package co.cutely.solitaire;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

// Base activity class.
public class Solitaire extends Activity {
    private static final int MENU_NEW_GAME = 1;
    private static final int MENU_RESTART = 2;
    private static final int MENU_OPTIONS = 3;
    private static final int MENU_SAVE_QUIT = 4;
    private static final int MENU_QUIT = 5;
    private static final int MENU_SOLITAIRE = 6;
    private static final int MENU_SPIDER = 7;
    private static final int MENU_FREECELL = 8;
    private static final int MENU_FORTYTHIEVES = 9;
    private static final int MENU_STATS = 10;
    private static final int MENU_HELP = 11;

    // View extracted from main.xml.
    private View mMainView;
    private SolitaireView mSolitaireView;
    private SharedPreferences mSettings;

    private boolean mDoSave;

    private float mPixelDensity;

    // Shared preferences are where the various user settings are stored.
    public SharedPreferences GetSettings() {
        return mSettings;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDoSave = true;

        // Get display density
        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mPixelDensity = metrics.density;

        // Force landscape and no title for extra room
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // If the user has never accepted the EULA show it again.
        mSettings = getSharedPreferences("SolitairePreferences", 0);
        setContentView(R.layout.main);
        mMainView = findViewById(R.id.main_view);
        mSolitaireView = (SolitaireView) findViewById(R.id.solitaire);
        mSolitaireView.SetTextView((TextView) findViewById(R.id.text));

        // Set screen density in Solitaire View
        mSolitaireView.setPixelDensity(mPixelDensity);

        // StartSolitaire(savedInstanceState);
    }

    // Entry point for starting the game.
    // public void StartSolitaire(Bundle savedInstanceState) {
    @Override
    public void onStart() {
        super.onStart();
        if (mSettings.getBoolean("SolitaireSaveValid", false)) {
            SharedPreferences.Editor editor = GetSettings().edit();
            editor.putBoolean("SolitaireSaveValid", false);
            editor.commit();
            // If save is corrupt, just start a new game.
            if (mSolitaireView.LoadSave()) {
                HelpSplashScreen();
                return;
            }
        }

        mSolitaireView.InitGame(mSettings.getInt("LastType", Rules.SOLITAIRE));
        HelpSplashScreen();
    }

    // Force show the help if this is the first time played. Sadly no one reads
    // it anyways.
    private void HelpSplashScreen() {
        if (!mSettings.getBoolean("PlayedBefore", false)) {
            mSolitaireView.DisplayHelp();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);

        SubMenu subMenu = menu.addSubMenu(0, MENU_NEW_GAME, 0, R.string.menu_newgame);
        subMenu.add(0, MENU_SOLITAIRE, 0, R.string.menu_solitaire);
        subMenu.add(0, MENU_SPIDER, 0, R.string.menu_spider);
        subMenu.add(0, MENU_FREECELL, 0, R.string.menu_freecell);
        subMenu.add(0, MENU_FORTYTHIEVES, 0, R.string.menu_fortythieves);

        menu.add(0, MENU_RESTART, 0, R.string.menu_restart);
        menu.add(0, MENU_OPTIONS, 0, R.string.menu_options);
        menu.add(0, MENU_SAVE_QUIT, 0, R.string.menu_save_quit);
        menu.add(0, MENU_QUIT, 0, R.string.menu_quit);
        menu.add(0, MENU_STATS, 0, R.string.menu_stats);
        menu.add(0, MENU_HELP, 0, R.string.menu_help);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SOLITAIRE:
                mSolitaireView.InitGame(Rules.SOLITAIRE);
                break;
            case MENU_SPIDER:
                mSolitaireView.InitGame(Rules.SPIDER);
                break;
            case MENU_FREECELL:
                mSolitaireView.InitGame(Rules.FREECELL);
                break;
            case MENU_FORTYTHIEVES:
                mSolitaireView.InitGame(Rules.FORTYTHIEVES);
                break;
            case MENU_RESTART:
                mSolitaireView.RestartGame();
                break;
            case MENU_STATS:
                DisplayStats();
                break;
            case MENU_OPTIONS:
                DisplayOptions();
                break;
            case MENU_HELP:
                mSolitaireView.DisplayHelp();
                break;
            case MENU_SAVE_QUIT:
                mSolitaireView.SaveGame();
                mDoSave = false;
                finish();
                break;
            case MENU_QUIT:
                mDoSave = false;
                finish();
                break;
        }

        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSolitaireView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDoSave) {
            mSolitaireView.SaveGame();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSolitaireView.onResume();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void DisplayOptions() {
        mSolitaireView.SetTimePassing(false);
        new Options(this, mSolitaireView.GetDrawMaster());
    }

    public void DisplayStats() {
        mSolitaireView.SetTimePassing(false);
        new Stats(this, mSolitaireView);
    }

    public void CancelOptions() {
        setContentView(mMainView);
        mSolitaireView.requestFocus();
        mSolitaireView.SetTimePassing(true);
    }

    public void NewOptions() {
        setContentView(mMainView);
        mSolitaireView.InitGame(mSettings.getInt("LastType", Rules.SOLITAIRE));
    }

    // This is called for option changes that require a refresh, but not a new game
    public void RefreshOptions() {
        setContentView(mMainView);
        mSolitaireView.RefreshOptions();
    }
}
