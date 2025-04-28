package com.notification.config.email;


import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import freemarker.template.Configuration;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class MailDataProvider {
	@Autowired
	private  MailSubjectConfiguration mailSubjectConfig;
	@Autowired
	private Configuration freemarkerConfig;





	public String getMailBody(Map model, String prefix) throws IOException, TemplateException {

		String templateName = getTemplateName(prefix);
		System.out.println("templateName = " + templateName);
		Template template = freemarkerConfig.getTemplate(templateName);
		return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
	}

	private String getTemplateName(String prefix) {
		String result = String.join("-", prefix);
		return result + ".ftl";
	}


	public String getMailSubjectCommon(Integer mailType) {
		return mailSubjectConfig.getRegistrationEmailEnglishSubject();
	}

}
