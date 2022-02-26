package biz.gelicon.core.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class Components {
    /*
    @Autowired
    private StreetRepositoryGC streetRepositoryGC;
    @Autowired
    private StreetRepositoryERP streetRepositoryERP;
    @Autowired
    private AddressRepositoryGC addressRepositoryGC;
    @Autowired
    private AddressRepositoryERP addressRepositoryERP;

    @Bean
    public StreetRepository streetRepository() {
        return (StreetRepository)(Config.CURRENT_EDITION_TAG == EditionTag.GELICON_UTILITIES?streetRepositoryGC:streetRepositoryERP);
    }

    @Bean
    public AddressRepository addressRepository() {
        return (AddressRepository)(Config.CURRENT_EDITION_TAG == EditionTag.GELICON_UTILITIES?addressRepositoryGC:addressRepositoryERP);
    }

     */

}
