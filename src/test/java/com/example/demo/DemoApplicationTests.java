package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTests {

	@Test
	void contextLoads() {
		int a = 0;
		int b = 0;
		a = a++;
		a = a++;
		System.out.println(a);
		System.out.println(b);
	}

}
