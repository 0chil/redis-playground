package chll.it.redisplayground

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RedisPlaygroundApplication

fun main(args: Array<String>) {
    runApplication<RedisPlaygroundApplication>(*args)
}
