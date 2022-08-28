package com.lagou.edu.controller;

import com.lagou.edu.pojo.QueryVo;
import com.lagou.edu.pojo.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * @author Jaa
 * @date 2022/1/11 22:44
 */
@Controller
@RequestMapping("/demo")
public class DemoController {

    // SpringMVC的异常处理机制（异常处理器）
    // 注意：写在这里只会对当前controller类生效
    /*@ExceptionHandler(ArithmeticException.class)
    public void handlerException(ArithmeticException exception, HttpServletResponse response) {
        // 异常处理逻辑
        try {
            response.getWriter().write(exception.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /**
     * http://localhost:8080/demo/handle01
     */
    @RequestMapping("/handle01")
    public ModelAndView handle01(@ModelAttribute("name") String name) {

        int i = 1 / 0;

        Date date = new Date();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("date", date);
        modelAndView.setViewName("success");
        return modelAndView;
    }

    /**
     * SpringMVC在handler方法中传入参数ModelMap、Model、Map，并向这些参数中保存数据，都可以在页面上获取到
     *
     * 它们之间有什么关系？？？
     * 运行时的具体类型都是BindingAwareModelMap，相当于给BindingAwareModelMap保存数据都会放在请求域中
     *  Map(jdk中的接口)  Model(Spring框架的接口)
     *  ModelMap(Spring框架的接口, 实现Map接口)
     *
     *      BindingAwareModelMap extends ExtendedModelMap
     *      ExtendedModelMap extends ModelMap implements Model
     */

    /**
     * 直接声明形参 ModelMap, 封装数据
     * http://localhost:8080/demo/handle11
     * =====modelMap: class org.springframework.validation.support.BindingAwareModelMap
     */
    @RequestMapping("/handle11")
    public String handle11(ModelMap modelMap) {
        Date date = new Date();
        modelMap.addAttribute("date", date);
        System.out.println("=====modelMap: " + modelMap.getClass());
        return "success";
    }

    /**
     * 直接声明形参 Model, 封装数据
     * http://localhost:8080/demo/handle12
     * =====model: class org.springframework.validation.support.BindingAwareModelMap
     */
    @RequestMapping("/handle12")
    public String handle12(Model model) {
        Date date = new Date();
        model.addAttribute("date", date);
        System.out.println("=====model: " + model.getClass());
        return "success";
    }

    /**
     * 直接声明形参Map, 封装数据
     * http://localhost:8080/demo/handle13
     * =====map: class org.springframework.validation.support.BindingAwareModelMap
     */
    @RequestMapping("/handle13")
    public String handle13(Map map) {
        Date date = new Date();
        map.put("date", date);
        System.out.println("=====map: " + map.getClass());
        return "success";
    }


    /**
     *
     * SpringMVC 对原生servlet api的支持  url：/demo/handle02?id=1
     *
     * 如果要在SpringMVC中使用servlet原生对象，比如HttpServletRequest\HttpServletResponse\HttpSession，直接在Handler方法形参中声明使用即可
     *
     */
    @RequestMapping("/handle02")
    public ModelAndView handle02(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String id = request.getParameter("id");
        Date date = new Date();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("date",date);
        modelAndView.setViewName("success");
        return modelAndView;
    }


    /*
     * SpringMVC 接收简单数据类型参数  url：/demo/handle03?id=1
     *
     * 注意：接收简单数据类型参数，直接在handler方法的形参中声明即可，框架会取出参数值然后绑定到对应参数上
     * 要求：传递的参数名和声明的形参名称保持一致
     */
    @RequestMapping("/handle03")
    public ModelAndView handle03(@RequestParam("ids") Integer id, Boolean flag) {
        Date date = new Date();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("date",date);
        modelAndView.setViewName("success");
        return modelAndView;
    }


    /*
     * SpringMVC接收pojo类型参数  url：/demo/handle04?id=1&username=zhangsan
     *
     * 接收pojo类型参数，直接形参声明即可，类型就是Pojo的类型，形参名无所谓
     * 但是要求传递的参数名必须和Pojo的属性名保持一致
     */
    @RequestMapping("/handle04")
    public ModelAndView handle04(User user) {
        Date date = new Date();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("date", date);
        modelAndView.setViewName("success");
        return modelAndView;
    }


    /*
     * SpringMVC接收pojo包装类型参数  url：/demo/handle05?user.id=1&user.username=zhangsan
     * 不管包装Pojo与否，它首先是一个pojo，那么就可以按照上述pojo的要求来
     * 1、绑定时候直接形参声明即可
     * 2、传参参数名和pojo属性保持一致，如果不能够定位数据项，那么通过属性名 + "." 的方式进一步锁定数据
     *
     */
    @RequestMapping("/handle05")
    public ModelAndView handle05(QueryVo queryVo) {
        Date date = new Date();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("date", date);
        modelAndView.setViewName("success");
        return modelAndView;
    }


    /**
     * 绑定日期类型参数
     * 定义一个SpringMVC的类型转换器  接口，扩展实现接口接口，注册你的实现
     * @param birthday
     * @return
     */
    @RequestMapping("/handle06")
    public ModelAndView handle06(Date birthday) {
        Date date = new Date();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("date",date);
        modelAndView.setViewName("success");
        return modelAndView;
    }


    /**
     * restful  get   /demo/handle/15
     */
    @RequestMapping(value = "/handle/{id}",method = {RequestMethod.GET})
    public ModelAndView handleGet(@PathVariable("id") Integer id) {
        Date date = new Date();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("date",date);
        modelAndView.setViewName("success");
        return modelAndView;
    }

    /**
     * restful  post  /demo/handle
     */
    @RequestMapping(value = "/handle",method = {RequestMethod.POST})
    public ModelAndView handlePost(String username) {
        Date date = new Date();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("date",date);
        modelAndView.setViewName("success");
        return modelAndView;
    }

    /**
     * restful  put  /demo/handle/15/lisi
     */
    @RequestMapping(value = "/handle/{id}/{name}",method = {RequestMethod.PUT})
    public ModelAndView handlePut(@PathVariable("id") Integer id, @PathVariable("name") String username) {
        Date date = new Date();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("date",date);
        modelAndView.setViewName("success");
        return modelAndView;
    }


    /*
     * restful  delete  /demo/handle/15
     */
    @RequestMapping(value = "/handle/{id}",method = {RequestMethod.DELETE})
    public ModelAndView handleDelete(@PathVariable("id") Integer id) {
        Date date = new Date();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("date",date);
        modelAndView.setViewName("success");
        return modelAndView;
    }

    @RequestMapping("/handle07")
    // 添加@ResponseBody之后，不再走视图解析器那个流程，而是等同于response直接输出数据
    @ResponseBody
    public User handle07(@RequestBody User user) {
        // 业务逻辑处理，修改name为张三丰
        user.setName("张三丰");
        return user;
    }

    /**
     * 文件上传
     * @param uploadFile
     * @return
     */
    @RequestMapping("/upload")
    public ModelAndView upload(MultipartFile uploadFile, HttpSession session) throws IOException {
        // 处理上传文件
        // 重命名, 原名 123.jpg 获取后缀
        String originalFilename = uploadFile.getOriginalFilename(); // 原始名称
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1, originalFilename.length());
        String newName = UUID.randomUUID().toString() + "." + ext;

        // 存储到指定的文件夹, /uploads, 考虑文件过多的情况按照日期, 生成一个文件夹
        String realPath = session.getServletContext().getRealPath("/upload");
        String datePath = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File folder = new File(realPath + "/" + datePath);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        // 存储文件到目录
        uploadFile.transferTo(new File(folder, newName));

        // TODO 文件磁盘路径要更新到数据库字段

        Date date = new Date();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("date", date);
        modelAndView.setViewName("success");
        return modelAndView;
    }

    /**
     * SpringMVC 重定向时参数传递的问题
     * 转发：A 找 B 借钱400，B没有钱但是悄悄的找到C借了400块钱给A
     *      url不会变,参数也不会丢失,一个请求
     * 重定向：A 找 B 借钱400，B 说我没有钱，你找别人借去，那么A 又带着400块的借钱需求找到C
     *      url会变,参数会丢失需要重新携带参数,两个请求
     */
    @RequestMapping("/handlerRedirect")
    public String handlerRedirect(String name, RedirectAttributes redirectAttributes) {

        // return "redirect:handle01?name=" + name; // 拼接参数安全性、参数长度都有局限

        // addFlashAttribute方法设置了一个flash类型属性，该属性会暂存到session中，在跳转到页面之后该属性会销毁
        redirectAttributes.addFlashAttribute("name", name);
        return "redirect:handle01";
    }

}
