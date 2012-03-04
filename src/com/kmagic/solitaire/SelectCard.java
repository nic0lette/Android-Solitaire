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

class SelectCard {

  private static final int MAX_CARDS = 13;

  private boolean mValid;
  private int mSelected;
  private Card[] mCard;
  private int mCardCount;
  private CardAnchor mCardAnchor;
  private float mLeftEdge;
  private float mRightEdge;
  private int mHeight;

  public SelectCard() {
    mHeight = 1;
    mCard = new Card[MAX_CARDS];
    Clear();
  }

  private void Clear() {
    mValid = false;
    mSelected = -1;
    mCardCount = 0;
    mLeftEdge = -1;
    mRightEdge = -1;
    mCardAnchor = null;
    for (int i = 0; i < MAX_CARDS; i++) {
      mCard[i] = null;
    }
  }

  public void SetHeight(int height) { mHeight = height; }
  public boolean IsValid() { return mValid; }

  public CardAnchor GetAnchor() { return mCardAnchor; }
  public int GetCount() {
    if (mSelected == -1)
      return mCardCount;
    return mCardCount - mSelected;
  }

  public void Draw(DrawMaster drawMaster, Canvas canvas) {
    drawMaster.DrawLightShade(canvas);
    for (int i = 0; i < mCardCount; i++) {
      drawMaster.DrawCard(canvas, mCard[i]);
    }
  }

  public void InitFromAnchor(CardAnchor cardAnchor) {
    mValid = true;
    mSelected = -1;
    mCardAnchor = cardAnchor;
    Card[] card = cardAnchor.GetCardStack();
    for (int i = 0; i < card.length; i++) {
      mCard[i] = card[i];
    }
    mCardCount = card.length;

    int mid = mCardCount / 2;
    if (mCardCount % 2 == 0) {
      mid--;
    }
    float x = mCard[0].GetX(); 
    float y = mCard[mid].GetY();
    if (y - mid * (Card.HEIGHT + 5) < 0) {
      mid = 0;
      y = 5;
    }

    for (int i = 0; i < mCardCount; i++) {
      mCard[i].SetPosition(x, y + (i - mid) * (Card.HEIGHT + 5));
    }

    mLeftEdge = cardAnchor.GetLeftEdge();
    mRightEdge = cardAnchor.GetRightEdge();
  }

  public boolean Tap(float x, float y) {
    float left = mLeftEdge == -1 ? mCard[0].GetX() : mLeftEdge;
    float right = mRightEdge == -1 ? mCard[0].GetX() + Card.WIDTH : mRightEdge;
    mSelected = -1;
    if (x >= left && x <= right) {
      for (int i = 0; i < mCardCount; i++) {
        if (y >= mCard[i].GetY() && y <= mCard[i].GetY() + Card.HEIGHT) {
          mSelected = i;
          return true;
        }
      }
    }
    return false;
  }

  public void Release() {
    if (mValid) {
      mValid = false;
      for (int i = 0; i < mCardCount; i++) {
        mCardAnchor.AddCard(mCard[i]);
      }
      Clear();
    }
  }

  public Card[] DumpCards() {
    Card[] ret = null;
    if (mValid) {
      mValid = false;
      if (mSelected > 0) {
        for (int i = 0; i < mCardCount; i++) {
          if (i < mSelected) {
            mCardAnchor.AddCard(mCard[i]);
          } else if (i == mSelected) {
            for (int j = 0; i < mCardCount; i++, j++) {
              mCard[j] = mCard[i];
            }
            break;
          }
        }
      }

      ret = new Card[GetCount()];
      for (int i = 0; i < GetCount(); i++) {
        ret[i] = mCard[i];
      }
      Clear();
    }
    return ret;
  }

  public void Scroll(float dy) {
    float x, y;
    for (int i = 0; i < mCardCount; i++) {
      x = mCard[i].GetX();
      y = mCard[i].GetY() - dy;
      mCard[i].SetPosition(x, y);
    }
  }

  public boolean IsOnCard() { return mSelected != -1; } 
}


