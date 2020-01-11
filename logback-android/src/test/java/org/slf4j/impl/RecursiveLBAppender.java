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
package org.slf4j.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.testUtil.RandomUtil;

public class RecursiveLBAppender extends AppenderBase<ILoggingEvent> {

  public List<ILoggingEvent> list = new ArrayList<ILoggingEvent>();
  public List<String> stringList = new ArrayList<String>();
  
  PatternLayout layout;
  
  public RecursiveLBAppender() {
    this(null);
  }
  
  public RecursiveLBAppender(PatternLayout layout) {
    this.layout = layout;
  }
  
  @Override
  public void start() {
    int diff = RandomUtil.getPositiveInt();
    Logger logger = LoggerFactory.getLogger("ResursiveLBAppender"+diff);
    logger.info("testing");
    super.start();
  }
  
  protected void append(ILoggingEvent e) {
    list.add(e);
    if(layout != null) {
      String s = layout.doLayout(e);
      stringList.add(s);
    }
  }
}
