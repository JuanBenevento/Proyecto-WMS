package com.juanbenevento.wms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class WmsApplicationTests {

	@Test
	@WithMockUser
	void contextLoads() {
	}

}
