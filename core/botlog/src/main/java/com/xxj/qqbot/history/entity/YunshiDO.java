package com.xxj.qqbot.history.entity;

import com.xxj.qqbot.history.basic.BaseDO;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "yunshi",
        indexes = {
                @Index(name = "uk_expire_time", columnList = "expire_time", unique = false),
        })
public class YunshiDO extends BaseDO {

    @Id
    @Column(length = 20,nullable = false,updatable = false)
    Long id;

    @Column(length = 16,nullable = false,updatable = false)
    Long qqId;

    @Column(length = 64,nullable = false,updatable = false,name = "expire_time")
    Date expireTime;

    @Column(length = 64,nullable = false,updatable = false)
    String imageId;

    @Column(length = 8,nullable = false,updatable = false)
    String lucky;

    @Column(length = 32,nullable = false,updatable = false)
    String context;

    @Column(length = 32,nullable = true,updatable = false)
    String nameCard;

    @Column(length = 16,nullable = false,updatable = false)
    Long groupId;

    @Column(length = 32,nullable = false,updatable = false)
    String groupName;

    @Tolerate
    public YunshiDO(){};
}
