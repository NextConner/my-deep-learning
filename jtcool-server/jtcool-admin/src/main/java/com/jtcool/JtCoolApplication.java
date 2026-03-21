package com.jtcool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 * 
 * @author jtcool
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class JtCoolApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(JtCoolApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  JtCool启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                "  ________  _______   ___      ___             ________  ________  ________   \n" +
                "|\\   ___ \\|\\  ___ \\ |\\  \\    /  /|           |\\   __  \\|\\   __  \\|\\   __  \\  \n" +
                "\\ \\  \\_|\\ \\ \\   __/|\\ \\  \\  /  / /___________\\ \\  \\|\\  \\ \\  \\|\\  \\ \\  \\|\\  \\ \n" +
                " \\ \\  \\ \\\\ \\ \\  \\_|/_\\ \\  \\/  / /\\____________\\ \\   __  \\ \\   ____\\ \\   ____\\\n" +
                "  \\ \\  \\_\\\\ \\ \\  \\_|\\ \\ \\    / /\\|____________|\\ \\  \\ \\  \\ \\  \\___|\\ \\  \\___|\n" +
                "   \\ \\_______\\ \\_______\\ \\__/ /                 \\ \\__\\ \\__\\ \\__\\    \\ \\__\\   \n" +
                "    \\|_______|\\|_______|\\|__|/                   \\|__|\\|__|\\|__|     \\|__|   \n");
    }
}
