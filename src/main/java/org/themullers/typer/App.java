package org.themullers.typer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@EnableAutoConfiguration
public class App 
{

    @GetMapping("/")
    String home(Model model) {
        model.addAttribute("numLines", 10);
        return "typer"; // the name of the template to use
    }
    
    public static void main( String[] args )
    {
        SpringApplication.run(new Object[]{App.class, Service.class, JdbcDao.class, TextPreparationService.class}, args);
    }
}
