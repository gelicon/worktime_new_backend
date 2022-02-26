package biz.gelicon.core.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Table(name = "progusergroup")
public class Progusergroup {
    // группа для всех пользователей
    public final static int EVERYONE = 1;


    @Id
    @Column(name = "progusergroup_id")
    private Integer progusergroupId;

    @NotNull(message = "Имя группы не может быть пустым")
    @Size(max = 30, message = "Имя группы должно содержать не более {max} символов")
    @Column(name = "progusergroup_name")
    private String progusergroupName;

    @Size(max = 255, message = "Описание группы должно содержать не более {max} символов")
    @Column(name = "progusergroup_note")
    private String progusergroupNote;

    @NotNull(message = "Видимость группы не может быть пустой")
    @Column(name = "progusergroup_visible")
    private Integer progusergroupVisible;

    public Progusergroup() {
        progusergroupVisible = 1;
    }

    public Progusergroup(Integer progUserGroupId, String progUserGroupName, String progUserGroupNote, Integer progUserGroupVisible) {
        this.progusergroupId = progUserGroupId;
        this.progusergroupName = progUserGroupName;
        this.progusergroupNote = progUserGroupNote;
        this.progusergroupVisible = progUserGroupVisible;
    }

    public Integer getProgusergroupId() {
        return progusergroupId;
    }

    public void setProgusergroupId(Integer progusergroupId) {
        this.progusergroupId = progusergroupId;
    }

    public String getProgusergroupName() {
        return progusergroupName;
    }

    public void setProgusergroupName(String progusergroupName) {
        this.progusergroupName = progusergroupName;
    }

    public String getProgusergroupNote() {
        return progusergroupNote;
    }

    public void setProgusergroupNote(String progusergroupNote) {
        this.progusergroupNote = progusergroupNote;
    }

    public Integer getProgusergroupVisible() {
        return progusergroupVisible;
    }

    public void setProgusergroupVisible(Integer progusergroupVisible) {
        this.progusergroupVisible = progusergroupVisible;
    }
}
