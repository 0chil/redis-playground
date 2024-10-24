package chll.it.redisplayground.redistemplate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import redis.embedded.RedisServer
import java.io.Serializable
import java.time.Duration
import java.time.Instant

class SerializationStudyTest {

    private lateinit var redisServer: RedisServer
    private lateinit var redisTemplate: RedisTemplate<String, Any>

    @BeforeEach
    fun setUp() {
        this.redisServer = RedisServer.newRedisServer()
            .setting("save")
            .build().also { it.start() }
        this.redisTemplate = RedisTemplate<String, Any>()
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.connectionFactory = LettuceConnectionFactory().also { it.start() }
        redisTemplate.afterPropertiesSet()
    }

    @AfterEach
    fun tearDown() {
        redisServer.stop()
    }

    class TestObject(val value: String) : Serializable

    @Test
    fun `객체를 바이너리로 직렬화 해 저장한다`() {
        val original = TestObject("somevalue")
        redisTemplate.valueSerializer = JdkSerializationRedisSerializer()
        redisTemplate.opsForList().leftPush("key", original)

        val savedByte = redisTemplate.execute {
            it.listCommands().lPop("key".toByteArray())
        }?.decodeToString()

        assertThat(savedByte).isEqualTo(
            "��\u0000\u0005sr\u0000;chll.it.redisplayground.saving.SerializationTest\$TestObject͓�uޱ�a\u0002\u0000\u0001L\u0000\u0005valuet\u0000\u0012Ljava/lang/String;xpt\u0000\tsomevalue"
        )
    }


    @Test
    fun `객체를 JSON으로 직렬화 해 저장한다`() {
        val original = TestObject("somevalue")
        redisTemplate.valueSerializer = GenericJackson2JsonRedisSerializer()
        redisTemplate.opsForList().leftPush("key2", original)

        val savedByte = redisTemplate.execute {
            it.listCommands().lPop("key2".toByteArray())
        }?.decodeToString()

        assertThat(savedByte).isEqualTo(
            "{\"@class\":\"chll.it.redisplayground.saving.SerializationTest\$TestObject\",\"value\":\"somevalue\"}"
        )
    }

    /**
     * 직렬화/역직렬화 과정이 사라지는 것은 아니기 때문이다!
     * 단지 직렬화한 결과가 ByteArray 일 뿐이다!
     */
    @Test
    fun `바이너리로 직렬화 한 경우에도 객체는 결국 달라진다`() {
        val original = TestObject("somevalue")
        redisTemplate.valueSerializer = JdkSerializationRedisSerializer()
        redisTemplate.opsForSet().add("key", original)

        val deserialized = redisTemplate.opsForSet().pop("key")

        assertThat(deserialized).isNotEqualTo(original)
    }

    @RepeatedTest(10)
    fun `바이너리 직렬화가 JSON 직렬화보다 빠르다`() {
        val original = TestObject("somevalue")
        val jsonSerialization = Runnable {
            redisTemplate.valueSerializer = GenericJackson2JsonRedisSerializer()
            redisTemplate.opsForSet().add("key", original)
        }
        val byteArraySerialization = Runnable {
            redisTemplate.valueSerializer = JdkSerializationRedisSerializer()
            redisTemplate.opsForSet().add("key", original)
        }
        jsonSerialization.run();byteArraySerialization.run() // Warming-up

        assertThat(durationOf(jsonSerialization)).isGreaterThan(durationOf(byteArraySerialization))
    }

    @Test
    fun `10000번을 동기적으로 저장하더라도 직렬화 속도의 차이는 무시할 수 있는 수준이다`() {
        val original = TestObject("somevalue")
        val jsonSerialization = Runnable {
            redisTemplate.valueSerializer = GenericJackson2JsonRedisSerializer()
            redisTemplate.opsForSet().add("key", original)
        }
        val byteArraySerialization = Runnable {
            redisTemplate.valueSerializer = JdkSerializationRedisSerializer()
            redisTemplate.opsForSet().add("key", original)
        }
        jsonSerialization.run();byteArraySerialization.run() // Warming-up

        val jsonSerializationDuration = durationOf {
            repeat(10000) { jsonSerialization.run() }
        }
        val byteArraySerializationDuration = durationOf {
            repeat(10000) { byteArraySerialization.run() }
        }

        assertThat(jsonSerializationDuration).isCloseTo(byteArraySerializationDuration, Duration.ofSeconds(1))
    }

    private fun durationOf(runnable: Runnable): Duration {
        val start = Instant.now()
        runnable.run()
        val end = Instant.now()
        return Duration.between(start, end)
    }
}
