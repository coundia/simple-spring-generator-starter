package com.pcoundia.model.entity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import com.pcoundia.view.View;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Setter
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    // @GeneratedValue(strategy= GenerationType.AUTO)
    @ToString.Include
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @JsonView({View.Public.class})
    protected Long id;

    @CreatedDate
//    @LastModifiedDate
    // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
    @JsonView({View.ReadOnlyDetail.class})
    protected Date createdAt;
    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
    @JsonView({View.ReadOnlyDetail.class})
    protected Date updatedAt;
    @JsonView({View.ReadOnlyDetail.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
    protected Date deletedAt;

    @JsonView({View.ReadOnly.class})
    protected boolean status = true;
    @JsonView({View.ReadOnlyDetail.class})
    protected boolean deleted = false;

    @JsonView({View.ReadOnlyDetail.class})
    @CreatedBy
    protected Long createdById;
    @JsonView({View.ReadOnlyDetail.class})
    @LastModifiedBy
    protected Long updatedById;
    @JsonView({View.ReadOnlyDetail.class})
    protected Long deletedById;

}
