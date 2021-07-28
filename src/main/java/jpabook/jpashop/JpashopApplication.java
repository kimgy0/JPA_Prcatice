package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}

	@Bean
	public Hibernate5Module hibernate5Module(){
		Hibernate5Module hibernateModule = new Hibernate5Module();
//		hibernateModule.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);
		//걍 제이슨 생성할때 레이지로딩 무시하고 다 끌고오고 싶으면 configure로 세팅.
		return hibernateModule;
	}

}
