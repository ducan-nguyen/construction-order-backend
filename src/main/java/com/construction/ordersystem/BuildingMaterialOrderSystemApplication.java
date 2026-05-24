package com.construction.ordersystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BuildingMaterialOrderSystemApplication {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(BuildingMaterialOrderSystemApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(BuildingMaterialOrderSystemApplication.class, args);
	}

}
