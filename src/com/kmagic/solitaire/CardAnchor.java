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


class CardAnchor {

  public static final int MAX_CARDS = 104;
  public static final int SEQ_SINK = 1;
  public static final int SUIT_SEQ_STACK = 2;
  public static final int DEAL_FROM = 3;
  public static final int DEAL_TO = 4;
  public static final int SPIDER_STACK = 5;
  public static final int FREECELL_STACK = 6;
  public static final int FREECELL_HOLD = 7;
  public static final int GENERIC_ANCHOR = 8;

  private int mNumber;
  protected Rules mRules;
  protected float mX;
  protected float mY;
  protected Card[] mCard;
  protected int mCardCount;
  protected int mHiddenCount;
  protected float mLeftEdge;
  protected float mRightEdge;
  protected float mBottom;
  protected boolean mDone;

  //Variables for GenericAnchor
  protected int mSTARTSEQ;
  protected int mBUILDSEQ;
  protected int mMOVESEQ;
  protected int mBUILDSUIT;
  protected int mMOVESUIT;
  protected boolean mBUILDWRAP;
  protected boolean mMOVEWRAP;
  protected int mDROPOFF;
  protected int mPICKUP;
  protected int mDISPLAY; 
  protected int mHACK;
  
  // ==========================================================================
  // Create a CardAnchor
  // -------------------
  public static CardAnchor CreateAnchor(int type, int number, Rules rules) {
    CardAnchor ret = null;
    switch (type) {
      case SEQ_SINK:
        ret = new SeqSink();
        break;
      case SUIT_SEQ_STACK:
        ret = new SuitSeqStack();
        break;
      case DEAL_FROM:
        ret = new DealFrom();
        break;
      case DEAL_TO:
        ret = new DealTo();
        break;
      case SPIDER_STACK:
        ret = new SpiderStack();
        break;
      case FREECELL_STACK:
        ret = new FreecellStack();
        break;
      case FREECELL_HOLD:
        ret = new FreecellHold();
        break;
      case GENERIC_ANCHOR:
        ret = new GenericAnchor();
        break;
    }
    ret.SetRules(rules);
    ret.SetNumber(number);
    return ret;
  }

  public CardAnchor() {
    mX = 1;
    mY = 1;
    mCard = new Card[MAX_CARDS];
    mCardCount = 0;
    mHiddenCount = 0;
    mLeftEdge = -1;
    mRightEdge = -1;
    mBottom = -1;
    mNumber = -1;
    mDone = false;
  }

  // ==========================================================================
  // Getters and Setters
  // -------------------
  public Card[] GetCards() { return mCard; }
  public int GetCount() { return mCardCount; }
  public int GetHiddenCount() { return mHiddenCount; }
  public float GetLeftEdge() { return mLeftEdge; }
  public int GetNumber() { return mNumber; }
  public float GetRightEdge() { return mRightEdge; }
  public int GetVisibleCount() { return mCardCount - mHiddenCount; }
  public int GetMovableCount() { return mCardCount > 0 ? 1 : 0; }
  public float GetX() { return mX; }
  public float GetNewY() { return mY; }
  public boolean IsDone() { return mDone; }

  public void SetBottom(float edge) { mBottom = edge; }
  public void SetHiddenCount(int count) { mHiddenCount = count; }
  public void SetLeftEdge(float edge) { mLeftEdge = edge; }
  public void SetMaxHeight(int maxHeight) { }
  public void SetNumber(int number) { mNumber = number; }
  public void SetRightEdge(float edge) { mRightEdge = edge; }
  public void SetRules(Rules rules) { mRules = rules; }
  public void SetShowing(int showing) {  }
  protected void SetCardPosition(int idx) { mCard[idx].SetPosition(mX, mY); }
  public void SetDone(boolean done) { mDone = done; }

  //Methods for GenericAnchor
  public void SetStartSeq(int seq){ mSTARTSEQ = seq; }
  public void SetSeq(int seq){ mBUILDSEQ = seq; mMOVESEQ = seq; }
  public void SetBuildSeq(int buildseq){ mBUILDSEQ = buildseq;  }
  public void SetMoveSeq(int moveseq){ mMOVESEQ = moveseq;  }
  
  public void SetWrap(boolean wrap){ mBUILDWRAP = wrap; mMOVEWRAP = wrap; }
  public void SetMoveWrap(boolean movewrap){ mMOVEWRAP = movewrap;  }
  public void SetBuildWrap(boolean buildwrap){ mBUILDWRAP = buildwrap;  }
  
  public void SetSuit(int suit){ mBUILDSUIT = suit; mMOVESUIT = suit; }
  public void SetBuildSuit(int buildsuit){ mBUILDSUIT = buildsuit;  }
  public void SetMoveSuit(int movesuit){ mMOVESUIT = movesuit;  }
  
  public void SetBehavior(int beh){ mDROPOFF = beh; mPICKUP = beh; }
  public void SetDropoff(int dropoff){ mDROPOFF = dropoff;  }
  public void SetPickup(int pickup){ mPICKUP = pickup;  }
  
  public void SetDisplay(int display){ mDISPLAY = display;  }
  
  public void SetHack(int hack){ mHACK = hack; }
  //End Methods for Generic Anchor  
  
  public void SetPosition(float x, float y) {
    mX = x;
    mY = y;
    for (int i = 0; i < mCardCount; i++) {
      SetCardPosition(i);
    }
  }

  // ==========================================================================
  // Functions to add cards
  // ----------------------
  public void AddCard(Card card) {
    mCard[mCardCount++] = card;
    SetCardPosition(mCardCount - 1);
  }

  public void AddMoveCard(MoveCard moveCard) {
    int count = moveCard.GetCount();
    Card[] cards = moveCard.DumpCards();

    for (int i = 0; i < count; i++) {
      AddCard(cards[i]);
    }
  }

  public boolean DropSingleCard(Card card) { return false; }
  public boolean CanDropCard(MoveCard moveCard, int close) { return false; }

  // ==========================================================================
  // Functions to take cards
  // -----------------------
  public Card[] GetCardStack() { return null; }

  public Card GrabCard(float x, float y) {
    Card ret = null;
    if (mCardCount > 0 && IsOverCard(x, y)) {
      ret = PopCard();
    }
    return ret;
  }

  public Card PopCard() {
    Card ret = mCard[--mCardCount];
    mCard[mCardCount] = null;
    return ret;
  }

  // ==========================================================================
  // Functions to interact with cards
  // --------------------------------
  public boolean TapCard(float x, float y) { return false; }

  public boolean UnhideTopCard() {
    if (mCardCount  > 0 && mHiddenCount > 0 && mHiddenCount == mCardCount) {
      mHiddenCount--;
      return true;
    }
    return false;
  }
  public boolean ExpandStack(float x, float y) { return false; }
  public boolean CanMoveStack(float x, float y) { return false; }


  // ==========================================================================
  // Functions to check locations
  // ----------------------------
  private boolean IsOver(float x, float y, boolean deck, int close) {
    float clx = mCardCount == 0 ? mX : mCard[mCardCount - 1].GetX();
    float leftX = mLeftEdge == -1 ? clx : mLeftEdge;
    float rightX = mRightEdge == -1 ? clx + Card.WIDTH : mRightEdge;
    float topY = (mCardCount == 0 || deck) ? mY : mCard[mCardCount-1].GetY();
    float botY = mCardCount > 0 ? mCard[mCardCount - 1].GetY() : mY;
    botY += Card.HEIGHT;

    leftX -= close*Card.WIDTH/2;
    rightX += close*Card.WIDTH/2;
    topY -= close*Card.HEIGHT/2;
    botY += close*Card.HEIGHT/2;
    if (mBottom != -1 && botY + 10 >= mBottom)
      botY = mBottom;

    if (x >= leftX && x <= rightX && y >= topY && y <= botY) {
      return true;
    }
    return false;
  }

  protected boolean IsOverCard(float x, float y) {
    return IsOver(x, y, false, 0);
  }
  protected boolean IsOverCard(float x, float y, int close) {
    return IsOver(x, y, false, close);
  }

  protected boolean IsOverDeck(float x, float y) {
    return IsOver(x, y, true, 0);
  }

  // ==========================================================================
  // Functions to Draw
  // ----------------------------
  public void Draw(DrawMaster drawMaster, Canvas canvas) {
    if (mCardCount == 0) {
      drawMaster.DrawEmptyAnchor(canvas, mX, mY, mDone);
    } else {
      drawMaster.DrawCard(canvas, mCard[mCardCount-1]);
    }
  }
}

// Straight up default
class DealTo extends CardAnchor {
  private int mShowing;
  public DealTo() {
    super();
    mShowing = 1;
  }

  @Override
  public void SetShowing(int showing) { mShowing = showing; }

  @Override
  protected void SetCardPosition(int idx) { 
    if (mShowing == 1) {
      mCard[idx].SetPosition(mX, mY);
    } else {
      if (idx < mCardCount - mShowing) {
        mCard[idx].SetPosition(mX, mY);
      } else {
        int offset = mCardCount - mShowing;
        offset = offset < 0 ? 0 : offset;
        mCard[idx].SetPosition(mX + (idx - offset) * Card.WIDTH/2, mY);
      }
    }
  }

  @Override
  public void AddCard(Card card) {
    super.AddCard(card);
    SetPosition(mX, mY);
  }

  @Override
  public boolean UnhideTopCard() {
    SetPosition(mX, mY);
    return false;
  }

  @Override
  public Card PopCard() {
    Card ret = super.PopCard();
    SetPosition(mX, mY);
    return ret;
  }

  @Override
  public void Draw(DrawMaster drawMaster, Canvas canvas) {
    if (mCardCount == 0) {
      drawMaster.DrawEmptyAnchor(canvas, mX, mY, mDone);
    } else {
      for (int i = mCardCount - mShowing; i < mCardCount; i++) {
        if (i >= 0) {
          drawMaster.DrawCard(canvas, mCard[i]);
        }
      }
    }
  }
}

// Abstract stack anchor
class SeqStack extends CardAnchor {
  protected static final int SMALL_SPACING = 7;
  protected static final int HIDDEN_SPACING = 3;

  protected int mSpacing;
  protected boolean mHideHidden;
  protected int mMaxHeight;

  public SeqStack() {
    super();
    mSpacing = GetMaxSpacing();
    mHideHidden = false;
    mMaxHeight = Card.HEIGHT;
  }

  @Override
  public void SetMaxHeight(int maxHeight) {
    mMaxHeight = maxHeight;
    CheckSizing();
    SetPosition(mX, mY);
  }

  // This can't be a constant as Card.HEIGHT isn't constant.
  protected int GetMaxSpacing() {
    return Card.HEIGHT/3;
  }

  @Override
  protected void SetCardPosition(int idx) {
    if (idx < mHiddenCount) {
      if (mHideHidden) {
        mCard[idx].SetPosition(mX, mY);
      } else {
        mCard[idx].SetPosition(mX, mY + HIDDEN_SPACING * idx);
      }
    } else {
      int startY = mHideHidden ? HIDDEN_SPACING : mHiddenCount * HIDDEN_SPACING;
      int y = (int)mY + startY + (idx - mHiddenCount) * mSpacing;
      mCard[idx].SetPosition(mX, y);
    }
  }

  @Override
  public void SetHiddenCount(int count) {
    super.SetHiddenCount(count);
    CheckSizing();
    SetPosition(mX, mY);
  }

  @Override
  public void AddCard(Card card) {
    super.AddCard(card);
    CheckSizing();
  }

  @Override
  public Card PopCard() {
    Card ret = super.PopCard();
    CheckSizing();
    return ret;
  }

  @Override
  public boolean ExpandStack(float x, float y) {
    if (IsOverDeck(x, y)) {
      if (mHiddenCount >= mCardCount) {
        mHiddenCount = mCardCount == 0 ? 0 : mCardCount - 1;
      } else if (mCardCount - mHiddenCount > 1) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int GetMovableCount() { return GetVisibleCount(); }

  @Override
  public void Draw(DrawMaster drawMaster, Canvas canvas) {
    if (mCardCount == 0) {
      drawMaster.DrawEmptyAnchor(canvas, mX, mY, mDone);
    } else {
      for (int i = 0; i < mCardCount; i++) {
        if (i < mHiddenCount) {
          drawMaster.DrawHiddenCard(canvas, mCard[i]);
        } else {
          drawMaster.DrawCard(canvas, mCard[i]);
        }
      }
    }
  }

  private void CheckSizing() {
    if (mCardCount < 2 || mCardCount - mHiddenCount < 2) {
      mSpacing = GetMaxSpacing();
      mHideHidden = false;
      return;
    }
    int max = mMaxHeight;
    int hidden = mHiddenCount;
    int showing = mCardCount - hidden;
    int spaceLeft = max - (hidden * HIDDEN_SPACING) - Card.HEIGHT;
    int spacing = spaceLeft / (showing - 1);

    if (spacing < SMALL_SPACING && hidden > 1) {
      mHideHidden = true;
      spaceLeft = max - HIDDEN_SPACING - Card.HEIGHT;
      spacing = spaceLeft / (showing - 1);
    } else {
      mHideHidden = false;
      if (spacing > GetMaxSpacing()) {
        spacing = GetMaxSpacing();
      }
    }
    if (spacing != mSpacing) {
      mSpacing = spacing;
      SetPosition(mX, mY);
    }
  }

  public float GetNewY() {
    if (mCardCount == 0) {
      return mY;
    }
    return mCard[mCardCount-1].GetY() + mSpacing;
  }
}

// Anchor where cards to deal come from
class DealFrom extends CardAnchor {

  @Override
  public Card GrabCard(float x, float y) { return null; }

  @Override
  public boolean TapCard(float x, float y) {
    if (IsOverCard(x, y)) {
      mRules.EventAlert(Rules.EVENT_DEAL, this);
      return true;
    }
    return false;
  }

  @Override
  public void Draw(DrawMaster drawMaster, Canvas canvas) {
    if (mCardCount == 0) {
      drawMaster.DrawEmptyAnchor(canvas, mX, mY, mDone);
    } else {
      drawMaster.DrawHiddenCard(canvas, mCard[mCardCount-1]);
    }
  }
}

// Anchor that holds increasing same suited cards
class SeqSink extends CardAnchor {

  @Override
  public void AddCard(Card card) {
    super.AddCard(card);
    mRules.EventAlert(Rules.EVENT_STACK_ADD, this);
  }

  @Override
  public boolean CanDropCard(MoveCard moveCard, int close) {
    Card card = moveCard.GetTopCard();
    float x = card.GetX() + Card.WIDTH/2;
    float y = card.GetY() + Card.HEIGHT/2;
    Card topCard = mCardCount > 0 ? mCard[mCardCount - 1] : null;
    float my = mCardCount > 0 ? topCard.GetY() : mY;

    if (IsOverCard(x, y, close)) {
      if (moveCard.GetCount() == 1) {
        if ((topCard == null && card.GetValue() == 1) ||
            (topCard != null && card.GetSuit() == topCard.GetSuit() &&
             card.GetValue() == topCard.GetValue() + 1)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public boolean DropSingleCard(Card card) {
    Card topCard = mCardCount > 0 ? mCard[mCardCount - 1] : null;
    if ((topCard == null && card.GetValue() == 1) ||
        (topCard != null && card.GetSuit() == topCard.GetSuit() &&
         card.GetValue() == topCard.GetValue() + 1)) {
      //AddCard(card);
      return true;
    }
    return false;
  }
}

// Regular color alternating solitaire stack
class SuitSeqStack extends SeqStack {

  @Override
  public boolean CanDropCard(MoveCard moveCard, int close) {

    Card card = moveCard.GetTopCard();
    float x = card.GetX() + Card.WIDTH/2;
    float y = card.GetY() + Card.HEIGHT/2;
    Card topCard = mCardCount > 0 ? mCard[mCardCount - 1] : null;
    float my = mCardCount > 0 ? topCard.GetY() : mY;

    if (IsOverCard(x, y, close)) {
      if (topCard == null) {
        if (card.GetValue() == Card.KING) {
          return true;
        }        
      } else if ((card.GetSuit()&1) != (topCard.GetSuit()&1) &&
                 card.GetValue() == topCard.GetValue() - 1) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Card[] GetCardStack() {
    int visibleCount = GetVisibleCount();
    Card[] ret = new Card[visibleCount];

    for (int i = visibleCount-1; i >= 0; i--) {
      ret[i] = PopCard();
    }
    return ret;
  }
  
  @Override
  public boolean CanMoveStack(float x, float y) { return super.ExpandStack(x, y); }
}

// Spider solitaire style stack
class SpiderStack extends SeqStack {

  @Override
  public void AddCard(Card card) {
    super.AddCard(card);
    mRules.EventAlert(Rules.EVENT_STACK_ADD, this);
  }

  @Override
  public boolean CanDropCard(MoveCard moveCard, int close) {

    Card card = moveCard.GetTopCard();
    float x = card.GetX() + Card.WIDTH/2;
    float y = card.GetY() + Card.HEIGHT/2;
    Card topCard = mCardCount > 0 ? mCard[mCardCount - 1] : null;
    float my = mCardCount > 0 ? topCard.GetY() : mY;

    if (IsOverCard(x, y, close)) {
      if (topCard == null || card.GetValue() == topCard.GetValue() - 1) {
        return true;
      }
    }

    return false;
  }

  @Override
  public int GetMovableCount() {
    if (mCardCount < 2)
      return mCardCount;

    int retCount = 1;
    int suit = mCard[mCardCount-1].GetSuit();
    int val = mCard[mCardCount-1].GetValue();

    for (int i = mCardCount-2; i >= mHiddenCount; i--, retCount++, val++) {
      if (mCard[i].GetSuit() != suit || mCard[i].GetValue() != val + 1) {
        break;
      }
    }

    return retCount;
  }

  @Override
  public Card[] GetCardStack() {
    int retCount = GetMovableCount();

    Card[] ret = new Card[retCount];

    for (int i = retCount-1; i >= 0; i--) {
      ret[i] = PopCard();
    }

    return ret;
  }

  @Override
  public boolean ExpandStack(float x, float y) {
    if (super.ExpandStack(x, y)) {
      Card bottom = mCard[mCardCount-1];
      Card second = mCard[mCardCount-2];
      if (bottom.GetSuit() == second.GetSuit() &&
          bottom.GetValue() == second.GetValue() - 1) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean CanMoveStack(float x, float y) {
    if (super.ExpandStack(x, y)) {
      float maxY = mCard[mCardCount-GetMovableCount()].GetY();

      if (y >= maxY - Card.HEIGHT/2) {
        return true;
      }
    }
    return false;
  }

}

// Freecell stack
class FreecellStack extends SeqStack {

  @Override
  public boolean CanDropCard(MoveCard moveCard, int close) {

    Card card = moveCard.GetTopCard();
    float x = card.GetX() + Card.WIDTH/2;
    float y = card.GetY() + Card.HEIGHT/2;
    Card topCard = mCardCount > 0 ? mCard[mCardCount - 1] : null;
    float my = mCardCount > 0 ? topCard.GetY() : mY;

    if (IsOverCard(x, y, close)) {
      if (topCard == null) {
        if (mRules.CountFreeSpaces() >= moveCard.GetCount()) {
          return true;
        }
      } else if ((card.GetSuit()&1) != (topCard.GetSuit()&1) &&
                 card.GetValue() == topCard.GetValue() - 1) {
        return true;
      }
    }

    return false;
  }

  @Override
  public int GetMovableCount() {
    if (mCardCount < 2)
      return mCardCount;

    int retCount = 1;
    int maxMoveCount = mRules.CountFreeSpaces() + 1;

    for (int i = mCardCount - 2; i >= 0 && retCount < maxMoveCount; i--, retCount++) {
      if ((mCard[i].GetSuit()&1) == (mCard[i+1].GetSuit()&1) ||
          mCard[i].GetValue() != mCard[i+1].GetValue() + 1) {
        break;
      }
    }

    return retCount;
  }

  @Override
  public Card[] GetCardStack() {
    int retCount = GetMovableCount();
    Card[] ret = new Card[retCount];

    for (int i = retCount-1; i >= 0; i--) {
      ret[i] = PopCard();
    }
    return ret;
  }

  @Override
  public boolean ExpandStack(float x, float y) {
    if (super.ExpandStack(x, y)) {
      if (mRules.CountFreeSpaces() > 0) {
        Card bottom = mCard[mCardCount-1];
        Card second = mCard[mCardCount-2];
        if ((bottom.GetSuit()&1) != (second.GetSuit()&1) &&
            bottom.GetValue() == second.GetValue() - 1) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean CanMoveStack(float x, float y) {
    if (super.ExpandStack(x, y)) {
      float maxY = mCard[mCardCount-GetMovableCount()].GetY();
      if (y >= maxY - Card.HEIGHT/2) {
        return true;
      }
    }
    return false;
  }
}

// Freecell holding pen
class FreecellHold extends CardAnchor {

  @Override
  public boolean CanDropCard(MoveCard moveCard, int close) {
    Card card = moveCard.GetTopCard();
    if (mCardCount == 0 && moveCard.GetCount() == 1 &&
        IsOverCard(card.GetX() + Card.WIDTH/2, card.GetY() + Card.HEIGHT/2, close)) {
      return true;
    }
    return false;
  }

}

// New Abstract
class GenericAnchor extends CardAnchor {

  //Sequence start values
  public static final int START_ANY=1; // An empty stack can take any card.
  public static final int START_KING=2; // An empty stack can take only a king.

  //Value Sequences
  public static final int SEQ_ANY=1; //You can build as you like
  public static final int SEQ_SEQ=2;  //Building only allows sequential
  public static final int SEQ_ASC=3;  //Ascending only
  public static final int SEQ_DSC=4;  //Descending only
    
  //Suit Sequences that limits how adding cards to the stack works
  public static final int SUIT_ANY=1;  //Build doesn't care about suite
  public static final int SUIT_RB=2;  //Must alternate Red & Black
  public static final int SUIT_OTHER=3;//As long as different
  public static final int SUIT_COLOR=4;//As long as same color
  public static final int SUIT_SAME=5; //As long as same suit
    
  //Pickup & Dropoff Behavior
  public static final int PACK_NONE=1;  // Interaction in this mode not allowed
  public static final int PACK_ONE=2;  //Can only accept 1 card
  public static final int PACK_MULTI=3;  //Can accept multiple cards
  public static final int PACK_FIXED=4;  //Don't think this will ever be used
  public static final int PACK_LIMIT_BY_FREE=5; //For freecell style movement
    
  //Anchor Display (Hidden vs. Shown faces)
  public static final int DISPLAY_ALL=1;  //All cards are shown
  public static final int DISPLAY_HIDE=2; //All cards are hidden
  public static final int DISPLAY_MIX=3;  //Uses a mixture
  public static final int DISPLAY_ONE=4;  //Displays one only

  //Hack to fix Spider Dealing
  public static final int DEALHACK=1;
    
  protected static final int SMALL_SPACING = 7;
  protected static final int HIDDEN_SPACING = 3;

  protected int mSpacing;
  protected boolean mHideHidden;
  protected int mMaxHeight;
  
  public GenericAnchor(){
    super();
    SetStartSeq(GenericAnchor.SEQ_ANY);
    SetBuildSeq(GenericAnchor.SEQ_ANY);
    SetBuildWrap(false);
    SetBuildSuit(GenericAnchor.SUIT_ANY);
    SetDropoff(GenericAnchor.PACK_NONE);
    SetPickup(GenericAnchor.PACK_NONE);
    SetDisplay(GenericAnchor.DISPLAY_ALL);    
    mSpacing = GetMaxSpacing();
    mHideHidden = false;
    mMaxHeight = Card.HEIGHT;
  }

  @Override
  public void SetMaxHeight(int maxHeight) {
    mMaxHeight = maxHeight;
    CheckSizing();
    SetPosition(mX, mY);
  }

  @Override
  protected void SetCardPosition(int idx) {
    if (idx < mHiddenCount) {
      if (mHideHidden) {
        mCard[idx].SetPosition(mX, mY);
      } else {
        mCard[idx].SetPosition(mX, mY + HIDDEN_SPACING * idx);
      }
    } else {
      int startY = mHideHidden ? HIDDEN_SPACING : mHiddenCount * HIDDEN_SPACING;
      int y = (int)mY + startY + (idx - mHiddenCount) * mSpacing;
      mCard[idx].SetPosition(mX, y);
    }
  }

  @Override
  public void SetHiddenCount(int count) {
    super.SetHiddenCount(count);
    CheckSizing();
    SetPosition(mX, mY);
  }
  
  @Override
  public void AddCard(Card card) {
    super.AddCard(card);
    CheckSizing();
    if (mHACK == GenericAnchor.DEALHACK){
      mRules.EventAlert(Rules.EVENT_STACK_ADD, this);
    }
  }

  @Override
  public Card PopCard() {
    Card ret = super.PopCard();
    CheckSizing();
    return ret;
  }
  
  @Override
  public boolean CanDropCard(MoveCard moveCard, int close) {
    if (mDROPOFF == GenericAnchor.PACK_NONE){
      return false;
    }
    
    Card card = moveCard.GetTopCard();
    float x = card.GetX() + Card.WIDTH/2;
    float y = card.GetY() + Card.HEIGHT/2;
    //Card topCard = mCardCount > 0 ? mCard[mCardCount - 1] : null;
    //float my = mCardCount > 0 ? topCard.GetY() : mY;
    if (IsOverCard(x, y, close)) {
      return CanBuildCard(card);
    }
    return false;
  }
  
  public boolean CanBuildCard(Card card){
    // SEQ_ANY will allow all
    if (mBUILDSEQ == GenericAnchor.SEQ_ANY){
      return true;
    }
    Card topCard = mCardCount > 0 ? mCard[mCardCount - 1] : null;
    // Rules for empty stacks
    if (topCard == null) {
      switch (mSTARTSEQ) {
        case GenericAnchor.START_KING:
          return card.GetValue() == Card.KING;
        case GenericAnchor.START_ANY:
        default:
          return true;
      }
    }
    int value = card.GetValue();
    int suit = card.GetSuit();
    int tvalue = topCard.GetValue();
    int tsuit = topCard.GetSuit();
    // Fail if sequence is wrong
    switch (mBUILDSEQ){
    //WRAP_NOWRAP=1; //Building stacks do not wrap
    //WRAP_WRAP=2;   //Building stacks wraps around
      case GenericAnchor.SEQ_ASC:
        if (value - tvalue != 1){
          return false;
        }
        break;
      case GenericAnchor.SEQ_DSC:
        if (tvalue - value != 1){
          return false;
        }
        break;
      case GenericAnchor.SEQ_SEQ:
        if (Math.abs(tvalue - value) != 1){
          return false;
        }
        break;
    }
    // Fail if suit is wrong
    switch (mBUILDSUIT){
      case GenericAnchor.SUIT_RB:
        if (Math.abs(tsuit - suit)%2 == 0){  return false;  }
        break;
      case GenericAnchor.SUIT_OTHER:
        if (tsuit == suit){  return false;  }
        break;
      case GenericAnchor.SUIT_COLOR:
        if (Math.abs(tsuit - suit) != 2){  return false;  }
        break;
      case GenericAnchor.SUIT_SAME:
        if (tsuit != suit){  return false;  }
        break;
    }
    // Passes all rules
    return true;
  }
  
  @Override
  public void Draw(DrawMaster drawMaster, Canvas canvas) {
    if (mCardCount == 0) {
      drawMaster.DrawEmptyAnchor(canvas, mX, mY, mDone);
      return;
    }
    switch (mDISPLAY){
      case GenericAnchor.DISPLAY_ALL:
        for (int i = 0; i < mCardCount; i++) {
          drawMaster.DrawCard(canvas, mCard[i]);
        }
        break;
      case GenericAnchor.DISPLAY_HIDE:
        for (int i = 0; i < mCardCount; i++) {
          drawMaster.DrawHiddenCard(canvas, mCard[i]);
        }
        break;
      case GenericAnchor.DISPLAY_MIX:
        for (int i = 0; i < mCardCount; i++) {
          if (i < mHiddenCount) {
            drawMaster.DrawHiddenCard(canvas, mCard[i]);
          } else {
            drawMaster.DrawCard(canvas, mCard[i]);
          }
        }
        break;
      case GenericAnchor.DISPLAY_ONE:
        for (int i = 0; i < mCardCount; i++) {
          if (i < mCardCount-1) {
            drawMaster.DrawHiddenCard(canvas, mCard[i]);
          } else {
            drawMaster.DrawCard(canvas, mCard[i]);
          }
        }
        break;
    }
  }
  
  @Override
  public boolean ExpandStack(float x, float y) {
    if (IsOverDeck(x, y)) {
      return (GetMovableCount() > 0);
      /*
      if (mHiddenCount >= mCardCount) {
        mHiddenCount = mCardCount == 0 ? 0 : mCardCount - 1;
      } else if (mCardCount - mHiddenCount > 1) {
        return true;
      }
      */
    }
    return false;
  }
  
  @Override
  public boolean CanMoveStack(float x, float y) { return ExpandStack(x, y); }

  @Override
  public Card[] GetCardStack() {
    int movableCount = GetMovableCount();
    Card[] ret = new Card[movableCount];
    for (int i = movableCount-1; i >= 0; i--) {
      ret[i] = PopCard();
    }
    return ret;
  }

  @Override
  public int GetMovableCount() {
    int visibleCount = GetVisibleCount();  
    if (visibleCount == 0 || mPICKUP == GenericAnchor.PACK_NONE){
      return 0;
    }
    int seq_allowed = 1;
    if (visibleCount > 1){
      int i = mCardCount-1;
      boolean g;
      boolean h;      
      do {
        g = true;
        h = true;
        switch (mMOVESEQ){
          case GenericAnchor.SEQ_ANY:
            h = true;
            break;
          case GenericAnchor.SEQ_ASC:
            h = this.is_seq_asc(i-1, i, mMOVEWRAP);
            break;
          case GenericAnchor.SEQ_DSC:
            h = this.is_seq_asc(i, i-1, mMOVEWRAP);
            break;
          case GenericAnchor.SEQ_SEQ:
            h = (this.is_seq_asc(i, i-1, mMOVEWRAP) || 
                this.is_seq_asc(i-1, i, mMOVEWRAP));
            break;
        }
        if (h == false){
          g = false;
        }
        switch (mMOVESUIT){
          case GenericAnchor.SUIT_ANY:
            h = true;
            break;
          case GenericAnchor.SUIT_COLOR:
            h = !this.is_suit_rb(i-1,i);
            break;
          case GenericAnchor.SUIT_OTHER:
            h = this.is_suit_other(i-1, i);
            break;
          case GenericAnchor.SUIT_RB:
            h = this.is_suit_rb(i-1, i);
            break;
          case GenericAnchor.SUIT_SAME:
            h = this.is_suit_same(i-1, i);
            break;
        }
        if (h == false){
          g = false;
        }
        if (g){  seq_allowed++;  }
        i--;
      }while(g && (mCardCount - i) < visibleCount);
    }
    
    switch (mPICKUP){
      case GenericAnchor.PACK_NONE:
        return 0;
      case GenericAnchor.PACK_ONE:
        seq_allowed = Math.min(1, seq_allowed);
        break;
      case GenericAnchor.PACK_MULTI:
        break;
      case GenericAnchor.PACK_FIXED:
        //seq_allowed = Math.min( xmin, seq_allowed);
        break;
      case GenericAnchor.PACK_LIMIT_BY_FREE:
        seq_allowed = Math.min(mRules.CountFreeSpaces()+1, seq_allowed);
        break;
    }
    return seq_allowed;
  }
  
  public boolean is_seq_asc(int p1, int p2, boolean wrap){
    Card c1 = mCard[p1];
    Card c2 = mCard[p2];
    int v1 = c1.GetValue();
    int v2 = c2.GetValue();
    
    if (v2 + 1 == v1){
      return true;
    }
    if (wrap){
      if (v2 == Card.KING && v1 == Card.ACE){
        return true;
      }
    }
    return false;
  }
  public boolean is_suit_rb(int p1, int p2){
    Card c1 = mCard[p1];
    Card c2 = mCard[p2];
    int s1 = c1.GetSuit();
    int s2 = c2.GetSuit();
    if (  (s1 == Card.CLUBS || s1 == Card.SPADES) &&
          (s2 == Card.HEARTS || s2 == Card.DIAMONDS)  ){
      return true;
    }
    if (  (s1 == Card.HEARTS || s1 == Card.DIAMONDS) &&
          (s2 == Card.CLUBS || s2 == Card.SPADES)  ){
      return true;
    }
    return false;    
  }
  public boolean is_suit_same(int p1, int p2){
    return (mCard[p1].GetSuit() == mCard[p2].GetSuit());
  }
  public boolean is_suit_other(int p1, int p2){
    return (mCard[p1].GetSuit() != mCard[p2].GetSuit());
  }  

  private void CheckSizing() {
    if (mCardCount < 2 || mCardCount - mHiddenCount < 2) {
      mSpacing = GetMaxSpacing();
      mHideHidden = false;
      return;
    }
    int max = mMaxHeight;
    int hidden = mHiddenCount;
    int showing = mCardCount - hidden;
    int spaceLeft = max - (hidden * HIDDEN_SPACING) - Card.HEIGHT;
    int spacing = spaceLeft / (showing - 1);

    if (spacing < SMALL_SPACING && hidden > 1) {
      mHideHidden = true;
      spaceLeft = max - HIDDEN_SPACING - Card.HEIGHT;
      spacing = spaceLeft / (showing - 1);
    } else {
      mHideHidden = false;
      if (spacing > GetMaxSpacing()) {
        spacing = GetMaxSpacing();
      }
    }
    if (spacing != mSpacing) {
      mSpacing = spacing;
      SetPosition(mX, mY);
    }
  }
  // This can't be a constant as Card.HEIGHT isn't constant.
  protected int GetMaxSpacing() {
    return Card.HEIGHT/3;
  }

  public float GetNewY() {
    if (mCardCount == 0) {
      return mY;
    }
    return mCard[mCardCount-1].GetY() + mSpacing;
  }
}
