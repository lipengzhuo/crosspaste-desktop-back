package com.clipevery.net

import com.clipevery.config.ConfigManager
import com.clipevery.exception.StandardErrorCode
import com.clipevery.net.exception.signalExceptionHandler
import com.clipevery.net.plugin.SignalDecryption
import com.clipevery.routing.syncRouting
import com.clipevery.serializer.IdentityKeySerializer
import com.clipevery.serializer.PreKeyBundleSerializer
import com.clipevery.utils.failResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.contentType
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.serializersModuleOf
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.state.PreKeyBundle
import java.net.BindException

class DesktopClipServer(private val configManager: ConfigManager,
                        private val clientHandlerManager :ClientHandlerManager): ClipServer {

    private val logger = KotlinLogging.logger {}

    private var port = 0

    private var server: NettyApplicationEngine = createServer(port = configManager.config.port)

    private fun createServer(port: Int): NettyApplicationEngine {
        return embeddedServer(Netty, port = port) {
            install(ContentNegotiation) {
                json(Json {
                    serializersModule = SerializersModule {
                        serializersModuleOf(PreKeyBundle::class, PreKeyBundleSerializer)
                        serializersModuleOf(IdentityKey::class, IdentityKeySerializer)
                    }
                })
            }
            install(StatusPages) {
                exception(Exception::class) { call, cause ->
                    logger.error(cause) { "Unhandled exception" }
                    failResponse(call, StandardErrorCode.UNKNOWN_ERROR.toErrorCode())
                }
                signalExceptionHandler()
            }
            install(SignalDecryption) {

            }
            intercept(ApplicationCallPipeline.Setup) {
                logger.info {"Received request: ${call.request.httpMethod.value} ${call.request.uri} ${call.request.contentType()}" }
            }
            routing {
                syncRouting()
            }
        }
    }

    override fun start(): ClipServer {
        clientHandlerManager.start()
        try {
            server.start(wait = false)
        } catch (e: BindException) {
            logger.warn { "Port ${configManager.config.port} is already in use" }
            server = createServer(port = 0)
            server.start(wait = false)
        }
        port = runBlocking { server.resolvedConnectors().first().port }
        if (port != configManager.config.port) {
            configManager.updateConfig {
                it.copy(port = port)
            }
        }
        logger.info { "Server started at port $port" }
        return this
    }

    override fun stop() {
        server.stop()
    }

    override fun port(): Int {
        return port
    }
}
