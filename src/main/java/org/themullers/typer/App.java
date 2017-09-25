package org.themullers.typer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@EnableAutoConfiguration
public class App 
{

    @GetMapping("/")
    String home(Model model) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        model.addAttribute("numLines", 10);
        model.addAttribute("userFullName", user.getUsername());
        return "type"; // the name of the template to use
    }
    
    @GetMapping("/login")
    String login() {
        return "login";
    }
    
    public static void main( String[] args )
    {
        Object[] beans = {
                App.class, Service.class, JdbcDao.class, TextPreparationService.class, WebSecurityConfig.class
                }; 
        
        SpringApplication.run(beans, args);
    }
}
