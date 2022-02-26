package biz.gelicon.core.model;

import biz.gelicon.core.annotations.TableDescription;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

/* Сущность сгенерирована 28.04.2021 11:57 */
@Table(
    name = "proguserchannel",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"proguser_id", "channelnotification_id"})
    }
)
@TableDescription("Канал оповещения пользователя")
public class ProguserChannel {

    @Id
    @Column(name = "proguserchannel_id",nullable = false)
    public Integer proguserChannelId;

    @ManyToOne(targetEntity = Proguser.class)
    @Column(name = "proguser_id", nullable = false)
    private Integer proguserId;

    @ManyToOne(targetEntity = CapCode.class)
    @Column(name = "channelnotification_id", nullable = false)
    private Integer channelNotificationId;

    @Column(name = "proguserchannel_address", nullable = true)
    @Size(max = 125, message = "Поле \"Адрес абонента\" должно содержать не более {max} символов")
    private String proguserChannelAddress;

    public Integer getProguserChannelId() {
        return proguserChannelId;
    }

    public void setProguserChannelId(Integer value) {
        this.proguserChannelId = value;
    }

    public Integer getProguserId() {
        return proguserId;
    }

    public void setProguserId(Integer value) {
        this.proguserId = value;
    }

    public Integer getChannelNotificationId() {
        return channelNotificationId;
    }

    public void setChannelNotificationId(Integer value) {
        this.channelNotificationId = value;
    }

    public String getProguserChannelAddress() {
        return proguserChannelAddress;
    }

    public void setProguserChannelAddress(String value) {
        this.proguserChannelAddress = value;
    }


    public ProguserChannel() {}

    public ProguserChannel(
            Integer proguserChannelId,
            Integer proguserId,
            Integer channelNotificationId,
            String proguserChannelAddress) {
        this.proguserChannelId = proguserChannelId;
        this.proguserId = proguserId;
        this.channelNotificationId = channelNotificationId;
        this.proguserChannelAddress = proguserChannelAddress;
    }
}

