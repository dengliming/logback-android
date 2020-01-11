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
package com.github.tony19.logback.xml

import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import com.github.tony19.logback.utils.capitalized
import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.anyName
import java.lang.reflect.Method
import java.nio.charset.Charset
import java.util.*

class XmlDeserializer(val onValue: (Any) -> Any = { it }): IDeserializer {
    override fun <T> deserialize(k: Konsumer, className: String): T {
        @Suppress("UNCHECKED_CAST")
        return deserialize(k, create(Class.forName(className))) as T
    }

    override fun deserialize(k: Konsumer, inst: Any): Any {
        return inst.apply {
            val instMethods by lazy { inst.javaClass.methods }
            k.children(anyName) {
                if (name?.localPart?.isNotEmpty()!!) {
                    val setterMethod = findSetterMethod(instMethods, name?.localPart)
                    if (setterMethod == null) {
                        skipContents()
                        println("warning: setter method not found: \"set${name!!.localPart.capitalized()}\" or \"add${name!!.localPart.capitalized()}\"")

                    // Arrays require an adder method to insert values!
                    // (we don't support array initialization)
                    } else if (setterMethod.parameterTypes[0].isArray && setterMethod.name.startsWith("set")) {
                        skipContents()
                        println("warning: adder method not found: \"add${name!!.localPart.capitalized()}\"")

                    } else {
                        val value = resolveValue(this, setterMethod.parameterTypes[0])
                        setterMethod.invoke(inst, value)
                    }
                }
            }
        }
    }

    private fun findSetterMethod(instMethods: Array<Method>, elemLocalPartName: String?): Method? {
        val elemName = elemLocalPartName?.toLowerCase(Locale.US)
        val find: (String) -> Method? = { prefix ->
            instMethods.find {
                it.name.toLowerCase(Locale.US) == "${prefix}${elemName}"
                && it.parameterTypes.size == 1
            }
        }
        // Prioritize adder in case a setter exists for an array. The adder
        // adds a single param, which works with the flow in resolveValue().
        return find("add") ?: find("set")
    }

    private fun resolveValue(k: Konsumer, paramType: Class<*>): Any {
        return when {
            paramType == java.lang.String::class.java -> onValue(k.text())
            paramType == java.nio.charset.Charset::class.java -> k.text { Charset.forName(onValue(it) as String) }
            paramType.isPrimitive -> k.text { parsePrimitive(paramType, onValue(it) as String)!! }
            else -> {
                val className = k.attributes.getValueOpt("class")
                val param = getParamClass(className, paramType)

                onValue(deserialize(k, create(param)))
            }
        }
    }

    private fun create(clazz: Class<*>): Any = try {
        clazz.getDeclaredConstructor().newInstance()
    } catch(e: NoSuchMethodException) {
        println("warning: missing default constructor for \"${clazz.name}\"")
        throw e
    }

    private val stringConverters by lazy {
        mapOf(
                "string" to String::toString,
                "byte" to String::toByte,
                "int" to String::toInt,
                "short" to String::toShort,
                "long" to String::toLong,
                "float" to String::toFloat,
                "double" to String::toDouble,
                "boolean" to String::toBoolean,
                "biginteger" to String::toBigInteger,
                "bigdecimal" to String::toBigDecimal
        )
    }

    private fun parsePrimitive(paramType: Class<*>, rawValue: String): Any? {
        return stringConverters[paramType.name.toLowerCase(Locale.US)]?.invoke(rawValue)
    }

    private fun getParamClass(className: String?, paramType: Class<*>): Class<*> {
        if (paramType.isInterface && className.isNullOrEmpty()) {
            return when (paramType) {
                ch.qos.logback.core.encoder.Encoder::class.java -> PatternLayoutEncoder::class.java
                ch.qos.logback.core.Layout::class.java -> PatternLayout::class.java
                else -> throw Error("warning: cannot instantiate interface: ${paramType.name}")
            }
        }
        return when {
            !className.isNullOrEmpty() -> Class.forName(className)
            else -> paramType
        }
    }
}