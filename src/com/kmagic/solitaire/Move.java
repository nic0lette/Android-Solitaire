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


public class Move {
  private int mFrom;
  private int mToBegin;
  private int mToEnd;
  private int mCount;
  private int mFlags;

  private static final int FLAGS_INVERT = 0x0001;
  private static final int FLAGS_UNHIDE = 0x0002;
  private static final int FLAGS_ADD_DEAL_COUNT = 0x0004;

  public Move() {
    mFrom = -1;
    mToBegin = -1;
    mToEnd = -1;
    mCount = 0;
    mFlags = 0;
  }
  public Move(Move move) {
    mFrom = move.mFrom;
    mToBegin = move.mToBegin;
    mToEnd = move.mToEnd;
    mCount = move.mCount;
    mFlags = move.mFlags;
  }
  public Move(int from, int toBegin, int toEnd, int count, boolean invert,
              boolean unhide) {
    mFrom = from;
    mToBegin = toBegin;
    mToEnd = toEnd;
    mCount = count;
    mFlags = 0;
    if (invert)
      mFlags |= FLAGS_INVERT;
    if (unhide)
      mFlags |= FLAGS_UNHIDE;
  }

  public Move(int from, int to, int count, boolean invert,
              boolean unhide) {
    mFrom = from;
    mToBegin = to;
    mToEnd = to;
    mCount = count;
    mFlags = 0;
    if (invert)
      mFlags |= FLAGS_INVERT;
    if (unhide)
      mFlags |= FLAGS_UNHIDE;
  }

  public Move(int from, int to, int count, boolean invert,
              boolean unhide, boolean addDealCount) {
    mFrom = from;
    mToBegin = to;
    mToEnd = to;
    mCount = count;
    mFlags = 0;
    if (invert)
      mFlags |= FLAGS_INVERT;
    if (unhide)
      mFlags |= FLAGS_UNHIDE;
    if (addDealCount)
      mFlags |= FLAGS_ADD_DEAL_COUNT;
  }

  public Move(int from, int toBegin, int toEnd, int count, int flags) {
    mFrom = from;
    mToBegin = toBegin;
    mToEnd = toEnd;
    mCount = count;
    mFlags = flags;
  }

  public int GetFrom() { return mFrom; }
  public int GetToBegin() { return mToBegin; }
  public int GetToEnd() { return mToEnd; }
  public int GetCount() { return mCount; }
  public int GetFlags() { return mFlags; }
  public boolean GetInvert() { return (mFlags & FLAGS_INVERT) != 0; }
  public boolean GetUnhide() { return (mFlags & FLAGS_UNHIDE) != 0; }
  public boolean GetAddDealCount() { return (mFlags & FLAGS_ADD_DEAL_COUNT) != 0; } 
}
