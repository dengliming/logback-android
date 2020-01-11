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
package com.github.tony19.logback.xml.data

import com.gitlab.mvysny.konsumexml.Konsumer

data class Logger (
        var name: String,
        var level: String? = null,
        var appenderRefs: List<AppenderRef>,
        var additivity: Boolean?
) {
    companion object {
        fun xml(k: Konsumer): Logger {
            k.checkCurrent("logger")

            return Logger(
                    name = k.attributes.getValue("name"),
                    level = k.attributes.getValueOpt("level"),
                    additivity = k.attributes.getValueOpt("additivity")?.toBoolean(),
                    appenderRefs = k.children("appender-ref") { AppenderRef.xml(this) }
            )
        }
    }
}