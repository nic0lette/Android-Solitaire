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

class Card {

    public static final int CLUBS = 0;
    public static final int DIAMONDS = 1;
    public static final int SPADES = 2;
    public static final int HEARTS = 3;

    public static final int ACE = 1;
    public static final int JACK = 11;
    public static final int QUEEN = 12;
    public static final int KING = 13;
    public static final String TEXT[] = {
            "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"
    };

    public static int WIDTH = 45;
    public static int HEIGHT = 64;

    private int mValue;
    private int mSuit;
    private float mX;
    private float mY;

    public static void SetSize(final int type) {
        if (type == Rules.SOLITAIRE) {
            WIDTH = 51;
            HEIGHT = 72;
        } else if (type == Rules.FREECELL) {
            WIDTH = 49;
            HEIGHT = 68;
        } else {
            WIDTH = 45;
            HEIGHT = 64;
        }
    }

    public Card(final int value, final int suit) {
        mValue = value;
        mSuit = suit;
        mX = 1;
        mY = 1;
    }

    public float GetX() {
        return mX;
    }

    public float getCenterX() {
        return mX + (WIDTH / 2);
    }

    public float getMoveY() {
        return mY + (HEIGHT * 2f / 3f);
    }

    public float GetY() {
        return mY;
    }

    public int GetValue() {
        return mValue;
    }

    public int GetSuit() {
        return mSuit;
    }

    public void SetPosition(final float x, final float y) {
        mX = x;
        mY = y;
    }

    public void MovePosition(final float dx, final float dy) {
        mX -= dx;
        mY -= dy;
    }
}
