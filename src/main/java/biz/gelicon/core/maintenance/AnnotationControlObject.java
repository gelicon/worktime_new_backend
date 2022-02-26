package biz.gelicon.core.maintenance;

/**
 * Вспомогательный класс для описания аннотированного метода
 */
class AnnotationControlObject {

    String controllerClassName; // Имя класса
    String controllerTagName; // name из аннотации Tag - русское наименование контроллера
    String controllerRequestMappingValue; // value из аннотации RequestMapping - урл для контроллера
    String methodName; // Имя метода
    String methodOperationSummary; // summary из аннотации к методу Operation - русское наименование метода
    String methodOperationDescription; // description из аннотации к методу Operation - длинное описание
    String methodRequestMappingValue; // // value из аннотации RequestMapping - урл для метода
    String controlObjectUrl; // полный урл с версией и пр.
    boolean needToSave; // необходимость создавать запись в ControlObject

    public AnnotationControlObject(
            String controllerClassName,
            String controllerTagName,
            String controllerRequestMappingValue,
            String methodName,
            String methodOperationSummary,
            String methodOperationDescription,
            String methodRequestMappingValue,
            String controlObjectUrl,
            boolean needToSave
    ) {
        this.controllerClassName = controllerClassName;
        this.controllerTagName = controllerTagName;
        this.controllerRequestMappingValue = controllerRequestMappingValue;
        this.methodName = methodName;
        this.methodOperationSummary = methodOperationSummary;
        this.methodOperationDescription = methodOperationDescription;
        this.methodRequestMappingValue = methodRequestMappingValue;
        this.controlObjectUrl = controlObjectUrl;
        this.needToSave = needToSave;
    }

    public String getControlObjectUrl() {
        return controlObjectUrl;
    }
}
