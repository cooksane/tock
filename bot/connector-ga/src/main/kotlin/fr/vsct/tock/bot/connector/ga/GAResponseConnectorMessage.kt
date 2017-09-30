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

package fr.vsct.tock.bot.connector.ga

import com.fasterxml.jackson.annotation.JsonIgnore
import fr.vsct.tock.bot.connector.ConnectorMessage
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.connector.ga.model.response.GACustomPushMessage
import fr.vsct.tock.bot.connector.ga.model.response.GAExpectedInput
import fr.vsct.tock.bot.connector.ga.model.response.GAFinalResponse
import fr.vsct.tock.bot.engine.message.SentenceElement

/**
 *
 */
data class GAResponseConnectorMessage(
        val expectUserResponse: Boolean = true,
        val expectedInputs: List<GAExpectedInput> = emptyList(),
        val finalResponse: GAFinalResponse? = null,
        val customPushMessage: GACustomPushMessage? = null) : ConnectorMessage {

    constructor(input: GAExpectedInput) : this(expectedInputs = listOf(input))

    @Transient
    val expectedInput: GAExpectedInput? = expectedInputs.firstOrNull()

    override val connectorType: ConnectorType @JsonIgnore get() = gaConnectorType

    override fun toSentenceElement(): SentenceElement?
            = expectedInput?.toSentenceElement() ?: finalResponse?.toSentenceElement()

}