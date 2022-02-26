package biz.gelicon.core.service;

import biz.gelicon.core.model.Edizm;
import biz.gelicon.core.repository.EdizmRepository;
import biz.gelicon.core.utils.GridDataOption;
import biz.gelicon.core.utils.Query;
import biz.gelicon.core.validators.EdizmValidator;
import biz.gelicon.core.view.EdizmView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class EdizmService extends BaseService<Edizm> {
    private static final Logger logger = LoggerFactory.getLogger(EdizmService.class);
    public static final String ALIAS_MAIN = "M0";
    public static final String MAINTABLE_NAME = "edizm";

    @Autowired
    private EdizmRepository edizmRepository;
    @Autowired
    private EdizmValidator edizmValidator;

    // главный запрос. используется в главной таблице
    // в контроллере используется в getlist и save
    final String mainSQL=String.format(
            "SELECT * " +
            "FROM   " + MAINTABLE_NAME + " %s /*FROM_PLACEHOLDER*/ " +
            "WHERE  1=1 /*WHERE_PLACEHOLDER*/ " +  //WHERE_PLACEHOLDER если не пуст, всегда добавляет and, поэтому требуется 1=1
            "/*ORDERBY_PLACEHOLDER*/"
            ,ALIAS_MAIN);

    // todo Разобраться
    /*
    @PostConstruct
    public void init() {
        init(edizmRepository, edizmValidator);
    }

     */

    public List<EdizmView> getMainList(GridDataOption gridDataOption) {
        return new Query.QueryBuilder<EdizmView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setPageableAndSort(gridDataOption.buildPageRequest())
                .setFrom(gridDataOption.buildFullTextJoin(MAINTABLE_NAME,ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(EdizmView.class,ALIAS_MAIN))
                .setParams(gridDataOption.buildQueryParams())
                .build(EdizmView.class)
                .execute();
    }

    public int getMainCount(GridDataOption gridDataOption) {
        return new Query.QueryBuilder<EdizmView>(mainSQL)
                .setMainAlias(ALIAS_MAIN)
                .setFrom(gridDataOption.buildFullTextJoin(MAINTABLE_NAME,ALIAS_MAIN))
                .setPredicate(gridDataOption.buildPredicate(EdizmView.class,ALIAS_MAIN))
                .setParams(gridDataOption.buildQueryParams())
                .build(EdizmView.class)
                .count();

    }


    public EdizmView getOne(Integer id) {
        return new Query.QueryBuilder<EdizmView>(mainSQL)
                .setPredicate(ALIAS_MAIN + "." + MAINTABLE_NAME + "_id = :" + MAINTABLE_NAME + "Id")
                .build(EdizmView.class)
                .executeOne(MAINTABLE_NAME + "Id", id);
    }
}
