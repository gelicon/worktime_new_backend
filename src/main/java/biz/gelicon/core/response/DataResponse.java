package biz.gelicon.core.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Формат вывода страницы с результатом")
public class DataResponse<T> {
    @Schema(description = "Список записей")
    private List<T> result;

    @Schema(description = "Всего доступных элементов")
    private int allRowCount;

    @Schema(description = "Номер текущей страницы")
    private int currPage;

    @Schema(description = "Всего страниц")
    private int allPage;

    public DataResponse() { }

    public DataResponse(List<T> result, int allRowCount, int currPage, int allPage) {
        this.result = result;
        this.allRowCount = allRowCount;
        this.currPage = currPage;
        this.allPage = allPage;
    }

    @Schema(description = "Количество элементов на странице")
    public int getRowCount(){return result != null ? result.size() : 0;}

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

    public int getAllRowCount() {
        return allRowCount;
    }

    public void setAllRowCount(int allRowCount) {
        this.allRowCount = allRowCount;
    }

    public int getCurrPage() {
        return currPage;
    }

    public void setCurrPage(int currPage) {
        this.currPage = currPage;
    }

    public int getAllPage() {
        return allPage;
    }

    public void setAllPage(int allPage) {
        this.allPage = allPage;
    }
}
