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
package com.kmagic.solitaire;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.view.WindowManager;


public class Options {

  public Options(final Solitaire solitaire, final DrawMaster drawMaster) {
    final int type = solitaire.GetSettings().getInt("LastType", Rules.SOLITAIRE);

    solitaire.setContentView(R.layout.options);
    View view = (View) solitaire.findViewById(R.id.options_view);
    view.setFocusable(true);
    view.setFocusableInTouchMode(true);

    // Display stuff
    final boolean bigCards = solitaire.GetSettings().getBoolean("DisplayBigCards", false);
    ((RadioButton)solitaire.findViewById(R.id.normal_cards)).setChecked(!bigCards);
    ((RadioButton)solitaire.findViewById(R.id.big_cards)).setChecked(bigCards);

    final boolean displayTime = solitaire.GetSettings().getBoolean("DisplayTime", true);
    ((CheckBox)solitaire.findViewById(R.id.display_time)).setChecked(displayTime);

    // Solitaire stuff
    final boolean dealThree = solitaire.GetSettings().getBoolean("SolitaireDealThree", true);
    final boolean styleNormal = solitaire.GetSettings().getBoolean("SolitaireStyleNormal", true);
    ((RadioButton)solitaire.findViewById(R.id.deal_3)).setChecked(dealThree);
    ((RadioButton)solitaire.findViewById(R.id.deal_1)).setChecked(!dealThree);
    ((RadioButton)solitaire.findViewById(R.id.style_normal)).setChecked(styleNormal);
    ((RadioButton)solitaire.findViewById(R.id.style_vegas)).setChecked(!styleNormal);

    // Spider stuff
    final int suits = solitaire.GetSettings().getInt("SpiderSuits", 4);
    ((RadioButton)solitaire.findViewById(R.id.suits_4)).setChecked(suits == 4);
    ((RadioButton)solitaire.findViewById(R.id.suits_2)).setChecked(suits == 2);
    ((RadioButton)solitaire.findViewById(R.id.suits_1)).setChecked(suits == 1);

    // Automove 
    final int autoMove = solitaire.GetSettings().getInt("AutoMoveLevel", Rules.AUTO_MOVE_ALWAYS);
    ((RadioButton)solitaire.findViewById(R.id.auto_move_always)).setChecked(autoMove == Rules.AUTO_MOVE_ALWAYS);
    ((RadioButton)solitaire.findViewById(R.id.auto_move_fling_only)).setChecked(autoMove == Rules.AUTO_MOVE_FLING_ONLY);
    ((RadioButton)solitaire.findViewById(R.id.auto_move_never)).setChecked(autoMove == Rules.AUTO_MOVE_NEVER);

    final Button accept = (Button) solitaire.findViewById(R.id.button_accept);
    accept.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        boolean commit = false;
        boolean newGame = false;
        SharedPreferences.Editor editor = solitaire.GetSettings().edit();

        if (bigCards != ((RadioButton)solitaire.findViewById(R.id.big_cards)).isChecked()) {
          editor.putBoolean("DisplayBigCards", !bigCards);
          commit = true;
          drawMaster.DrawCards(!bigCards);
        }

        if (displayTime != ((CheckBox)solitaire.findViewById(R.id.display_time)).isChecked()) {
          editor.putBoolean("DisplayTime", !displayTime);
          commit = true;
        }

        if (dealThree != ((RadioButton)solitaire.findViewById(R.id.deal_3)).isChecked()) {
          editor.putBoolean("SolitaireDealThree", !dealThree);
          commit = true;
          if (type == Rules.SOLITAIRE) {
            newGame = true;
          }
        }
        
        if (styleNormal != ((RadioButton)solitaire.findViewById(R.id.style_normal)).isChecked()) {
          editor.putBoolean("SolitaireStyleNormal", !styleNormal);
          commit = true;
          if (type == Rules.SOLITAIRE) {
            newGame = true;
          }
        }

        int newSuits = 1;
        if (((RadioButton)solitaire.findViewById(R.id.suits_4)).isChecked()) {
          newSuits = 4;
        } else if (((RadioButton)solitaire.findViewById(R.id.suits_2)).isChecked()) {
          newSuits = 2;
        }

        if (newSuits != suits) {
          editor.putInt("SpiderSuits", newSuits);
          commit = true;
          if (type == Rules.SPIDER) {
            newGame = true;
          }
        }

        int newAutoMove = Rules.AUTO_MOVE_NEVER;
        if (((RadioButton)solitaire.findViewById(R.id.auto_move_always)).isChecked()) {
          newAutoMove = Rules.AUTO_MOVE_ALWAYS;
        } else if (((RadioButton)solitaire.findViewById(R.id.auto_move_fling_only)).isChecked()) {
          newAutoMove = Rules.AUTO_MOVE_FLING_ONLY;
        }

        if (newAutoMove != autoMove) {
          editor.putInt("AutoMoveLevel", newAutoMove);
          commit = true;
        }

        if (commit) {
          editor.commit();
          solitaire.RefreshOptions();
        }
        if (newGame) {
          solitaire.NewOptions();
        } else {
          solitaire.CancelOptions();
        }
      }
    });
    final Button decline = (Button) solitaire.findViewById(R.id.button_cancel);
    decline.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        solitaire.CancelOptions();
      }
    });

    view.setOnKeyListener(new View.OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (keyCode) {
          case KeyEvent.KEYCODE_BACK:
          case KeyEvent.KEYCODE_HOME:
            solitaire.CancelOptions();
            return true;
        }
        return false;
      }
    });
    view.requestFocus();
  }
}

