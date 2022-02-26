package biz.gelicon.core.view;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "Представление узла дерева")
public class TreeViewItem {

    @Schema(description = "Идентификатор узла дерева")
    private Integer value;

    @Schema(description = "Альтернативное свойство для идентификатора узла дерева")
    private Integer key;

    @Schema(description = "Наименование узла дерева")
    private String title;

    @Schema(description = "Дочерние узлы")
    private List<TreeViewItem> children;

    @Schema(description = "Идентификатор родительского узла")
    private Integer parentId;

    public TreeViewItem() {
        this.children = new ArrayList<>();
    }

    public TreeViewItem(Integer value, String title,Integer parentId) {
        this.value = value;
        this.key = value;
        this.title = title;
        this.parentId =parentId;
        this.children = new ArrayList<>();
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<TreeViewItem> getChildren() {
        return children;
    }

    public void setChildren(List<TreeViewItem> children) {
        this.children = children;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }
}
