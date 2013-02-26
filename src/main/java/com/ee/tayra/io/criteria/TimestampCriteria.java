/*******************************************************************************
 * Copyright (c) 2013, Equal Experts Ltd
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation
 * are those of the authors and should not be interpreted as representing
 * official policies, either expressed or implied, of the Tayra Project.
 ******************************************************************************/
package com.ee.tayra.io.criteria;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimestampCriteria implements Criterion {

  private static final String TS_IDENTIFIER = "$ts:";
  private static final String INC_IDENTIFIER = "$inc:";
  private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  private static final long MILLI_CONVERSION = 1000L;
  private final Date timeStampUntil;
  private final int increment;
  private final boolean toExclude;

  public TimestampCriteria(final String filter, final boolean toExclude) {
    this.toExclude = toExclude;
    this.timeStampUntil = getTimestampFrom(filter);
    this.increment = getIncrementFrom(filter);
  }

  @Override
  public boolean isSatisfiedBy(final String document) {
    if (toExclude) {
      return !criteriaSatisfied(document);
    }
    return criteriaSatisfied(document);
  }

  private boolean criteriaSatisfied(final String document) {
    String tsDocument = document.replaceAll("\"", "")
      .replaceAll(" ", "");
    if (timeStampUntil.compareTo(getTimestampFrom(tsDocument)) > 0) {
      return true;
    }
    if (timeStampUntil.compareTo(getTimestampFrom(tsDocument)) == 0) {
      return increment >= getIncrementFrom(tsDocument);
    }
    return false;
  }

  private int getIncrementFrom(final String filter) {
    if (filter.contains(INC_IDENTIFIER)) {
      int incStartIndex = filter.indexOf(INC_IDENTIFIER)
              + INC_IDENTIFIER.length();
      int incEndIndex = filter.indexOf("}", incStartIndex);
      return Integer.parseInt(filter
              .substring(incStartIndex, incEndIndex).trim());
     }
    return Integer.MAX_VALUE;
  }

  private Date getTimestampFrom(final String filter) {
   if (filter.contains(TS_IDENTIFIER)) {
     int tsStartIndex = filter.indexOf(TS_IDENTIFIER) + TS_IDENTIFIER.length();
     int tsEndIndex = filter.indexOf(INC_IDENTIFIER);
     return new Date(Long.parseLong(filter.substring(tsStartIndex, tsEndIndex)
                              .replaceAll(",", "").trim()) * MILLI_CONVERSION);
   } else {
     try {
       SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
       return format.parse(filter.substring(filter.indexOf("=") + 1));
     } catch (ParseException p) {
       return new Date(0L);
     }
   }
  }

}
