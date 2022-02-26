package biz.gelicon.core.model;

import biz.gelicon.core.annotations.TableDescription;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Table(
    name = "sqlaction",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sqlaction_sql"})
    }
)
@TableDescription("Действие")
public class Sqlaction {
    public static final int TYPE_EXE_APP = 0;
    public static final int TYPE_USER_APP = 1;
    public static final int TYPE_WEB_APP = 2;
    public static final int TYPE_GELICON_CORE_APP = 3;

    @Id
    @Column(name = "sqlaction_id",nullable = false)
    public Integer sqlactionId;

    @Column(name = "sqlaction_sql", nullable = false)
    @Size(max = 10, message = "Поле 'Действие' должно содержать не более {max} символов")
    @NotBlank(message = "Поле 'Действие' не может быть пустым")
    private String sqlactionSql;

    @Column(name = "sqlaction_note", nullable = true)
    @Size(max = 20, message = "Поле 'Примечание' должно содержать не более {max} символов")
    private String sqlactionNote;

    public Integer getsqlactionId() {
        return sqlactionId;
    }

    public void setsqlactionId(Integer value) {
        this.sqlactionId = value;
    }

    public String getsqlactionSql() {
        return sqlactionSql;
    }

    public void setsqlactionSql(String value) {
        this.sqlactionSql = value;
    }

    public String getsqlactionNote() {
        return sqlactionNote;
    }

    public void setsqlactionNote(String value) {
        this.sqlactionNote = value;
    }

    public Sqlaction() {}

    public Sqlaction(
            Integer sqlactionId,
            String sqlactionSql,
            String sqlactionNote) {
        this.sqlactionId = sqlactionId;
        this.sqlactionSql = sqlactionSql;
        this.sqlactionNote = sqlactionNote;
    }
}

