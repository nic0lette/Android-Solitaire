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

import android.util.Log;
import java.util.Stack;

public class Replay implements Runnable {
  private Stack<Move> mMoveStack;
  private SolitaireView mView;
  private AnimateCard mAnimateCard;
  private CardAnchor[] mCardAnchor;
  private boolean mIsPlaying;

  private Card[] mSinkCard;
  private int mSinkCount;
  private int mEventCount;
  private CardAnchor mSinkAnchor;
  private CardAnchor mSinkFrom;
  private boolean mSinkUnhide;

  public Replay(SolitaireView view, AnimateCard animateCard) {
    mView = view;
    mAnimateCard = animateCard;
    mIsPlaying = false;
    mMoveStack = new Stack<Move>();
    mSinkCard = new Card[104];
  }

  public boolean IsPlaying() { return mIsPlaying; }
  public void StopPlaying() { mIsPlaying = false; }

  public void StartReplay(Stack<Move> history, CardAnchor[] anchor) {
    mCardAnchor = anchor;
    mMoveStack.clear();
    while (!history.empty()) {
      Move move = history.peek();
      if (move.GetToBegin() != move.GetToEnd()) {
        for (int i = move.GetToEnd(); i >= move.GetToBegin(); i--) {
          mMoveStack.push(new Move(move.GetFrom(), i, 1, false, false));
        }
      } else {
        mMoveStack.push(move);
      }
      mView.Undo();
    }
    mView.DrawBoard();
    mIsPlaying = true;
    PlayNext();
  }

  public void PlayNext() {
    if (!mIsPlaying || mMoveStack.empty()) {
      mIsPlaying = false;
      mView.StopAnimating();
      return;
    }
    Move move = mMoveStack.pop();

    if (move.GetToBegin() == move.GetToEnd()) {
      mSinkCount = move.GetCount();
      mEventCount = 0;
      mSinkAnchor = mCardAnchor[move.GetToBegin()];
      mSinkUnhide = move.GetUnhide();
      mSinkFrom = mCardAnchor[move.GetFrom()];

      if (move.GetInvert()) {
        for (int i = 0; i < mSinkCount; i++) {
          mSinkCard[i] = mSinkFrom.PopCard();
        }
      } else {
        for (int i = mSinkCount-1; i >= 0; i--) {
          mSinkCard[i] = mSinkFrom.PopCard();
        }
      }
      mAnimateCard.MoveCards(mSinkCard, mSinkAnchor, mSinkCount, this);
    } else {
      Log.e("Replay.java", "Invalid move encountered, aborting.");
      mIsPlaying = false;
    }
  }

  public void run() {
    if (mIsPlaying) {
      if (mSinkUnhide) {
        mSinkFrom.UnhideTopCard();
      }
      PlayNext();
    }
  }
}
