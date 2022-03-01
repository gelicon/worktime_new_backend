package biz.gelicon.core.maintenance;

import biz.gelicon.core.annotations.MethodPermission;
import biz.gelicon.core.artifacts.ArtifactManagerImpl;
import biz.gelicon.core.model.ControlObject;
import biz.gelicon.core.reports.ReportManagerImpl;
import biz.gelicon.core.repository.ApplicationRepository;
import biz.gelicon.core.repository.ControlObjectRepository;
import biz.gelicon.core.utils.ReflectUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Сервисный класс для обслуживания Системы
 *
 * @author dav
 */
@Service
public class MaintenanceSystemService {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceSystemService.class);
    private boolean developFlag = false; // Для подрбного логирования установить

    @Autowired
    ControlObjectRepository controlObjectRepository;

    @Autowired
    ReportManagerImpl reportManager;

    @Autowired
    ArtifactManagerImpl artifactManager;

    @Autowired
    ApplicationRepository applicationRepository;

    /** префикс пакетов системы */
    @Value("${gelicon.core.prefix:biz.gelicon.core}")
    private String geliconCorePrefix;

    /**
     * автоматическое заполнение таблицы ControlObject при запуске бекенда Сканирует все аннотации
     * RestController
     *
     * @param prefix префикс пакетов для сканирования, например, "biz.gelicon.core"
     */
    public void fillControlObject(String prefix) {
        logger.info("Actualizing table ControlObject ...");
        if (prefix == null) {
            prefix = "biz.gelicon.core";
        }
        // Считаем все объекты контроля из базы.
        List<ControlObject> controlObjectList = controlObjectRepository.findAll();
        // Получим все контроллеры просканировав пакеты
        logDebug("Reading @RestController annotation...");
        // Считываем все аннотации RestController
        Set<Class<?>> set = getTypesAnnotatedWithRestController(prefix);
        // Получим все аннотации всех методов контроллеров
        List<AnnotationControlObject> annotationControlObjectList =
                getAnnotationControlObjectList(set);
        // Скорректируем базу данных
        correctControlObjectRepository(controlObjectList, annotationControlObjectList);
        logger.info("Actualizing table ControlObject ...Ok");
    }

    /**
     * Корректирует ControlObject в базе на основе всех аннотаций всех методов
     */
    public void correctControlObjectRepository(
            List<ControlObject> controlObjectList,
            List<AnnotationControlObject> annotationControlObjectList
    ) {
        // Удалим из базы все, которых нет в аннотациях
        // Сравниваем по УРЛу
        controlObjectList.forEach(c -> { // Цикл по всем объектам из базы
            // Попробуем найти в аннотациях по урл
            AnnotationControlObject aco = annotationControlObjectList.stream()
                    .filter(a -> a.getControlObjectUrl().equals(c.getControlObjectUrl()))
                    .findFirst()
                    .orElse(null);
            if (aco != null) { // Есть в аннотациях - update
                // Подправим наименование, если они не равны
                String newName = aco.controllerTagName + ": " + aco.methodOperationSummary;
                if (!c.getControlObjectName().equals(newName)) {
                    c.setControlObjectName(newName);
                    controlObjectRepository.update(c); // изменим в бд
                }
                // Удалим из коллекции, чтобы потом не проверять
                annotationControlObjectList.remove(aco);
            } else { // Нет в аннотациях - delete
                controlObjectRepository.deleteCascade(c.controlObjectId);
            }
        });
        // Скорректируем генератор
        controlObjectRepository.correctSequence();
        // Добавим из аннотаций оставшиеся, которых нет в базе
        annotationControlObjectList.forEach(a -> {
            ControlObject co = new ControlObject();
            co.setControlObjectName(a.controllerTagName + ": " + a.methodOperationSummary);
            co.setControlObjectUrl(a.getControlObjectUrl());
            controlObjectRepository.insert(co);
        });
    }

    /**
     * Возвращает все аннотации всех методов аннотированных RequestMapping для списка классов,
     * переданных в качестве параметра
     *
     * @return список объектов
     */
    public List<AnnotationControlObject> getAnnotationControlObjectList(
            Set<Class<?>> classSet
    ) {
        List<AnnotationControlObject> list = new ArrayList<>();
        // Цикл по всем классам, аннотированным как RestController
        classSet.forEach(s -> {
            list.addAll(getAnnotationControlObjectClassList(s));
        });
        return list;
    }

    /**
     * Возвращает список описания аннотированных методов для класса
     *
     * @param s
     * @return
     */
    public List<AnnotationControlObject> getAnnotationControlObjectClassList(
            Class<?> s
    ) {
        List<AnnotationControlObject> list = new ArrayList<>();
        String controllerClassName = s.getName(); // Имя контроллера
        logDebug(controllerClassName);
        Annotation[] annotations = s.getAnnotations(); // Все аннотации контроллера
        // Найдем аннотацию MethodPermission
        if (isMethodPermissionNoStore(annotations)) {
            // если она есть - не добавляем контроллер
            logDebug("  Установлен MethodPermissionNoStore - не добавляем");
            return list;
        }
        // Найдем аннотацию Tag
        String controllerTagName = getControllerTagName(annotations);
        // Если аннотации нет - просто имя класса
        if (controllerTagName == null) {
            controllerTagName = controllerClassName;
        }
        logDebug(" " + controllerTagName);
        // Найдем аннотацию RequestMapping
        String controllerRequestMappingValue = getRequestMappingValue(annotations);
        logDebug("   " + controllerRequestMappingValue);
        // Список методов
        Method[] methods = s.getMethods();
        String finalControllerTagName = controllerTagName;
        Arrays.stream(methods) // Цикл по методам
                .forEach(m -> {
                    String methodName = m.getName(); // Имя метода
                    logDebug("   " + methodName);
                    // Список аннотаций метода
                    Annotation[] mAnnotations = m.getAnnotations();
                    // Найдем аннотацию Operation для метода
                    String methodOperationSummary = getOperationSummary(mAnnotations);
                    // Если аннотации нет - просто имя метода
                    if (methodOperationSummary == null) {
                        methodOperationSummary = methodName;
                    }
                    logDebug("      " + methodOperationSummary);
                    String methodOperationDescription = getOperationDescription(
                            mAnnotations);
                    logDebug("      " + methodOperationDescription);
                    // Найдем аннотацию RequestMapping value для метода
                    String methodRequestMappingValue = getRequestMappingValue(
                            mAnnotations);
                    logDebug("      " + methodRequestMappingValue);
                    // Урл у метода должен быть
                    if (methodRequestMappingValue == null) {
                        return;
                    }
                    // сохранять этот метод или нет
                    boolean needToSave = ReflectUtils
                            .isRequestMappingMethodWithSuffix(m, methodRequestMappingValue);
                    if (!needToSave) {
                        // Писать не надо
                        logDebug("         Писать не надо");
                        return;
                    }
                    // Полный урл
                    String controlObjectUrl = controllerRequestMappingValue + "/"
                            + methodRequestMappingValue;
                    Map<String, String> methodList = new HashMap<>();
                    // Проверим, не save ли это метод
                    if (ReflectUtils.isSaveMethod(m)) { // Надо создать две записи
                        methodList.put(
                                controlObjectUrl + "#ins",
                                methodOperationSummary + " - вставка"
                        );
                        methodList.put(
                                controlObjectUrl + "#upd",
                                methodOperationSummary + " - изменение"
                        );
                    } else { // Одна запись
                        methodList.put(
                                controlObjectUrl,
                                methodOperationSummary
                        );
                    }
                    // Добавим две или одну записи
                    for (Map.Entry<String, String> entry : methodList.entrySet()) {
                        // Сожраним в коллекции
                        list.add(new AnnotationControlObject(
                                controllerClassName,
                                finalControllerTagName,
                                controllerRequestMappingValue,
                                methodName,
                                entry.getValue(),
                                methodOperationDescription,
                                methodRequestMappingValue,
                                entry.getKey(),
                                needToSave
                        ));
                    }
                });
        return list;
    }

    private void logDebug(String message) {
        if (developFlag) {
            logger.info(message);
        }
    }

    /**
     * Считываем все аннотации RestController
     *
     * @param prefix Откуда считывать
     * @return
     */
    public Set<Class<?>> getTypesAnnotatedWithRestController(String prefix) {
        Reflections reflections = new Reflections(prefix); // Рефлектор
        return reflections.getTypesAnnotatedWith(RestController.class);
    }

    public void setDevelopFlag(boolean developFlag) {
        this.developFlag = developFlag;
    }

    /**
     * Возвращает аннотацию Tag name для контроллера
     *
     * @param annotations
     * @return
     */
    public String getControllerTagName(Annotation[] annotations) {
        // Найдем аннотацию Tag
        return Arrays.stream(annotations)
                .filter(Tag.class::isInstance)
                .map(a -> ((Tag) a).name())
                .findAny()
                .orElse(null);
    }

    /**
     * Проверяет наличие аннотации MethodPermission с noStore=true
     *
     * @param annotations - список аннотаций
     * @return если есть и true - возвращает true, иначе - false
     */
    public boolean isMethodPermissionNoStore(Annotation[] annotations) {
        // Найдем аннотацию MethodPermission
        return Arrays.stream(annotations)
                .filter(MethodPermission.class::isInstance)
                .map(a -> ((MethodPermission) a).noStore())
                .findAny()
                .orElse(false);
    }

    /**
     * Возвращает аннотацию Tag value для контроллера или метода
     *
     * @param annotations
     * @return
     */
    public String getRequestMappingValue(Annotation[] annotations) {
        // Найдем аннотацию Tag
        return Arrays.stream(annotations)
                .filter(RequestMapping.class::isInstance)
                .map(a -> ((RequestMapping) a).value()[0])
                .findAny()
                .orElse(null);
    }

    /**
     * Возвращает аннотацию Operation description для метода
     *
     * @param annotations
     * @return
     */
    public String getOperationDescription(Annotation[] annotations) {
        // Найдем аннотацию Operation
        return Arrays.stream(annotations)
                .filter(Operation.class::isInstance)
                .map(a -> ((Operation) a).description())
                .findAny()
                .orElse(null);
    }

    /**
     * Возвращает аннотацию Operation summary для метода
     *
     * @param annotations
     * @return
     */
    public String getOperationSummary(Annotation[] annotations) {
        // Найдем аннотацию Operation
        return Arrays.stream(annotations)
                .filter(Operation.class::isInstance)
                .map(a -> ((Operation) a).summary())
                .findAny()
                .orElse(null);
    }

}
