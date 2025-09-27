package com.bill_api_generator.bill_api_generator;

import org.springframework.boot.SpringApplication;

public class TestBillApiGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.from(BillApiGeneratorApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
