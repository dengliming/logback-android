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
package com.github.tony19.logback.dsl

import ch.qos.logback.classic.Logger
import com.github.tony19.logback.Logback
import io.kotlintest.specs.FreeSpec

class LogbackTest: FreeSpec({
    fun logback(block: Logger.() -> Unit): Logback {
        return Logback(Configuration {
            debug(true)
            root {
                block()
            }
        })
    }

    "logs events to logcat" {
        logback { logcatAppender() }.logger.info("hello world")
    }

    "logs events to file" {
        logback { fileAppender() }.logger.info("hello world")
    }

    "logs events to rolling files" {
        logback { rollingFileAppender() }.logger.info("hello world")
    }
})