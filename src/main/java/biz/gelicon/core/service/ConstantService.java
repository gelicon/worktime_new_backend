package biz.gelicon.core.service;

import biz.gelicon.core.model.Constant;
import biz.gelicon.core.repository.ConstantRepository;
import biz.gelicon.core.validators.ConstantValidator;
import biz.gelicon.core.view.ConstantView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class ConstantService extends BaseService<Constant> {
    private static final Logger logger = LoggerFactory.getLogger(ConstantService.class);

    @Autowired
    private ConstantRepository constantRepository;
    @Autowired
    private ConstantValidator constantValidator;

    @PostConstruct
    public void init() {
        init(constantRepository, constantValidator);
    }


    public ConstantView getConstant(Integer id) {
        List<ConstantView> list = constantRepository.findQuery(ConstantView.class, "" +
                "SELECT c.*," +
                "cc.capclass_name " +
                "FROM constant c " +
                "INNER JOIN capclass cc ON cc.capclass_id=c.constantgroup_id " +
                "WHERE c.constant_id=:constantId", "constantId", id);
        return list.isEmpty()?null:list.get(0);
    }

}

