package com.codebycs.monitor.rubinotxp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class RubinotXpMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(RubinotXpMonitorApplication.class, args);
	}

}
