package com.bill_api_generator.bill_api_generator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class BillApiGeneratorApplicationTests {

	@Test
	void contextLoads() {
	}

}
