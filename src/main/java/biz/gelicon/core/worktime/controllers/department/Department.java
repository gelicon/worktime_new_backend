package biz.gelicon.core.worktime.controllers.department;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Отдел - модель
 */
@Table(name = "department")
public class Department {

    @Id
    @Column(name = "department_id")
    private Integer departmentId;

    @NotNull(message = "Имя отдела не может быть пустым")
    @Size(max = 50, message = "Имя отдела должно содержать не более {max} символов")
    @Column(name = "department_name")
    private String departmentName;

    @NotNull(message = "Статус отдела не может быть пустым")
    @Column(name = "department_status")
    private Integer departmentStatus;

    @Schema(description = "Наименование статуса: 1-Действующий, 0-Закрытый")
    private String departmentStatusName;

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public void setDepartmentStatus(Integer departmentStatus) {
        this.departmentStatus = departmentStatus;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public Integer getDepartmentStatus() {
        return departmentStatus;
    }

    public String getDepartmentStatusName() {
        return departmentStatus == 1 ? "Действующий" : "Закрытый";
    }

    public Department(Integer departmentId, String departmentName, Integer departmentStatus) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.departmentStatus = departmentStatus;
    }

    public Department() {
        this.departmentStatus = 1;
    }
}
