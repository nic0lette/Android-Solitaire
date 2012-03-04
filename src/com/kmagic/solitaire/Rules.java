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

import android.os.Bundle;
import android.util.Log;
import android.graphics.Canvas;
import java.lang.InterruptedException;
import java.lang.Thread;
import java.util.Stack;


public abstract class Rules {

  public static final int SOLITAIRE = 1;
  public static final int SPIDER = 2;
  public static final int FREECELL = 3;
  public static final int FORTYTHIEVES = 4;

  public static final int EVENT_INVALID = -1;
  public static final int EVENT_DEAL = 1;
  public static final int EVENT_STACK_ADD = 2;
  public static final int EVENT_FLING = 3;
  public static final int EVENT_SMART_MOVE = 4;
  public static final int EVENT_DEAL_NEXT = 5;

  public static final int AUTO_MOVE_ALWAYS = 2;
  public static final int AUTO_MOVE_FLING_ONLY = 1;
  public static final int AUTO_MOVE_NEVER = 0;

  private int mType;
  protected SolitaireView mView;
  protected Stack<Move> mMoveHistory;
  protected AnimateCard mAnimateCard; 
  protected boolean mIgnoreEvents;
  protected EventPoster mEventPoster;


  // Anchors
  protected CardAnchor[] mCardAnchor;
  protected int mCardAnchorCount;

  protected Deck mDeck;
  protected int mCardCount;

  // Automove
  protected int mAutoMoveLevel;
  protected boolean mWasFling;

  public int GetType() { return mType; }
  public int GetCardCount() { return mCardCount; }
  public CardAnchor[] GetAnchorArray() { return mCardAnchor; }
  public void SetType(int type) { mType = type; }
  public void SetView(SolitaireView view) { mView = view; }
  public void SetMoveHistory(Stack<Move> moveHistory) { mMoveHistory = moveHistory; }
  public void SetAnimateCard(AnimateCard animateCard) { mAnimateCard = animateCard; }
  public void SetIgnoreEvents(boolean ignore) { mIgnoreEvents = ignore; }
  public void SetEventPoster(EventPoster ep) { mEventPoster = ep; }
  public boolean GetIgnoreEvents() { return mIgnoreEvents; }
  public int GetRulesExtra() { return 0; }
  public String GetGameTypeString() { return ""; }
  public String GetPrettyGameTypeString() { return ""; }
  public boolean HasScore() { return false; }
  public boolean HasString() { return false; }
  public String GetString() { return ""; }
  public void SetCarryOverScore(int score) {}
  public int GetScore() { return 0; }
  public void AddDealCount() {}

  public int CountFreeSpaces() { return 0; }
  protected void SignalWin() { mView.DisplayWin(); }

  abstract public void Init(Bundle map);
  public void EventAlert(int event) { if (!mIgnoreEvents) { mEventPoster.PostEvent(event); mView.Refresh(); } }
  public void EventAlert(int event, CardAnchor anchor) { if (!mIgnoreEvents) { mEventPoster.PostEvent(event, anchor);  mView.Refresh();} }
  public void EventAlert(int event, CardAnchor anchor, Card card) { if (!mIgnoreEvents) { mEventPoster.PostEvent(event, anchor, card);  mView.Refresh();} }
  public void ClearEvent() { mEventPoster.ClearEvent(); }
  abstract public void EventProcess(int event, CardAnchor anchor);
  abstract public void EventProcess(int event, CardAnchor anchor, Card card);
  abstract public void EventProcess(int event);
  abstract public void Resize(int width, int height);
  public boolean Fling(MoveCard moveCard) { moveCard.Release(); return false; }
  public void HandleEvents() { 
    while (!mIgnoreEvents && mEventPoster.HasEvent()) {
      mEventPoster.HandleEvent();
    }
  }

  public void RefreshOptions() {
    mAutoMoveLevel = mView.GetSettings().getInt("AutoMoveLevel", Rules.AUTO_MOVE_ALWAYS);
    mWasFling = false;
  }

  public static Rules CreateRules(int type, Bundle map, SolitaireView view,
                                  Stack<Move> moveHistory, AnimateCard animate) {
    Rules ret = null;
    switch (type) {
      case SOLITAIRE:
        ret = new NormalSolitaire();
        break;
      case SPIDER:
        ret = new Spider();
        break;
      case FREECELL:
        ret = new Freecell();
        break;
      case FORTYTHIEVES:
        ret = new FortyThieves();
        break;
    }

    if (ret != null) {
      ret.SetType(type);
      ret.SetView(view);
      ret.SetMoveHistory(moveHistory);
      ret.SetAnimateCard(animate);
      ret.SetEventPoster(new EventPoster(ret));
      ret.RefreshOptions();
      ret.Init(map);
    }
    return ret;
  }
}

class NormalSolitaire extends Rules {

  private boolean mDealThree;
  private int mDealsLeft;
  private String mScoreString;
  private int mLastScore;
  private int mCarryOverScore;

  @Override
  public void Init(Bundle map) {
    mIgnoreEvents = true;
    mDealThree = mView.GetSettings().getBoolean("SolitaireDealThree", true);

    // Thirteen total anchors for regular solitaire
    mCardCount = 52;
    mCardAnchorCount = 13;
    mCardAnchor = new CardAnchor[mCardAnchorCount];

    // Top dealt from anchors
    mCardAnchor[0] = CardAnchor.CreateAnchor(CardAnchor.DEAL_FROM, 0, this);
    mCardAnchor[1] = CardAnchor.CreateAnchor(CardAnchor.DEAL_TO, 1, this);
    if (mDealThree) {
      mCardAnchor[1].SetShowing(3);
    } else {
      mCardAnchor[1].SetShowing(1);
    }

    // Top anchors for placing cards
    for (int i = 0; i < 4; i++) {
      mCardAnchor[i+2] = CardAnchor.CreateAnchor(CardAnchor.SEQ_SINK, i+2, this);
    }

    // Middle anchor stacks
    for (int i = 0; i < 7; i++) {
      mCardAnchor[i+6] = CardAnchor.CreateAnchor(CardAnchor.GENERIC_ANCHOR, i+6, this);
      mCardAnchor[i+6].SetStartSeq(GenericAnchor.START_KING);
      mCardAnchor[i+6].SetBuildSeq(GenericAnchor.SEQ_DSC);
      mCardAnchor[i+6].SetMoveSeq(GenericAnchor.SEQ_ASC);
      mCardAnchor[i+6].SetSuit(GenericAnchor.SUIT_RB);
      mCardAnchor[i+6].SetWrap(false);
      mCardAnchor[i+6].SetBehavior(GenericAnchor.PACK_MULTI);
      mCardAnchor[i+6].SetDisplay(GenericAnchor.DISPLAY_MIX);
    }

    if (map != null) {
      // Do some assertions, default to a new game if we find an invalid state
      if (map.getInt("cardAnchorCount") == 13 &&
          map.getInt("cardCount") == 52) {
        int[] cardCount = map.getIntArray("anchorCardCount");
        int[] hiddenCount = map.getIntArray("anchorHiddenCount");
        int[] value = map.getIntArray("value");
        int[] suit = map.getIntArray("suit");
        int cardIdx = 0;
        mDealsLeft = map.getInt("rulesExtra");

        for (int i = 0; i < 13; i++) {
          for (int j = 0; j < cardCount[i]; j++, cardIdx++) {
            Card card = new Card(value[cardIdx], suit[cardIdx]);
            mCardAnchor[i].AddCard(card);
          }
          mCardAnchor[i].SetHiddenCount(hiddenCount[i]);
        }
        if (mDealsLeft != -1) {
          // Reset to zero as GetScore() uses it in its calculation.
          mCarryOverScore = 0;
          mCarryOverScore = map.getInt("score") - GetScore();
        }

        mIgnoreEvents = false;
        // Return here so an invalid save state will result in a new game
        return;
      }
    }

    mDeck = new Deck(1);
    for (int i = 0; i < 7; i++) {
      for (int j = 0; j <= i; j++) {
        mCardAnchor[i+6].AddCard(mDeck.PopCard());
      }
      mCardAnchor[i+6].SetHiddenCount(i);
    }

    while (!mDeck.Empty()) {
      mCardAnchor[0].AddCard(mDeck.PopCard());
    }

    if (mView.GetSettings().getBoolean("SolitaireStyleNormal", true)) {
      mDealsLeft = -1;
    } else {
      mDealsLeft = mDealThree ? 2 : 0;
      mLastScore = -52;
      mScoreString = "-$52";
      mCarryOverScore = 0;
    }
    mIgnoreEvents = false;
  }

  @Override
  public void SetCarryOverScore(int score) {
    mCarryOverScore = score;
  }

  @Override
  public void Resize(int width, int height) {
    int rem = width - Card.WIDTH*7;
    int maxHeight = height - (20 + Card.HEIGHT);
    rem /= 8;
    for (int i = 0; i < 7; i++) {
      mCardAnchor[i+6].SetPosition(rem + i * (rem+Card.WIDTH), 20 + Card.HEIGHT);
      mCardAnchor[i+6].SetMaxHeight(maxHeight);
    }

    for (int i = 3; i >= 0; i--) {
      mCardAnchor[i+2].SetPosition(rem + (6-i) * (rem + Card.WIDTH), 10);
    }

    for (int i = 0; i < 2; i++) {
      mCardAnchor[i].SetPosition(rem + i * (rem + Card.WIDTH), 10);
    }

    // Setup edge cards (Touch sensor loses sensitivity towards the edge).
    mCardAnchor[0].SetLeftEdge(0);
    mCardAnchor[2].SetRightEdge(width);
    mCardAnchor[6].SetLeftEdge(0);
    mCardAnchor[12].SetRightEdge(width);
    for (int i = 0; i < 7; i++) {
      mCardAnchor[i+6].SetBottom(height);
    }
  }

  @Override
  public void EventProcess(int event, CardAnchor anchor) {
    if (mIgnoreEvents) {
      return;
    }
    if (event == EVENT_DEAL) {
      if (mCardAnchor[0].GetCount() == 0) {
        boolean addDealCount = false;
        if (mDealsLeft == 0) {
          mCardAnchor[0].SetDone(true);
          return;
        } else if (mDealsLeft > 0) {
          mDealsLeft--;
          addDealCount = true;
        }
        int count = 0;
        while (mCardAnchor[1].GetCount() > 0) {
          mCardAnchor[0].AddCard(mCardAnchor[1].PopCard());
          count++;
        }
        mMoveHistory.push(new Move(1, 0, count, true, false, addDealCount));
        mView.Refresh();
      } else {
        int count = 0;
        int maxCount = mDealThree ? 3 : 1;
        for (int i = 0; i < maxCount && mCardAnchor[0].GetCount() > 0; i++) {
          mCardAnchor[1].AddCard(mCardAnchor[0].PopCard());
          count++;
        }
        if (mDealsLeft == 0 && mCardAnchor[0].GetCount() == 0) {
          mCardAnchor[0].SetDone(true);
        }
        mMoveHistory.push(new Move(0, 1, count, true, false));
      }
    } else if (event == EVENT_STACK_ADD) {
      if (mCardAnchor[2].GetCount() == 13 && mCardAnchor[3].GetCount() == 13 &&
          mCardAnchor[4].GetCount() == 13 && mCardAnchor[5].GetCount() == 13) {
        SignalWin();
      } else {
        if (mAutoMoveLevel == AUTO_MOVE_ALWAYS ||
            (mAutoMoveLevel == AUTO_MOVE_FLING_ONLY && mWasFling)) {
          EventAlert(EVENT_SMART_MOVE);
        } else {
          mView.StopAnimating();
          mWasFling = false;
        }
      }
    }
  }

  @Override
  public void EventProcess(int event, CardAnchor anchor, Card card) {
    if (mIgnoreEvents) {
      anchor.AddCard(card);
      return;
    }
    if (event == EVENT_FLING) {
      mWasFling = true;
      if (!TryToSinkCard(anchor, card)) {
        anchor.AddCard(card);
        mWasFling = false;
      }
    } else {
      anchor.AddCard(card);
    }
  }

  @Override
  public void EventProcess(int event) {
    if (mIgnoreEvents) {
      return;
    }
    if (event == EVENT_SMART_MOVE) {
      int i;
      for (i = 0; i < 7; i++) {
        if (mCardAnchor[i+6].GetCount() > 0 &&
            TryToSink(mCardAnchor[i+6])) {
          break;
        }
      }
      if (i == 7) {
        mWasFling = false;
        mView.StopAnimating();
      }
    }
  }

  @Override
  public boolean Fling(MoveCard moveCard) {
    if (moveCard.GetCount() == 1) {
      CardAnchor anchor = moveCard.GetAnchor();
      Card card = moveCard.DumpCards(false)[0];
      for (int i = 0; i < 4; i++) {
        if (mCardAnchor[i+2].DropSingleCard(card)) {
          EventAlert(EVENT_FLING, anchor, card);
          return true;
        }
      }
      anchor.AddCard(card);
    } else {
      moveCard.Release();
    }
    return false;
  }

  private boolean TryToSink(CardAnchor anchor) {
    Card card = anchor.PopCard();
    boolean ret = TryToSinkCard(anchor, card);
    if (!ret) {
      anchor.AddCard(card);
    }
    return ret;
  }

  private boolean TryToSinkCard(CardAnchor anchor, Card card) {
    for (int i = 0; i < 4; i++) {
      if (mCardAnchor[i+2].DropSingleCard(card)) {
        mMoveHistory.push(new Move(anchor.GetNumber(), i+2, 1, false, anchor.UnhideTopCard()));
        mAnimateCard.MoveCard(card, mCardAnchor[i+2]);
        return true;
      }
    }

    return false;
  }

  @Override
  public int GetRulesExtra() {
    return mDealsLeft;
  }

  @Override
  public String GetGameTypeString() {
    if (mDealsLeft == -1) {
      if (mDealThree) {
        return "SolitaireNormalDeal3";
      } else {
        return "SolitaireNormalDeal1";
      }
    } else {
      if (mDealThree) {
        return "SolitaireVegasDeal3";
      } else {
        return "SolitaireVegasDeal1";
      }
    }
  }
  @Override
  public String GetPrettyGameTypeString() {
    if (mDealsLeft == -1) {
      if (mDealThree) {
        return "Solitaire Dealing Three Cards";
      } else {
        return "Solitaire Dealing One Card";
      }
    } else {
      if (mDealThree) {
        return "Vegas Solitaire Dealing Three Cards";
      } else {
        return "Vegas Solitaire Dealing One Card";
      }
    }
  }

  @Override
  public boolean HasScore() {
    if (mDealsLeft != -1) {
      return true;
    }
    return false;
  }

  @Override
  public boolean HasString() {
    return HasScore();
  }

  @Override
  public String GetString() {
    if (mDealsLeft != -1) {
      int score = mCarryOverScore - 52;
      for (int i = 0; i < 4; i++) {
        score += 5 * mCardAnchor[i+2].GetCount();
      }
      if (score != mLastScore) {
        if (score < 0) {
          mScoreString = "-$" + (score * -1);
        } else {
          mScoreString = "$" + score;
        }
      }
      return mScoreString;
    }
    return "";
  }

  @Override
  public int GetScore() {
    if (mDealsLeft != -1) {
      int score = mCarryOverScore - 52;
      for (int i = 0; i < 4; i++) {
        score += 5 * mCardAnchor[i+2].GetCount();
      }
      return score;
    }
    return 0;
  }

  @Override
  public void AddDealCount() {
    if (mDealsLeft != -1) {
      mDealsLeft++;
      mCardAnchor[0].SetDone(false);
    }
  }
}

class Spider extends Rules {
  private boolean mStillDealing;
  public void Init(Bundle map) {
    mIgnoreEvents = true;
    mStillDealing = false;

    mCardCount = 104;
    mCardAnchorCount = 12;
    mCardAnchor = new CardAnchor[mCardAnchorCount];

    // Anchor stacks
    for (int i = 0; i < 10; i++) {
      mCardAnchor[i] = CardAnchor.CreateAnchor(CardAnchor.SPIDER_STACK, i, this);
      mCardAnchor[i] = CardAnchor.CreateAnchor(CardAnchor.GENERIC_ANCHOR, i, this);
      mCardAnchor[i].SetBuildSeq(GenericAnchor.SEQ_DSC);
      mCardAnchor[i].SetBuildSuit(GenericAnchor.SEQ_ANY);
      mCardAnchor[i].SetMoveSeq(GenericAnchor.SEQ_ASC);
      mCardAnchor[i].SetMoveSuit(GenericAnchor.SUIT_SAME);
      mCardAnchor[i].SetBehavior(GenericAnchor.PACK_MULTI);
      mCardAnchor[i].SetDisplay(GenericAnchor.DISPLAY_MIX);
      mCardAnchor[i].SetHack(GenericAnchor.DEALHACK);
    }

    mCardAnchor[10] = CardAnchor.CreateAnchor(CardAnchor.DEAL_FROM, 10, this);
    mCardAnchor[11] = CardAnchor.CreateAnchor(CardAnchor.DEAL_TO, 11, this);

    if (map != null) {
      // Do some assertions, default to a new game if we find an invalid state
      if (map.getInt("cardAnchorCount") == 12 &&
          map.getInt("cardCount") == 104) {
        int[] cardCount = map.getIntArray("anchorCardCount");
        int[] hiddenCount = map.getIntArray("anchorHiddenCount");
        int[] value = map.getIntArray("value");
        int[] suit = map.getIntArray("suit");
        int cardIdx = 0;

        for (int i = 0; i < mCardAnchorCount; i++) {
          for (int j = 0; j < cardCount[i]; j++, cardIdx++) {
            Card card = new Card(value[cardIdx], suit[cardIdx]);
            mCardAnchor[i].AddCard(card);
          }
          mCardAnchor[i].SetHiddenCount(hiddenCount[i]);
        }

        mIgnoreEvents = false;
        // Return here so an invalid save state will result in a new game
        return;
      }
    }

    int suits = mView.GetSettings().getInt("SpiderSuits", 4);
    mDeck = new Deck(2, suits);
    int i = 54;
    while (i > 0) {
      for (int j = 0; j < 10 && i > 0; j++) {
        i--;
        mCardAnchor[j].AddCard(mDeck.PopCard());
        mCardAnchor[j].SetHiddenCount(mCardAnchor[j].GetCount() - 1);
      }
    }

    while (!mDeck.Empty()) {
      mCardAnchor[10].AddCard(mDeck.PopCard());
    }
    mIgnoreEvents = false;
  }

  public void Resize(int width, int height) {
    int rem = (width - (Card.WIDTH * 10)) / 10;
    for (int i = 0; i < 10; i++) {
      mCardAnchor[i].SetPosition(rem/2 + i * (rem + Card.WIDTH), 10);
      mCardAnchor[i].SetMaxHeight(height-10);
    }
    // Setup edge cards (Touch sensor loses sensitivity towards the edge).
    mCardAnchor[0].SetLeftEdge(0);
    mCardAnchor[9].SetRightEdge(width);

    for (int i = 0; i < 10; i++) {
      mCardAnchor[i].SetBottom(height);
    }
    // These two are offscreen as the user doesn't need to see them, but they
    // are needed to hold onto out of play cards.
    mCardAnchor[10].SetPosition(-50, 1);
    mCardAnchor[11].SetPosition(-50, 1);
  }

  @Override
  public void EventProcess(int event) {
  }

  @Override
  public void EventProcess(int event, CardAnchor anchor, Card card) {
    anchor.AddCard(card);
  }

  @Override
  public void EventProcess(int event, CardAnchor anchor) {
    if (mIgnoreEvents) {
      return;
    }
    if (event == EVENT_STACK_ADD) {
      if (anchor.GetCount() - anchor.GetHiddenCount() >= 13) {
        Card[] card = anchor.GetCards();
        if (card[anchor.GetCount()-1].GetValue() == 1) {
          int suit = card[anchor.GetCount()-1].GetSuit();
          int val = 2;
          for (int i = anchor.GetCount() - 2; i >= 0 && val < 14; i--, val++) {
            if (card[i].GetValue() != val || card[i].GetSuit() != suit) {
              break;
            }
          }
          if (val == 14) {
            for (int j = 0; j < 13; j++) {
              mCardAnchor[11].AddCard(anchor.PopCard());
            }
            mMoveHistory.push(new Move(anchor.GetNumber(), 11, 13, true, anchor.UnhideTopCard()));

            if (mCardAnchor[11].GetCount() == mCardCount) {
              SignalWin();
            }
          }
        }
      }
      if (mStillDealing) {
        // Post another event if we aren't done yet.
        EventAlert(EVENT_DEAL_NEXT, mCardAnchor[anchor.GetNumber()+1]);
      }
    } else if (event == EVENT_DEAL) {
      if (mCardAnchor[10].GetCount() > 0) {
        int count = mCardAnchor[10].GetCount() > 10 ? 10 : mCardAnchor[10].GetCount();
        mAnimateCard.MoveCard(mCardAnchor[10].PopCard(), mCardAnchor[0]);
        mMoveHistory.push(new Move(10, 0, count-1, 1, false, false));
        mStillDealing = true;
      }
    } else if (event == EVENT_DEAL_NEXT) {
      if (mCardAnchor[10].GetCount() > 0 && anchor.GetNumber() < 10) {
        mAnimateCard.MoveCard(mCardAnchor[10].PopCard(), anchor);
      } else {
        mView.StopAnimating();
        mStillDealing = false;
      }
    }
  }

  @Override
  public String GetGameTypeString() {
    int suits = mView.GetSettings().getInt("SpiderSuits", 4);
    if (suits == 1) {
      return "Spider1Suit";
    } else if (suits == 2) {
      return "Spider2Suit";
    } else {
      return "Spider4Suit";
    }
  }
  @Override
  public String GetPrettyGameTypeString() {
    int suits = mView.GetSettings().getInt("SpiderSuits", 4);
    if (suits == 1) {
      return "Spider One Suit";
    } else if (suits == 2) {
      return "Spider Two Suit";
    } else {
      return "Spider Four Suit";
    }
  }

  @Override
  public boolean HasString() {
    return true;
  }

  @Override
  public String GetString() {
    int dealCount = mCardAnchor[10].GetCount() / 10;
    if (dealCount == 1) {
      return "1 deal left";
    }
    return dealCount + " deals left";
  }

}

class Freecell extends Rules {

  public void Init(Bundle map) {
    mIgnoreEvents = true;

    // Thirteen total anchors for regular solitaire
    mCardCount = 52;
    mCardAnchorCount = 16;
    mCardAnchor = new CardAnchor[mCardAnchorCount];

    // Top anchors for holding cards
    for (int i = 0; i < 4; i++) {
      mCardAnchor[i] = CardAnchor.CreateAnchor(CardAnchor.FREECELL_HOLD, i, this);
    }

    // Top anchors for sinking cards
    for (int i = 0; i < 4; i++) {
      mCardAnchor[i+4] = CardAnchor.CreateAnchor(CardAnchor.SEQ_SINK, i+4, this);
    }

    // Middle anchor stacks
    for (int i = 0; i < 8; i++) {
      mCardAnchor[i+8] = CardAnchor.CreateAnchor(CardAnchor.FREECELL_STACK, i+8,
                                                 this);
    }

    if (map != null) {
      // Do some assertions, default to a new game if we find an invalid state
      if (map.getInt("cardAnchorCount") == 16 &&
          map.getInt("cardCount") == 52) {
        int[] cardCount = map.getIntArray("anchorCardCount");
        int[] hiddenCount = map.getIntArray("anchorHiddenCount");
        int[] value = map.getIntArray("value");
        int[] suit = map.getIntArray("suit");
        int cardIdx = 0;

        for (int i = 0; i < 16; i++) {
          for (int j = 0; j < cardCount[i]; j++, cardIdx++) {
            Card card = new Card(value[cardIdx], suit[cardIdx]);
            mCardAnchor[i].AddCard(card);
          }
          mCardAnchor[i].SetHiddenCount(hiddenCount[i]);
        }

        mIgnoreEvents = false;
        // Return here so an invalid save state will result in a new game
        return;
      }
    }

    mDeck = new Deck(1);
    while (!mDeck.Empty()) {
      for (int i = 0; i < 8 && !mDeck.Empty(); i++) {
        mCardAnchor[i+8].AddCard(mDeck.PopCard());
      }
    }
    mIgnoreEvents = false;
  }

  public void Resize(int width, int height) {
    int rem = (width - (Card.WIDTH * 8)) / 8;
    for (int i = 0; i < 8; i++) {
      mCardAnchor[i].SetPosition(rem/2 + i * (rem + Card.WIDTH), 10);
      mCardAnchor[i+8].SetPosition(rem/2 + i * (rem + Card.WIDTH), 30 + Card.HEIGHT);
      mCardAnchor[i+8].SetMaxHeight(height - 30 - Card.HEIGHT);
    }

    // Setup edge cards (Touch sensor loses sensitivity towards the edge).
    mCardAnchor[0].SetLeftEdge(0);
    mCardAnchor[7].SetRightEdge(width);
    mCardAnchor[8].SetLeftEdge(0);
    mCardAnchor[15].SetRightEdge(width);
    for (int i = 0; i < 8; i++) {
      mCardAnchor[i+8].SetBottom(height);
    }
  }

  public void EventProcess(int event, CardAnchor anchor) {
    if (mIgnoreEvents) {
      return;
    }
    if (event == EVENT_STACK_ADD) {
      if (anchor.GetNumber() >= 4 && anchor.GetNumber() < 8) {
        if (mCardAnchor[4].GetCount() == 13 && mCardAnchor[5].GetCount() == 13 &&
            mCardAnchor[6].GetCount() == 13 && mCardAnchor[7].GetCount() == 13) {
          SignalWin();
        } else {
          if (mAutoMoveLevel == AUTO_MOVE_ALWAYS ||
              (mAutoMoveLevel == AUTO_MOVE_FLING_ONLY && mWasFling)) {
            EventAlert(EVENT_SMART_MOVE);
          } else {
            mView.StopAnimating();
            mWasFling = false;
          }
        }
      }
    }
  }

  @Override
  public boolean Fling(MoveCard moveCard) {
    if (moveCard.GetCount() == 1) {
      CardAnchor anchor = moveCard.GetAnchor();
      Card card = moveCard.DumpCards(false)[0];
      for (int i = 0; i < 4; i++) {
        if (mCardAnchor[i+4].DropSingleCard(card)) {
          EventAlert(EVENT_FLING, anchor, card);
          return true;
        }
      }
      anchor.AddCard(card);
    } else {
      moveCard.Release();
    }

    return false;
  }

  @Override
  public void EventProcess(int event, CardAnchor anchor, Card card) {
    if (mIgnoreEvents) {
      anchor.AddCard(card);
      return;
    }
    if (event == EVENT_FLING) {
      mWasFling = true;
      if (!TryToSinkCard(anchor, card)) {
        anchor.AddCard(card);
        mWasFling = false;
      }
    } else {
      anchor.AddCard(card);
    }
  }

  private boolean TryToSink(CardAnchor anchor) {
    Card card = anchor.PopCard();
    boolean ret = TryToSinkCard(anchor, card);
    if (!ret) {
      anchor.AddCard(card);
    }
    return ret;
  }

  private boolean TryToSinkCard(CardAnchor anchor, Card card) {
    for (int i = 0; i < 4; i++) {
      if (mCardAnchor[i+4].DropSingleCard(card)) {
        mAnimateCard.MoveCard(card, mCardAnchor[i+4]);
        mMoveHistory.push(new Move(anchor.GetNumber(), i+4, 1, false, false));
        return true;
      }
    }

    return false;
  }

  @Override
  public void EventProcess(int event) {
    if (mIgnoreEvents == true) {
      return;
    }
    if (event == EVENT_SMART_MOVE) {
      for (int i = 0; i < 4; i++) {
        if (mCardAnchor[i].GetCount() > 0 &&
            TryToSink(mCardAnchor[i])) {
          return;
        }
      }
      for (int i = 0; i < 8; i++) {
        if (mCardAnchor[i+8].GetCount() > 0 &&
            TryToSink(mCardAnchor[i+8])) {
          return;
        }
      }
      mWasFling = false;
      mView.StopAnimating();
    }
  }

  @Override
  public int CountFreeSpaces() {
    int free = 0;
    for (int i = 0; i < 4; i++) {
      if (mCardAnchor[i].GetCount() == 0) {
        free++;
      }
    }
    for (int i = 0; i < 8; i++) {
      if (mCardAnchor[i+8].GetCount() == 0) {
        free++;
      }
    }
    return free;
  }

  @Override
  public String GetGameTypeString() {
    return "Freecell";
  }
  @Override
  public String GetPrettyGameTypeString() {
    return "Freecell";
  }
}

class FortyThieves extends Rules {

  public void Init(Bundle map) {
    mIgnoreEvents = true;

    mCardCount = 104;
    mCardAnchorCount = 20;
    mCardAnchor = new CardAnchor[mCardAnchorCount];

    // Anchor stacks
    for (int i = 0; i < 10; i++) {
      mCardAnchor[i] = CardAnchor.CreateAnchor(CardAnchor.GENERIC_ANCHOR, i, this);
      mCardAnchor[i].SetBuildSeq(GenericAnchor.SEQ_DSC);
      mCardAnchor[i].SetMoveSeq(GenericAnchor.SEQ_ASC);
      mCardAnchor[i].SetSuit(GenericAnchor.SUIT_SAME);
      mCardAnchor[i].SetWrap(false);
      mCardAnchor[i].SetPickup(GenericAnchor.PACK_LIMIT_BY_FREE);
      mCardAnchor[i].SetDropoff(GenericAnchor.PACK_MULTI);
      mCardAnchor[i].SetDisplay(GenericAnchor.DISPLAY_ALL);
    }
    // Bottom anchors for holding cards
    for (int i = 0; i < 8; i++) {
      mCardAnchor[i+10] = CardAnchor.CreateAnchor(CardAnchor.SEQ_SINK, i+10, this);
    }
    
    mCardAnchor[18] = CardAnchor.CreateAnchor(CardAnchor.DEAL_FROM, 18, this);
    mCardAnchor[19] = CardAnchor.CreateAnchor(CardAnchor.DEAL_TO, 19, this);

    if (map != null) {
      // Do some assertions, default to a new game if we find an invalid state
      if (map.getInt("cardAnchorCount") == 20 &&
          map.getInt("cardCount") == 104) {
        int[] cardCount = map.getIntArray("anchorCardCount");
        int[] hiddenCount = map.getIntArray("anchorHiddenCount");
        int[] value = map.getIntArray("value");
        int[] suit = map.getIntArray("suit");
        int cardIdx = 0;

        for (int i = 0; i < 20; i++) {
          for (int j = 0; j < cardCount[i]; j++, cardIdx++) {
            Card card = new Card(value[cardIdx], suit[cardIdx]);
            mCardAnchor[i].AddCard(card);
          }
          mCardAnchor[i].SetHiddenCount(hiddenCount[i]);
        }

        mIgnoreEvents = false;
        // Return here so an invalid save state will result in a new game
        return;
      }
    }

    mDeck = new Deck(2);
    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 4; j++){
        mCardAnchor[i].AddCard(mDeck.PopCard());
      }
    }
    while (!mDeck.Empty()) {
      mCardAnchor[18].AddCard(mDeck.PopCard());
    }
    mIgnoreEvents = false;
  }

  public void Resize(int width, int height) {
    int rem = (width - (Card.WIDTH * 10)) / 10;
    for (int i = 0; i < 10; i++) {
      mCardAnchor[i].SetMaxHeight(height - 30 - Card.HEIGHT);
      mCardAnchor[i].SetPosition(rem/2 + i * (rem + Card.WIDTH), 30 + Card.HEIGHT);
      
      mCardAnchor[i+10].SetPosition(rem/2 + i * (rem + Card.WIDTH), 10);
    }

    // Setup edge cards (Touch sensor loses sensitivity towards the edge).
    mCardAnchor[0].SetLeftEdge(0);
    mCardAnchor[9].SetRightEdge(width);
    mCardAnchor[10].SetLeftEdge(0);
    mCardAnchor[19].SetRightEdge(width);
    for (int i = 0; i < 10; i++) {
      mCardAnchor[i].SetBottom(height);
    }
  }

  @Override
  public boolean Fling(MoveCard moveCard) {
    if (moveCard.GetCount() == 1) {
      CardAnchor anchor = moveCard.GetAnchor();
      Card card = moveCard.DumpCards(false)[0];
      for (int i = 0; i < 8; i++) {
        if (mCardAnchor[i+10].DropSingleCard(card)) {
          mEventPoster.PostEvent(EVENT_FLING, anchor, card);
          return true;
        }
      }
      anchor.AddCard(card);
    } else {
      moveCard.Release();
    }
    return false;
  }

  @Override
  public void EventProcess(int event, CardAnchor anchor, Card card) {
    if (mIgnoreEvents) {
      anchor.AddCard(card);
      return;
    }
    if (event == EVENT_FLING) {
      mWasFling = true;
      if (!TryToSinkCard(anchor, card)) {
        anchor.AddCard(card);
        mWasFling = false;
      }
    } else {
      anchor.AddCard(card);
    }
  }
  
  private boolean TryToSink(CardAnchor anchor) {
    Card card = anchor.PopCard();
    boolean ret = TryToSinkCard(anchor, card);
    if (!ret) {
      anchor.AddCard(card);
    }
    return ret;
  }

  private boolean TryToSinkCard(CardAnchor anchor, Card card) {
    for (int i = 0; i < 8; i++) {
      if (mCardAnchor[i+10].DropSingleCard(card)) {
        mAnimateCard.MoveCard(card, mCardAnchor[i+10]);
        mMoveHistory.push(new Move(anchor.GetNumber(), i+10, 1, false, false));
        return true;
      }
    }
    return false;
  }

  @Override
  public void EventProcess(int event, CardAnchor anchor) {
    if (mIgnoreEvents) {
      return;
    }
    if (event == EVENT_DEAL) {
      if (mCardAnchor[18].GetCount()>0){
        mCardAnchor[19].AddCard(mCardAnchor[18].PopCard());
        if (mCardAnchor[18].GetCount() == 0) {
          mCardAnchor[18].SetDone(true);
        }
        mMoveHistory.push(new Move(18, 19, 1, true, false));
      }
    } else if (event == EVENT_STACK_ADD) {
      if (anchor.GetNumber() >= 10 && anchor.GetNumber() < 18) {
        if (mCardAnchor[10].GetCount() == 13 && mCardAnchor[11].GetCount() == 13 &&
            mCardAnchor[12].GetCount() == 13 && mCardAnchor[13].GetCount() == 13 &&
            mCardAnchor[14].GetCount() == 13 && mCardAnchor[15].GetCount() == 13 &&
            mCardAnchor[16].GetCount() == 13 && mCardAnchor[17].GetCount() == 13) {
          SignalWin();
        } else {
          if (mAutoMoveLevel == AUTO_MOVE_ALWAYS ||
              (mAutoMoveLevel == AUTO_MOVE_FLING_ONLY && mWasFling)) {
            mEventPoster.PostEvent(EVENT_SMART_MOVE);
          } else {
            mView.StopAnimating();
            mWasFling = false;
          }
        }
      }
    }
  }
  
  @Override
  public void EventProcess(int event) {
    if (mIgnoreEvents == true) {
      return;
    }
    if (event == EVENT_SMART_MOVE) {
      for (int i = 0; i < 10; i++) {
        if (mCardAnchor[i].GetCount() > 0 &&
            TryToSink(mCardAnchor[i])) {
          return;
        }
      }
      mWasFling = false;
      mView.StopAnimating();
    }
  }

  @Override
  public int CountFreeSpaces() {
    int free = 0;
    for (int i = 0; i < 10; i++) {
      if (mCardAnchor[i].GetCount() == 0) {
        free++;
      }
    }
    return free;
  }

  @Override
  public String GetGameTypeString() {
    return "Forty Thieves";
  }
  @Override
  public String GetPrettyGameTypeString() {
    return "Forty Thieves";
  }
  
  @Override
  public boolean HasString() {
    return true;
  }

  @Override
  public String GetString() {
    int cardsLeft = mCardAnchor[18].GetCount();
    if (cardsLeft == 1) {
      return "1 card left";
    }
    return cardsLeft + " cards left";
  }
  
}


class EventPoster {
  private int mEvent;
  private CardAnchor mCardAnchor;
  private Card mCard;
  private Rules mRules;

  public EventPoster(Rules rules) {
    mRules = rules;
    mEvent = -1;
    mCardAnchor = null;
    mCard = null;
  }

  public void PostEvent(int event) {
    PostEvent(event, null, null);
  }

  public void PostEvent(int event, CardAnchor anchor) {
    PostEvent(event, anchor, null);
  }

  public void PostEvent(int event, CardAnchor anchor, Card card) {
    mEvent = event;
    mCardAnchor = anchor;
    mCard = card;
  }


  public void ClearEvent() {
    mEvent = Rules.EVENT_INVALID;
    mCardAnchor = null;
    mCard = null;
  }

  public boolean HasEvent() {
    return mEvent != Rules.EVENT_INVALID;
  }

  public void HandleEvent() {
    if (HasEvent()) {
      int event = mEvent;
      CardAnchor cardAnchor = mCardAnchor;
      Card card = mCard;
      ClearEvent();
      if (cardAnchor != null && card != null) {
        mRules.EventProcess(event, cardAnchor, card);
      } else if (cardAnchor != null) {
        mRules.EventProcess(event, cardAnchor);
      } else {
        mRules.EventProcess(event);
      }
    }
  }
}


