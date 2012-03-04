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
import android.graphics.PointF;

import android.util.Log;


class MoveCard {

  private static final int MAX_CARDS = 13;

  private boolean mValid;
  private Card[] mCard;
  private int mCardCount;
  private CardAnchor mCardAnchor;
  private PointF mOriginalPoint;

  public MoveCard() {
    mCard = new Card[MAX_CARDS];
    mOriginalPoint = new PointF(1, 1);
    Clear();
  }

  public boolean IsValid() { return mValid; }
  public CardAnchor GetAnchor() { return mCardAnchor; }
  public int GetCount() { return mCardCount; }
  public Card GetTopCard() { return mCard[0]; }
  public void SetAnchor(CardAnchor anchor) { 
    mCardAnchor = anchor;
  }

  public void Draw(DrawMaster drawMaster, Canvas canvas) {
    for (int i = 0; i < mCardCount; i++) {
      drawMaster.DrawCard(canvas, mCard[i]);
    }
  }

  private void Clear() {
    mValid = false;
    mCardCount = 0;
    mCardAnchor = null;
    for (int i = 0; i < MAX_CARDS; i++) {
      mCard[i] = null;
    }
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

  public void AddCard(Card card) {
    if (mCardCount == 0) {
      mOriginalPoint.set(card.GetX(), card.GetY());
    }
    mCard[mCardCount++] = card;
    mValid = true;
  }

  public void MovePosition(float dx, float dy) {
    for (int i = 0; i < mCardCount; i++) {
      mCard[i].MovePosition(dx, dy);
    }
  }

  public Card[] DumpCards() {
    return DumpCards(true);
  }

  public Card[] DumpCards(boolean unhide) {
    Card[] ret = null;
    if (mValid) {
      mValid = false;
      if (unhide) {
        mCardAnchor.UnhideTopCard();
      }
      ret = new Card[mCardCount];
      for (int i = 0; i < mCardCount; i++) {
        ret[i] = mCard[i];
      }
      Clear();
    }
    return ret;
  }

  public void InitFromSelectCard(SelectCard selectCard, float x, float y) {
    int count = selectCard.GetCount();
    mCardAnchor = selectCard.GetAnchor();
    Card[] cards = selectCard.DumpCards();

    for (int i = 0; i < count; i++) {
      cards[i].SetPosition(x - Card.WIDTH/2, y - Card.HEIGHT/2 + 15*i);
      AddCard(cards[i]);
    }
    mValid = true;
  }

  public void InitFromAnchor(CardAnchor cardAnchor, float x, float y) {
    mCardAnchor = cardAnchor;
    Card[] cards = cardAnchor.GetCardStack();

    for (int i = 0; i < cards.length; i++) {
      cards[i].SetPosition(x, y + 15*i);
      AddCard(cards[i]);
    }
    mValid = true;
  }

  public boolean HasMoved() {
    float x = mCard[0].GetX();
    float y = mCard[0].GetY();

    if (x >= mOriginalPoint.x - 2 && x <= mOriginalPoint.x + 2 &&
        y >= mOriginalPoint.y - 2 && y <= mOriginalPoint.y + 2) {
      return false;
    }
    return true;
  }
}

