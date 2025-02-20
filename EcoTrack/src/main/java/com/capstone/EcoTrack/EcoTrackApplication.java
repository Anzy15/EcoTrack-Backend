package com.capstone.EcoTrack;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@ComponentScan(basePackages = {"com.capstone.EcoTrack"})
@SpringBootApplication
public class EcoTrackApplication {

	public static void main(String[] args){
	
		SpringApplication.run(EcoTrackApplication.class, args);
	}

}
