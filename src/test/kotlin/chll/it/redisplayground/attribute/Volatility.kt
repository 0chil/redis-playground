package chll.it.redisplayground.attribute

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import redis.embedded.RedisServer

class Volatility {

    private lateinit var redisTemplate: RedisTemplate<String, String>
    private lateinit var redisServer: RedisServer

    @BeforeEach
    fun setUp() {
        this.redisServer = RedisServer.newRedisServer()
            .port(6379)
            .setting("save")
            .build()
        redisServer.start()

        val connectionFactory = LettuceConnectionFactory("localhost", 6379).also { it.start() }
        this.redisTemplate = RedisTemplate<String, String>().also {
            it.connectionFactory = connectionFactory
            it.afterPropertiesSet()
        }
    }

    @AfterEach
    fun tearDown() {
        redisServer.stop()
    }

    @Test
    fun `Redis 서버를 재시작하면 데이터가 휘발된다`() {
        redisTemplate.opsForSet().add("key", "value")

        redisServer.stop()
        redisServer.start()

        assertThat(redisTemplate.opsForSet().members("key")).isEmpty()
    }
}