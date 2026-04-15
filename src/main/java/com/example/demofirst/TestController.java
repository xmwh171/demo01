package com.example.demofirst;

import com.example.demofirst.constant.ErrorCodeEnum;
import com.example.demofirst.zk.ZkDistributedLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ObjectInputFilter;
import java.util.Arrays;
import java.util.List;

@RestController
public class TestController {

    @Autowired
    private ZkDistributedLock zkLock;

    // 改造原有hello接口，返回统一格式
    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("复工第2天！统一返回体生效啦 ✅");
    }

    // 新增测试接口：返回失败示例
    @GetMapping("/testError")
    public Result<String> testError() {
        return Result.error(ErrorCodeEnum.PARAM_ERROR, "参数错误，请检查！");
    }

    // 新增：测试全局异常捕获
    @GetMapping("/testException")
    public Result<String> testException() {
        // 故意写空指针异常，测试全局异常处理
        String str = null;
        str.length(); // 这里会抛NullPointerException
        return Result.success("不会执行到这里");
    }

    @GetMapping("/testList")
    public Result<List<String>> testList() {
        List<String> list = Arrays.asList("Java", "SpringBoot", "MySQL");

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ObjectInputFilter.Config.class);
        context.getBean("");
        return Result.success(list);
    }

//    @GetMapping("/testLock")
//    public void testLock() {
//        zkLock.doLock("user_order_100", () -> {
//            System.out.println("拿到分布式锁，执行业务");
//        });
//    }


}
