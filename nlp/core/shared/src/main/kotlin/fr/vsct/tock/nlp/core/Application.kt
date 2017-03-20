/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.nlp.core

import java.util.Locale

/**
 *
 */
data class Application(
        val name: String,
        val intents: List<Intent>,
        val supportedLocales: Set<Locale>) {

    private val intentMap: Map<String, Intent>

    init {
        intentMap = intents.associateBy { it.name }
    }

    fun getIntent(intentName: String): Intent = intentMap[intentName] ?: error("Intent $intentName does not exist in $name")

}