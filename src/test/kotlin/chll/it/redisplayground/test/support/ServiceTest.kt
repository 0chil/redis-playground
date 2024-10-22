package chll.it.redisplayground.test.support

import org.springframework.boot.test.context.SpringBootTest

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
annotation class ServiceTest
