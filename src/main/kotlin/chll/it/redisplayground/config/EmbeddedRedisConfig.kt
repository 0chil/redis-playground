package chll.it.redisplayground.config

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import redis.embedded.RedisServer

@Profile("local", "test")
@Configuration
class EmbeddedRedisConfig(
    @Value("\${spring.redis.port}") port: Int = 6379
) {
    private val redisServer = RedisServer(port)

    @PostConstruct
    fun startRedis() {
        redisServer.start()
    }

    @PreDestroy
    fun stopRedis() {
        redisServer.stop()
    }
}