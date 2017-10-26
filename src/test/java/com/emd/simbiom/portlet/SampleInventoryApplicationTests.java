package com.emd.simbiom.portlet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.emd.simbiom.portlet.SampleInventoryApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SampleInventoryApplication.class)
@WebAppConfiguration
public class SampleInventoryApplicationTests {

	@Test
	public void contextLoads() {
	}

}
