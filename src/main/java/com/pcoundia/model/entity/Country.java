package com.pcoundia.model.entity;

import com.pcoundia.view.CountryView;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "country")
public class Country extends BaseEntity implements Serializable{



    @JsonView({ CountryView.CyRead.class, CountryView.CyWrite.class })
	private String code ;



    @JsonView({ CountryView.CyRead.class, CountryView.CyWrite.class })
	private String name ;


	private String alpha2 ;

}
