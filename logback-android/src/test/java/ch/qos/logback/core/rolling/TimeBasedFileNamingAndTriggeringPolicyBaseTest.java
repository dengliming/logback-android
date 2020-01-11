/*
 * Copyright (c) 2020 Anthony Trinh.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.qos.logback.core.rolling;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusChecker;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class TimeBasedFileNamingAndTriggeringPolicyBaseTest {

  static long MILLIS_IN_MINUTE = 60*1000;
  static long MILLIS_IN_HOUR = 60*MILLIS_IN_MINUTE;

  Context context = new ContextBase();
  RollingFileAppender<Object> rfa = new RollingFileAppender<Object>();
  TimeBasedRollingPolicy<Object> tbrp = new TimeBasedRollingPolicy<Object>();
  DefaultTimeBasedFileNamingAndTriggeringPolicy<Object> timeBasedFNATP = new DefaultTimeBasedFileNamingAndTriggeringPolicy<Object>();

  @Before
  public void setUp() {
    rfa.setContext(context);
    tbrp.setContext(context);
    timeBasedFNATP.setContext(context);

    rfa.setRollingPolicy(tbrp);
    tbrp.setParent(rfa);
    tbrp.setTimeBasedFileNamingAndTriggeringPolicy(timeBasedFNATP);
    timeBasedFNATP.setTimeBasedRollingPolicy(tbrp);
  }

  @Test
  public void singleDate() {
    // Tuesday December 20th 17:59:01 CET 2011
    long startTime = 1324400341553L;
    tbrp.setFileNamePattern("foo-%d{yyyy-MM'T'mm}.log");
    tbrp.start();

    timeBasedFNATP.setCurrentTime(startTime);
    timeBasedFNATP.start();

    timeBasedFNATP.setCurrentTime(startTime+MILLIS_IN_MINUTE);
    boolean triggerred = timeBasedFNATP.isTriggeringEvent(null, null);
    assertTrue(triggerred);
    String elapsedPeriodsFileName = timeBasedFNATP.getElapsedPeriodsFileName();
    assertEquals("foo-2011-12T59.log", elapsedPeriodsFileName);
  }

  // see "log rollover should be configurable using %d multiple times in file name pattern"
  // http://jira.qos.ch/browse/LBCORE-242

  @Test
  public void multiDate() {
    // Tuesday December 20th 17:59:01 CET 2011
    long startTime = 1324400341553L;
    tbrp.setFileNamePattern("foo-%d{yyyy-MM, AUX}/%d{mm}.log");
    tbrp.start();

    timeBasedFNATP.setCurrentTime(startTime);
    timeBasedFNATP.start();

    timeBasedFNATP.setCurrentTime(startTime+MILLIS_IN_MINUTE);
    timeBasedFNATP.isTriggeringEvent(null, null);
    String elapsedPeriodsFileName = timeBasedFNATP.getElapsedPeriodsFileName();
    assertEquals("foo-2011-12/59.log", elapsedPeriodsFileName);
  }

  @Test
  public void withTimeZone() {
    // Tuesday December 20th 17:59:01 CET 2011
    long startTime = 1324400341553L;
    tbrp.setFileNamePattern("foo-%d{yyyy-MM-dd, GMT+5}.log");
    tbrp.start();

    timeBasedFNATP.setCurrentTime(startTime);
    timeBasedFNATP.start();

    timeBasedFNATP.setCurrentTime(startTime + MILLIS_IN_MINUTE + 2 * MILLIS_IN_HOUR);
    boolean triggerred = timeBasedFNATP.isTriggeringEvent(null, null);
    assertTrue(triggerred);
    String elapsedPeriodsFileName = timeBasedFNATP.getElapsedPeriodsFileName();
    assertEquals("foo-2011-12-20.log", elapsedPeriodsFileName);
  }

  @Test
  public void extraIntegerTokenInFileNamePatternShouldBeDetected() {
    String pattern = "test-%d{yyyy-MM-dd'T'HH}-%i.log.zip";
    tbrp.setFileNamePattern(pattern);
    tbrp.start();

    assertFalse(tbrp.isStarted());
    StatusChecker statusChecker = new StatusChecker(context);
    statusChecker.assertContainsMatch(Status.ERROR, "Filename pattern .{37} contains an integer token converter");
  }
}
