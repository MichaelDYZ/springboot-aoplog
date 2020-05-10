# springboot-aoplog
springboot 配置aop切面记录请求日志信息
该项目展示如何使用 aop 切面对请求进行日志记录，以及记录 UserAgent 的信息。
一、新建springboot配置pom文件如下：
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.7.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.dyz</groupId>
    <artifactId>aoplog</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>aoplog</name>
    <description>Demo project for Spring Boot</description>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>


        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>


        <!-- 引入AOP-->

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>

        <!-- 解析 UserAgent 信息 -->
        <!-- https://mvnrepository.com/artifact/eu.bitwalker/UserAgentUtils -->
        <dependency>
            <groupId>eu.bitwalker</groupId>
            <artifactId>UserAgentUtils</artifactId>
            <version>1.21</version>
        </dependency>


        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>5.3.3</version>
        </dependency>


        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>


        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>org.junit.vintage</groupId>
                    <artifactId>junit-vintage-engine</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
二、新建切面配置类AopLog,如下：
/**
 * @author dyz
 * @version 1.0
 * @date 2020/5/8 16:17
 *
 * 使用 aop 切面记录请求日志信息
 */
@Aspect
@Component
@Slf4j
public class AopLog {
    private static final String START_TIME = "request-start";

    /**
     * 设置切入点
     * 切入controller包下面的所有Controller
     */
    @Pointcut("execution(public * com.dyz.aoplog.controller.*Controller.*(..))")
    public void log() {

    }

    /**
     * 配置前置操作
     * @param point
     */
    @Before("log()")
    public void beforeLog(JoinPoint point) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        HttpServletRequest request = Objects.requireNonNull(attributes).getRequest();

        log.info("【请求 URL】：{}", request.getRequestURL());
        log.info("【请求 IP】：{}", request.getRemoteAddr());
        log.info("【请求类名】：{}，【请求方法名】：{}", point.getSignature().getDeclaringTypeName(), point.getSignature().getName());

        Map<String, String[]> parameterMap = request.getParameterMap();
        log.info("【请求参数】：{}，", JSONUtil.toJsonStr(parameterMap));
        Long start = System.currentTimeMillis();
        request.setAttribute(START_TIME, start);
    }

    /**
     * 配置环绕操作
     *
     * @param point 切入点
     * @return 原方法返回值
     * @throws Throwable 异常信息
     */
    @Around("log()")
    public Object aroundLog(ProceedingJoinPoint point) throws Throwable {
        Object result = point.proceed();
        log.info("【返回值】：{}", JSONUtil.toJsonStr(result));
        return result;
    }

    /**
     * 配置后置操作
     */
    @AfterReturning("log()")
    public void afterReturning() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = Objects.requireNonNull(attributes).getRequest();

        Long start = (Long) request.getAttribute(START_TIME);
        Long end = System.currentTimeMillis();
        log.info("【请求耗时】：{}毫秒", end - start);

        String header = request.getHeader("User-Agent");
        UserAgent userAgent = UserAgent.parseUserAgentString(header);
        log.info("【浏览器类型】：{}，【操作系统】：{}，【原始User-Agent】：{}", userAgent.getBrowser().toString(), userAgent.getOperatingSystem().toString(), header);
    }

}
三、新建Controller进行测试
/**
 * @author dyz
 * @version 1.0
 * @date 2020/5/8 16:42
 */
@RestController
public class Controller {

    /**
     * 切面日志测试
     * @param name
     * @return
     */
    @GetMapping("/test")
    public String test(String name) {
        return "访问者："+name;
    }
}
四、访问接口，并展示打印日志


