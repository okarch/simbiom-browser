package com.emd.simbiom.portlet;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zkoss.zk.au.http.DHtmlUpdateServlet;
import org.zkoss.zk.ui.http.DHtmlLayoutServlet;
import org.zkoss.zk.ui.http.HttpSessionListener;

import com.emd.simbiom.portlet.InventoryCleanup;

@Configuration
@ComponentScan("com.emd.simbiom.portlet")
@EnableAutoConfiguration
public class SampleInventoryApplication {

    public static void main(String[] args) {
	SpringApplication.run(SampleInventoryApplication.class, args);
    }

    /*
     * plain URL...
     */
    @RequestMapping("/")
    @ResponseBody
    String home() {
	return "hi!";
    }

    /*
     * Servlet listeners
     */
    @Bean
    public ServletListenerRegistrationBean httpSessionListener() {
	HttpSessionListener sListener = new HttpSessionListener();
	ServletListenerRegistrationBean reg = new ServletListenerRegistrationBean( sListener );
	return reg;
    }

    @Bean
    public ServletListenerRegistrationBean inventoryCleanupListener() {
	InventoryCleanup sListener = new InventoryCleanup();
	ServletListenerRegistrationBean reg = new ServletListenerRegistrationBean( sListener );
	return reg;
    }

	/*
	 * ZK servlets
	 */
	@Bean
	public ServletRegistrationBean dHtmlLayoutServlet() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("update-uri", "/zkau");
		DHtmlLayoutServlet dHtmlLayoutServlet = new DHtmlLayoutServlet();
		ServletRegistrationBean reg = new ServletRegistrationBean(dHtmlLayoutServlet, "*.zul");
		reg.setLoadOnStartup(1);
		reg.setInitParameters(params);
		return reg;
	}

	@Bean
	public ServletRegistrationBean dHtmlUpdateServlet() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("update-uri", "/zkau/*");
		ServletRegistrationBean reg = new ServletRegistrationBean(new DHtmlUpdateServlet(), "/zkau/*");
		reg.setLoadOnStartup(2);
		reg.setInitParameters(params);
		return reg;
	}

}
