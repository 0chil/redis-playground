package chll.it.redisplayground.datastructure.set

import chll.it.redisplayground.test.support.ServiceTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.RedisTemplate

@ServiceTest
class SetCommandsTest {

    private val redisTemplate: RedisTemplate<String, String> = RedisTemplate()

    @Test
    fun `Set에 동일한 값은 한 번만 삽입 가능하다`() {
        repeat(2) {
            redisTemplate.opsForSet().add("key", "value")
        }

        val values = redisTemplate.opsForSet().members("key")

        assertThat(values).containsOnlyOnce("value")
    }
}