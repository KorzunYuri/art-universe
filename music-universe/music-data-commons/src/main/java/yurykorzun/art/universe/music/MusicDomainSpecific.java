package yurykorzun.art.universe.music;

import yurykorzun.art.universe.common.DomainSpecificType;

public interface MusicDomainSpecific extends DomainSpecificType {

    @Override
    default String getDomainCode(){
        return MusicDomainConstants.DOMAIN_CODE;
    }

}
