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

package fr.vsct.tock.bot.engine

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.vertx.vertx
import fr.vsct.tock.bot.connector.Connector
import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.UserLock
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.bot.engine.user.UserTimelineDAO
import io.vertx.ext.web.Router
import mu.KotlinLogging

/**
 *
 */
class ConnectorController(
        private val bot: Bot,
        private val connector: Connector,
        private val router: Router) {

    companion object {

        private val logger = KotlinLogging.logger {}

        internal fun register(connector: Connector,
                              bot: Bot,
                              router: Router) {
            logger.info { "Register connector $connector with " }
            connector.register(ConnectorController(bot, connector, router), router)
        }
    }


    private val userLock: UserLock by injector.instance()
    private val userTimelineDAO: UserTimelineDAO by injector.instance()

    internal val connectorType: ConnectorType get() = connector.connectorType

    fun handle(action: Action) {
        val playerId = action.playerId
        val id = playerId.id

        if (userLock.lock(id)) {
            try {
                val userTimeline = userTimelineDAO.loadWithLastValidDialog(action.playerId, { bot.botDefinition.findStoryDefinition(it) })
                bot.handle(action, userTimeline, this)
                userTimelineDAO.save(userTimeline)
            } catch(t: Throwable) {
                logger.error(t.message, t)
                send(bot.errorActionFor(action))
            } finally {
                userLock.releaseLock(id)
            }
        } else {
            logger.debug { "$playerId locked - skip action $action" }
        }
    }

    fun send(action: Action, delay: Long = 0) {
        try {
            if (delay == 0L) {
                sendAsynchronous(action)
            } else {
                vertx.setTimer(delay, {
                    sendAsynchronous(action)
                })
            }
        } catch(t: Throwable) {
            logger.error(t.message, t)
        }
    }

    fun errorMessage(playerId: PlayerId, applicationId: String, recipientId: PlayerId): Action {
        return bot.botDefinition.errorAction(playerId, applicationId, recipientId)
    }

    private fun sendAsynchronous(action: Action) {
        vertx.executeBlocking<Void>({
            try {
                logger.debug { "message sent: $action" }
                connector.send(action)
            } catch(t: Throwable) {
                logger.error(t.message, t)
            } finally {
                it.complete()
            }
        }, false, {})
    }

    internal fun loadProfile(applicationId: String, playerId: PlayerId): UserPreferences {
        return connector.loadProfile(applicationId, playerId)
    }

    internal fun startTypingAnswer(action: Action) {
        connector.startTypingAnswer(action)
    }
}