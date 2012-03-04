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

import android.graphics.Canvas;
import android.util.Log;
import java.lang.InterruptedException;
import java.lang.Runnable;
import java.lang.Thread;
import java.lang.Math;
import java.util.concurrent.Semaphore;

public class AnimateCard {

  private static final float PPF = 40;

  protected SolitaireView mView;
  private Card[] mCard;
  private CardAnchor mCardAnchor;
  private int mCount;
  private int mFrames;
  private float mDx;
  private float mDy;
  private boolean mAnimate;
  private Runnable mCallback;

  public AnimateCard(SolitaireView view) {
    mView = view;
    mAnimate = false;
    mCard = new Card[104];
    mCallback = null;
  }

  public boolean GetAnimate() { return mAnimate; }

  public void Draw(DrawMaster drawMaster, Canvas canvas) {
    if (mAnimate) {
      for (int j = 0; j < mCount; j++) {
        mCard[j].MovePosition(-mDx, -mDy);
      }
      for (int i = 0; i < mCount; i++) {
        drawMaster.DrawCard(canvas, mCard[i]);
      }
      mFrames--;
      if (mFrames <= 0) {
        mAnimate = false;
        Finish();
      }
    }
  }

  public void MoveCards(Card[] card, CardAnchor anchor, int count, Runnable callback) {
    float x = anchor.GetX();
    float y = anchor.GetNewY();
    mCardAnchor = anchor;
    mCallback = callback;
    mAnimate = true;

    for (int i = 0; i < count; i++) {
      mCard[i] = card[i];
    }
    mCount = count;
    Move(mCard[0], x, y);
  }

  public void MoveCard(Card card, CardAnchor anchor) {
    float x = anchor.GetX();
    float y = anchor.GetNewY();
    mCardAnchor = anchor;
    mCallback = null;
    mAnimate = true;

    mCard[0] = card;
    mCount = 1;
    Move(card, x, y);
  }

  private void Move(Card card, float x, float y) {
    float dx = x - card.GetX(); 
    float dy = y - card.GetY(); 

    mFrames = Math.round((float)Math.sqrt(dx * dx + dy * dy) / PPF);
    if (mFrames == 0) {
      mFrames = 1;
    }
    mDx = dx / mFrames;
    mDy = dy / mFrames;

    mView.StartAnimating();
    if (!mAnimate) {
      Finish();
    }
  }

  private void Finish() {
    for (int i = 0; i < mCount; i++) {
      mCardAnchor.AddCard(mCard[i]);
      mCard[i] = null;
    }
    mCardAnchor = null;
    mView.DrawBoard();
    if (mCallback != null) {
      mCallback.run();
    }
  }

  public void Cancel() {
    if (mAnimate) {
      for (int i = 0; i < mCount; i++) {
        mCardAnchor.AddCard(mCard[i]);
        mCard[i] = null;
      }
      mCardAnchor = null;
      mAnimate = false;
    }
  }
}
