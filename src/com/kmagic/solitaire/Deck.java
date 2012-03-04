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

import java.util.Random;


public class Deck {

  private Card[] mCard;
  private int mCardCount;
  private int mTotalCards;

  public Deck(int decks) {
    Init(decks, 4);
  }

  public Deck(int decks, int suits) {
    if (suits == 2) {
      decks *= 2;
    } else if (suits == 1) {
      decks *= 4;
    }
    Init(decks, suits);
  }

  private void Init(int decks, int suits) {
    mCardCount = decks * 13 * suits;
    mTotalCards = mCardCount;
    mCard = new Card[mCardCount];
    for (int deck = 0; deck < decks; deck++) {
      for (int suit = Card.CLUBS; suit < suits; suit++) {
        for (int value = 0; value < 13; value++) {
          mCard[deck*suits*13 + suit*Card.KING + value] = new Card(value+1, suit);
        }
      }
    }

    Shuffle();
    Shuffle();
    Shuffle();
  }

  public void PushCard(Card card) {
    mCard[mCardCount++] = card;
  }

  public Card PopCard() {
    if (mCardCount > 0) {
      return mCard[--mCardCount];
    }
    return null;
  }

  public boolean Empty() {
    return mCardCount == 0;
  }

  public void Shuffle() {
    int lastIdx = mCardCount - 1;
    int swapIdx;
    Card swapCard;
    Random rand = new Random();

    while (lastIdx > 1) {
      swapIdx = rand.nextInt(lastIdx);
      swapCard = mCard[swapIdx];
      mCard[swapIdx] = mCard[lastIdx];
      mCard[lastIdx] = swapCard;
      lastIdx--;
    }
  }
}
