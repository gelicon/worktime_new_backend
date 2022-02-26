package biz.gelicon.core.controllers;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Hidden
@Tag(name="Корневой контроллер", description="Корневой контроллер")
@Controller
public class MainController {
    @Value("${gelicon.appName}")
    private String appName;

    @RequestMapping(value = "/")
    public @ResponseBody String main() {
        return "<html>" +
                "<body>" +
                "<h1>" +appName+" API </h1>"+
                "<p><a href='/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config'>Документация</a><p>"+
                "</body>" +
                "</html>";
    }



}