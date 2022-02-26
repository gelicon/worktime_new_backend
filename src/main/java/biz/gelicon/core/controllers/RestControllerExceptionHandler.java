package biz.gelicon.core.controllers;

import biz.gelicon.core.annotations.ColumnDescription;
import biz.gelicon.core.annotations.TableDescription;
import biz.gelicon.core.dialect.DialectFactory;
import biz.gelicon.core.repository.TableRepository;
import biz.gelicon.core.response.ErrorResponse;
import biz.gelicon.core.response.exceptions.*;
import biz.gelicon.core.utils.OrmUtils;
import biz.gelicon.core.utils.TableMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice(annotations = RestController.class)
public class RestControllerExceptionHandler {
    static Logger logger = LoggerFactory.getLogger(RestControllerExceptionHandler.class);

    public static final int UNKNOWN_ERROR = 100;
    /** Отсутствует сортировка при наличии пагинации */
    public static final int BAD_PAGING_NO_SORT = 123;
    /** Ошибка при выборке данных */
    public static final int FETCH_ERROR = 124;
    /** Ошибка при сохранении данных */
    public static final int POST_ERROR = 125;
    /** Ошибка при удалении данных */
    public static final int DELETE_ERROR = 126;

    public static final int USER_LOCKED = 127;
    public static final int USER_OR_PASSWORD_INCORRECT = 128;
    public static final int TOKEN_INCORRECT = 129;
    public static final int TOKEN_EXPIRED = 140;

    public static final int ACCESS_DENIED = 130;
    public static final int TEMPORARY_ACCESS = 131;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e) {
        e.printStackTrace();

        ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
        errorResponse.setErrorCode(UNKNOWN_ERROR);
        errorResponse.setTimeStamp(new Date().getTime()); // Установим датувремя
        errorResponse.setExceptionClassName(e.getClass().getName()); // установим имя класса

        if(e instanceof AccessDeniedException) {
            errorResponse.setErrorCode(ACCESS_DENIED);
        }
        if(e instanceof HasTemporaryAuthenticationException) {
            errorResponse.setErrorCode(TEMPORARY_ACCESS);
        }

        if (e instanceof UserLockException) {
            errorResponse.setErrorCode(USER_LOCKED);
        }
        if (e instanceof IncorrectUserOrPasswordException) {
            errorResponse.setErrorCode(USER_OR_PASSWORD_INCORRECT);
        }
        if (e instanceof IncorrectTokenException) {
            errorResponse.setErrorCode(TOKEN_INCORRECT);
        }
        if (e instanceof TokenExpiredException) {
            errorResponse.setErrorCode(TOKEN_EXPIRED);
        }
        if (e instanceof BadPagingException) {
            errorResponse.setErrorCode(BAD_PAGING_NO_SORT);
        }
        if (e instanceof FetchQueryException) {
            errorResponse.setErrorCode(FETCH_ERROR);
            errorResponse.setCause(e.getCause()!=null?e.getCause().getMessage():null);
        }
        if (e instanceof PostRecordException) {
            errorResponse.setErrorCode(POST_ERROR);
            // в e.getMessage() общее сообщение ни о чем не говорящее
            errorResponse.setErrorMessage(e.getCause()!=null?e.getCause().getMessage():null);
            // нарушение уникальности
            if(e.getCause() instanceof DuplicateKeyException) {
                errorResponse.setCause(e.getCause().getMessage());
                processDuplicateError((PostRecordException) e, e.getCause().getMessage(),errorResponse);
            } else
            if(e.getCause() instanceof DataIntegrityViolationException) {
                errorResponse.setCause(e.getCause().getMessage());
                DataIntegrityViolationException exIntegrity = (DataIntegrityViolationException) e.getCause();
                // проверяем исключения. для некоторых СУБД DulpicateKeyConstraint попадает в DataIntegrityViolationException
                if(DialectFactory.getDialect().isDulpicateKeyConstraint(exIntegrity.getCause().getMessage())) {
                    processDuplicateError((PostRecordException) e, exIntegrity.getCause().getMessage(),errorResponse);
                } else {
                    processIntegrityViolationForUpdateError(exIntegrity, errorResponse);
                }
            } else
            {
                // Надо извлечь свойство bindingResult
                List<FieldError> fieldErrorList = ((PostRecordException) e).getBindingResult().getFieldErrors();
                Map<String,String> fieldErrors = new HashMap<>();
                for (int i = 0; i < fieldErrorList.size(); i++) {
                    FieldError fldError = fieldErrorList.get(i);
                    String field = fldError.getField();
                    String message = fldError.getDefaultMessage();
                    // когда defaultMessage пустой извлечем из codes
                    if(message==null && fldError.getCodes().length>0) {
                        // берем последний в массиве
                        message = fldError.getCodes()[fldError.getCodes().length - 1];
                    }
                    fieldErrors.put(field,message);
                }
                // и потом из него сделать Map fieldErrors в errorResponse
                errorResponse.setFieldErrors(fieldErrors);
            }
        }
        if (e instanceof DeleteRecordException) {
            errorResponse.setErrorCode(DELETE_ERROR);
            if(e.getCause() instanceof DataIntegrityViolationException) {
                errorResponse.setCause(e.getCause().getMessage());
                processIntegrityViolationForDeleteError((DataIntegrityViolationException) e.getCause(), errorResponse);
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");

        return new ResponseEntity<>(errorResponse, headers, HttpStatus.OK);
    }

    private void processIntegrityViolationForDeleteError(DataIntegrityViolationException ex, ErrorResponse errorResponse) {
        String tableName = DialectFactory.getDialect().extractTableNameFromForeignKeyMessage(ex.getCause().getMessage());
        String tableDesc = "?";
        if(tableName!=null) {
            tableDesc = tableName;
            TableMetadata meta = TableRepository.tableMetadataMap.get(tableName);
            if(meta!=null) {
                TableDescription def = (TableDescription) meta.getModelCls().getAnnotation(TableDescription.class);
                if(def!=null) {
                    tableDesc = def.value();
                }
            }
        }
        errorResponse.setErrorMessage(String.format("Существует ссылка на удаляемую запись из таблицы \"%s\"", tableDesc));
    }

    private void processIntegrityViolationForUpdateError(DataIntegrityViolationException ex, ErrorResponse errorResponse) {
        String tableName = DialectFactory.getDialect().extractTableNameFromForeignKeyMessage(ex.getCause().getMessage());
        String tableDesc = "?";
        if(tableName!=null) {
            tableDesc = tableName;
            TableMetadata meta = TableRepository.tableMetadataMap.get(tableName);
            if(meta!=null) {
                TableDescription def = (TableDescription) meta.getModelCls().getAnnotation(TableDescription.class);
                if(def!=null) {
                    tableDesc = def.value();
                }
            }
        }
        errorResponse.setErrorMessage(String.format("Запрашиваемая ссылка на запись из таблицы \"%s\" неверна, так как такая запись отсутствует", tableDesc));
    }

    private void processDuplicateError(PostRecordException ex, ErrorResponse errorResponse) {
        Field fld = null;
        String fldDesc = "?";
        String columnName = DialectFactory.getDialect().extractColumnNameFromDuplicateMessage(ex.getCause().getMessage());
        if(columnName!=null) {
            String[] fieldsNames = columnName.split(",");
            // есть только одно поле
            if(fieldsNames.length==1) {
                fld = OrmUtils.getField(ex.getModelCls(), fieldsNames[0]);
            }
            for (int i = 0; i < fieldsNames.length; i++) {
                fieldsNames[i] = getColumnDesc(fieldsNames[i],ex);
            }
            fldDesc = String.join(",",fieldsNames);
        }

        String prettyMessage = String.format("Запись с аналогичным значением \"%s\" уже существует", fldDesc);
        errorResponse.setErrorMessage(prettyMessage);

        Map<String,String> fieldErrors = new HashMap<>();
        if(fld!=null) {
            fieldErrors.put(fld.getName(),prettyMessage);
        } else {
            fieldErrors.put("",prettyMessage);
        }
        errorResponse.setFieldErrors(fieldErrors);

    }

    private void processDuplicateError(PostRecordException ex, String message,ErrorResponse errorResponse) {
        Field fld = null;
        String fldDesc = "?";
        String columnName = DialectFactory.getDialect().extractColumnNameFromDuplicateMessage(message);
        if(columnName!=null) {
            String[] fieldsNames = columnName.split(",");
            // есть только одно поле
            if(fieldsNames.length==1) {
                fld = OrmUtils.getField(ex.getModelCls(), fieldsNames[0]);
            }
            for (int i = 0; i < fieldsNames.length; i++) {
                fieldsNames[i] = getColumnDesc(fieldsNames[i],ex);
            }
            fldDesc = String.join(",",fieldsNames);
        }

        String prettyMessage = String.format("Запись с аналогичным значением \"%s\" уже существует", fldDesc);
        errorResponse.setErrorMessage(prettyMessage);

        Map<String,String> fieldErrors = new HashMap<>();
        if(fld!=null) {
            fieldErrors.put(fld.getName(),prettyMessage);
        } else {
            fieldErrors.put("",prettyMessage);
        }
        errorResponse.setFieldErrors(fieldErrors);

    }


    private String getColumnDesc(String columnName,PostRecordException ex) {
        String fldDesc = columnName;
        if(columnName!=null) {
            Field fld = OrmUtils.getField(ex.getModelCls(), columnName);
            if(fld!=null) {
                fldDesc=fld.getName();
                ColumnDescription col = fld.getAnnotation(ColumnDescription.class);
                if(col!=null) {
                    fldDesc=col.value();
                }
            }
        }
        return fldDesc;
    }

}
